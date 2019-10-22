/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.ExportsWebComponent;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;
import com.vaadin.flow.portal.util.PortletHubUtil;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.util.SharedUtil;

/**
 * Vaadin implementation of the {@link GenericPortlet}.
 *
 * @since
 */
@PreserveOnRefresh
public abstract class VaadinPortlet<C extends Component> extends GenericPortlet
        implements ExportsWebComponent<C> {

    private static final String VAADIN_PREFIX = "vaadin.";
    private static final String VAADIN_EVENT = VAADIN_PREFIX + "event";
    private static final String ACTION_STATE = "state";
    private static final String ACTION_MODE = "mode";

    private VaadinPortletService vaadinService;
    private String webComponentProviderURL;
    private String webComponentBootstrapHandlerURL;
    private String webComponentUIDLRequestHandlerURL;

    private String windowState = WindowState.UNDEFINED.toString();
    private String portletMode = PortletMode.UNDEFINED.toString();
    private Map<String, String> actionURL = new HashMap<>();

    private boolean isPortlet3 = false;
    // TODO: As a temporary crutch target the last instantiated view.
    // TODO: Create a portlet-instance mapping (#45) for event dispatching.
    private C viewInstance = null;

    @Override
    public void init(PortletConfig config) throws PortletException {
        CurrentInstance.clearAll();
        super.init(config);
        Properties initParameters = new Properties();

        // Read default parameters from the context
        final PortletContext context = config.getPortletContext();
        for (final Enumeration<String> e = context.getInitParameterNames(); e
                .hasMoreElements();) {
            final String name = e.nextElement();
            initParameters.setProperty(name, context.getInitParameter(name));
        }

        // Override with application settings from portlet.xml
        for (final Enumeration<String> e = config.getInitParameterNames(); e
                .hasMoreElements();) {
            final String name = e.nextElement();
            initParameters.setProperty(name, config.getInitParameter(name));
        }

        DeploymentConfiguration deploymentConfiguration = createDeploymentConfiguration(
                initParameters);
        try {
            vaadinService = createPortletService(deploymentConfiguration);
        } catch (ServiceException e) {
            throw new PortletException("Could not initialize VaadinPortlet", e);
        }
        // Sets current service even though there are no request and response
        VaadinService.setCurrent(null);

        portletInitialized();

        CurrentInstance.clearAll();
    }

    @Override
    public void configure(WebComponent<C> webComponent, C component) {
        if (VaadinPortlet.getCurrent() != null) {
            // Cannot use 'this' as it is only a temporary object created by
            // WebComponentExporter handling logic
            VaadinPortlet<C> thisPortlet = VaadinPortlet.getCurrent();
            thisPortlet.viewInstance = component;
        }
    }

    /**
     * Sends the given {@link PortletModeEvent} to the given view instance of
     * this portlet.
     *
     * @param view
     *            the view instance
     * @param event
     *            the event object
     */
    protected void fireModeChange(PortletModeHandler view,
            PortletModeEvent event) {
        view.portletModeChange(event);
    }

    /**
     * Sends the given {@link WindowStateEvent} to the given view instance of
     * this portlet.
     *
     * @param view
     *            the view instance
     * @param event
     *            the event object
     */
    protected void fireWindowStateChange(WindowStateHandler view,
            WindowStateEvent event) {
        view.windowStateChange(event);
    }

    private boolean isViewInstanceOf(Class<?> instance) {
        return instance.isAssignableFrom(getComponentClass());
    }

    protected DeploymentConfiguration createDeploymentConfiguration(
            Properties initParameters) {
        initParameters.put(Constants.SERVLET_PARAMETER_COMPATIBILITY_MODE,
                "false");
        return new DefaultDeploymentConfiguration(getClass(), initParameters);
    }

    protected VaadinPortletService createPortletService(
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        VaadinPortletService service = new VaadinPortletService(this,
                deploymentConfiguration);
        service.init();
        return service;
    }

    protected VaadinPortletService getService() {
        return vaadinService;
    }

    protected void portletInitialized() throws PortletException {

    }

    @Override
    public void renderHeaders(HeaderRequest request, HeaderResponse response)
            throws PortletException, IOException {
        // This is only called for portlet 3.0 portlets.
        isPortlet3 = true;
        response.addDependency("PortletHub", "javax.portlet", "3.0.0");
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);
        if (!isPortlet3 && !actionURL.containsKey(response.getNamespace())) {
            actionURL.put(response.getNamespace(),
                    response.createActionURL().toString());
        }
        /*
         * Note: mode update events must be sent to handlers before window state
         * update events.
         */
        String oldMode = portletMode;
        portletMode = request.getPortletMode().toString();
        if (!oldMode.equals(portletMode)
                && isViewInstanceOf(PortletModeHandler.class)
                && viewInstance != null) {
            fireModeChange((PortletModeHandler) viewInstance,
                    new PortletModeEvent(new PortletMode(portletMode),
                            new PortletMode(oldMode)));
        }
        String oldWindowState = windowState;
        windowState = request.getWindowState().toString();
        if (!oldWindowState.equals(windowState)
                && isViewInstanceOf(WindowStateHandler.class)
                && viewInstance != null) {
            fireWindowStateChange((WindowStateHandler) viewInstance,
                    new WindowStateEvent(new WindowState(windowState),
                            new WindowState(oldWindowState)));
        }
    }

    @Override
    protected void doDispatch(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        try {
            // try to let super handle - it'll call methods annotated for
            // handling, the default doXYZ(), or throw if a handler for the mode
            // is not found
            super.doDispatch(request, response);

        } catch (PortletException e) {
            if (e.getCause() == null) {
                // No cause interpreted as 'unknown mode' - pass that trough
                // so that the application can handle
                handleRequest(request, response);

            } else {
                // Something else failed, pass on
                throw e;
            }
        }
    }

    /**
     * Wraps the request in a (possibly portal specific) Vaadin portlet request.
     *
     * @param request
     *            The original PortletRequest
     * @return A wrapped version of the PortletRequest
     */
    protected VaadinPortletRequest createVaadinRequest(PortletRequest request) {
        VaadinPortletService service = getService();
        return new VaadinPortletRequest(request, service);
    }

    private VaadinPortletResponse createVaadinResponse(
            PortletResponse response) {
        return new VaadinPortletResponse(response, getService());
    }

    @Override
    public void serveResource(ResourceRequest request,
            ResourceResponse response) throws PortletException {
        handleRequest(request, response);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException {
        Set<String> names = request.getActionParameters().getNames();
        if (!isPortlet3 && names.contains(ACTION_STATE)) {
            WindowState windowState = new WindowState(
                    request.getActionParameters().getValue(ACTION_STATE));
            PortletMode portletMode = new PortletMode(
                    request.getActionParameters().getValue(ACTION_MODE));

            response.setWindowState(windowState);
            response.setPortletMode(portletMode);
        } else if (names.contains(VAADIN_EVENT)
                && (viewInstance instanceof EventHandler)) {
            String event = request.getActionParameters().getValue(VAADIN_EVENT);
            Map<String, String[]> map = new HashMap<>(
                    request.getParameterMap());
            if (event.startsWith(VAADIN_PREFIX)) {
                event = event.substring(VAADIN_PREFIX.length());
            }
            String ev = event;
            map.remove(VAADIN_EVENT);

            assert viewInstance.getElement().getNode().isAttached();
            VaadinSession session = viewInstance.getUI().get().getSession();
            session.access(() -> ((EventHandler) viewInstance)
                    .handleEvent(new PortletEvent(ev, map)));
        }
    }

    @Override
    public void processEvent(EventRequest request, EventResponse response)
            throws PortletException {
        handleRequest(request, response);
    }

    protected void handleRequest(PortletRequest request,
            PortletResponse response) throws PortletException {

        CurrentInstance.clearAll();

        try {
            getService().handleRequest(createVaadinRequest(request),
                    createVaadinResponse(response));
        } catch (ServiceException e) {
            throw new PortletException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        getService().destroy();
    }

    /**
     * Gets the currently used Vaadin portlet. The current portlet is
     * automatically defined when processing requests related to the service
     * (see {@link ThreadLocal}) and in {@link VaadinSession#access(Command)}
     * and {@link UI#access(Command)}. In other cases, (e.g. from background
     * threads, the current service is not automatically defined.
     *
     * The current portlet is derived from the current service using
     * {@link VaadinService#getCurrent()}
     *
     * @return the current vaadin portlet instance if available, otherwise
     *         <code>null</code>
     * @since 7.0
     */
    public static VaadinPortlet getCurrent() {
        VaadinService vaadinService = CurrentInstance.get(VaadinService.class);
        if (vaadinService instanceof VaadinPortletService) {
            VaadinPortletService vps = (VaadinPortletService) vaadinService;
            return vps.getPortlet();
        } else {
            return null;
        }
    }

    /**
     * Gets the tag for the main component in the portlet.
     * <p>
     * By default derives the tag name from the class name.
     *
     * @return the tag of the main component to use
     */
    @Override
    public String getTag() {
        if (getClass().isAnnotationPresent(Tag.class)) {
            Tag tag = getClass().getAnnotation(Tag.class);
            return tag.value();
        } else {
            String candidate = SharedUtil
                    .camelCaseToDashSeparated(getClass().getSimpleName())
                    .replaceFirst("^-", "");
            if (!candidate.contains("-")) {
                candidate = candidate + "-portlet";

            }
            return candidate;
        }
    }

    public void setWebComponentProviderURL(String url) {
        webComponentProviderURL = url;
    }

    public String getWebComponentProviderURL() {
        return webComponentProviderURL;
    }

    public void setWebComponentBootstrapHandlerURL(String url) {
        webComponentBootstrapHandlerURL = url;
    }

    public String getWebComponentBootstrapHandlerURL() {
        return webComponentBootstrapHandlerURL;
    }

    public void setWebComponentUIDLRequestHandlerURL(String url) {
        webComponentUIDLRequestHandlerURL = url;
    }

    public String getWebComponentUIDLRequestHandlerURL() {
        return webComponentUIDLRequestHandlerURL;
    }

    /**
     * Get the window state for this portlet.
     *
     * @return window state
     */
    public WindowState getWindowState() {
        return new WindowState(windowState);
    }

    /**
     * Get the portlet mode for this portlet.
     *
     * @return portlet mode
     */
    public PortletMode getPortletMode() {
        return new PortletMode(portletMode);
    }

    /**
     * Set a new window state for this portlet
     *
     * @param newWindowState
     *            window state to set
     */
    public void setWindowState(WindowState newWindowState) {
        if (isPortlet3) {
            PortletHubUtil.updatePortletState(newWindowState.toString(),
                    portletMode);
        } else if (actionURL.containsKey(getNamespace())) {
            stateChangeAction(newWindowState.toString(), portletMode);
        }
    }

    /**
     * Set a new portlet mode for this portlet.
     *
     * @param newPortletMode
     *            portlet mode to set
     */
    public void setPortletMode(PortletMode newPortletMode) {
        if (isPortlet3) {
            PortletHubUtil.updatePortletState(windowState,
                    newPortletMode.toString());
        } else if (actionURL.containsKey(getNamespace())) {
            stateChangeAction(windowState, newPortletMode.toString());
        }
    }

    /**
     * Send an event with the given parameters with the {@code portletComponent}
     * as a source.
     * <p>
     * If {@code eventName} has {@code "vaadin."} prefix then Vaadin Portlet
     * will send this event to the server as an action event out of the box.
     * Such event will be handled by the
     * {@link VaadinPortlet#processAction(ActionRequest, ActionResponse)}
     * method.
     *
     * @param portletComponent
     *            a source component
     * @param eventName
     *            an event name
     * @param parameters
     *            parameters to add to event action
     */
    public void sendEvent(Component portletComponent, String eventName,
            Map<String, String> parameters) {
        StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append(PortletHubUtil.getHubString());
        eventBuilder.append("var params = hub.newParameters();");
        eventBuilder.append("params['action'] = ['send'];");
        parameters.forEach((key, value) -> eventBuilder
                .append(String.format("params['%s'] = ['%s'];", key, value)));
        eventBuilder.append(String
                .format("hub.dispatchClientEvent('%s', params);", eventName));

        portletComponent.getElement().executeJs(eventBuilder.toString());
    }

    /**
     * Adds a client side (JavaScript) event listener for the {@code eventName}.
     *
     * @param portletComponent
     *            a context portlet component
     * @param eventName
     *            an event name
     * @param listenerFactory
     *            a factory which produces a JS expression which will be
     *            executed as a listener for the event, the factory may use the
     *            first parameter to access to the event type (name) and the
     *            second parameter to access to the event payload.
     */
    public void addEventListener(Component portletComponent, String eventName,
            BiFunction<String, String, String> listenerFactory) {
        StringBuilder listenerBuilder = new StringBuilder();
        listenerBuilder.append(PortletHubUtil.getHubString());
        listenerBuilder.append("hub.addEventListener('").append(eventName)
                .append("',");
        listenerBuilder.append("function(type, payload){")
                .append(listenerFactory.apply("type", "payload")).append("});");
        portletComponent.getElement().executeJs(listenerBuilder.toString());
    }

    private void stateChangeAction(String state, String mode) {
        String namespace = getNamespace();
        if (actionURL.containsKey(namespace)) {
            String stateChangeScript = String.format(
                    "location.href = '%s?%s=%s&%s=%s'",
                    actionURL.get(namespace), ACTION_STATE, state, ACTION_MODE,
                    mode);

            UI.getCurrent().getPage().executeJs(stateChangeScript);
        }
    }

    private String getNamespace() {
        return VaadinPortletService.getCurrentResponse().getPortletResponse()
                .getNamespace();
    }

}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.vaadin.flow.server.SessionExpiredException;
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

    private static final String ACTION_STATE = "state";
    private static final String ACTION_MODE = "mode";

    private VaadinPortletService vaadinService;

    private boolean isPortlet3 = false;

    private Map<String, String> actionURL = new HashMap<>();

    private Map<String, String> webComponentProviderURL = new HashMap<>();
    private Map<String, String> webComponentBootstrapHandlerURL = new HashMap<>();
    private Map<String, String> webComponentUIDLRequestHandlerURL = new HashMap<>();

    /*
     * The session currently stores a number of maps with the following keys:
     *
     * "<portlet class name>-view": namespace to view mapping
     * "<portlet class name>-mode": namespace to portlet mode mapping
     * "<portlet class name>-windowState": namespace to window state mapping
     *
     * Note that this is not enough to work correctly, for the following reason.
     *
     * Given a single session and namespace, there will be two different view
     * instances (each with their own state) for every opened browser window
     * that contains the portlet. This mean the window name also needs to be
     * incorporated in this map (and it needs to be sent from the client
     * with each portlet request so that we can resolve to the correct view
     * instance when dispatching events).
     *
     * TODO: This should be fixed as part of https://github.com/vaadin/flow/issues/6373
     *
     *   VaadinPortletSession(1)   VaadinPortletSession(2)
     *              V                       V
     *              │                       │
     * ╔════════════╪═══════════════════════╪════════════╗
     * ║ Window(1)  │                       │            ║
     * ╟────────────┼───────────────────────┼───────────-╢
     * ║ ┌──────────┼──────────┐ ┌──────────┼──────────┐ ║
     * ║ │ MyPortlet│(L)       │ │ MyPortlet│(R)       │ ║
     * ║ ├──────────┼──── ─────┤ ├──────────┼──────────┤ ║
     * ║ │ ┌────────┼────────┐ │ │ ┌────────┼────────┐ │ ║
     * ║ │ │ View(1)│        │ │ │ │ View(2)│        │ │ ║
     * ║ │ └────────┼────────┘ │ │ └────────┼────────┘ │ ║
     * ║ └──────────┼──────────┘ └──────────┼──────────┘ ║
     * ╚════════════╪═══════════════════════╪════════════╝
     *              │                       │
     * ╔════════════╪═══════════════════════╪════════════╗
     * ║ Window(2)  │                       │            ║
     * ╟────────────┼───────────────────────┼───────────-╢
     * ║ ┌──────────┼──────────┐ ┌──────────┼──────────┐ ║
     * ║ │ MyPortlet│(L)       │ │ MyPortlet│(R)       │ ║
     * ║ ├──────────┼──── ─────┤ ├──────────┼──────────┤ ║
     * ║ │ ┌────────┼────────┐ │ │ ┌────────┼────────┐ │ ║
     * ║ │ │ View(3)│        │ │ │ │ View(4)│        │ │ ║
     * ║ │ └────────┼────────┘ │ │ └────────┼────────┘ │ ║
     * ║ └──────────┼──────────┘ └──────────┼──────────┘ ║
     * ╚════════════╪═══════════════════════╪════════════╝
     *              │                       │
     *              V                       V
     *
     * In the above scenario, the same portal page contains two instances of
     * the same portlet in namespaces L and R. The page itself is open in two
     * different browser windows. There are then two VaadinPortletSession
     * instances (one for MyPortlet(L) and one for MyPortlet(R), and four
     * view instances. Each view instance has its own mode and window state.
     */
    private final static String VIEW_SESSION_SUBKEY = "view";
    private final static String MODE_SESSION_SUBKEY = "mode";
    private final static String WINDOWSTATE_SESSION_SUBKEY = "windowState";

    @Override
    public void init(PortletConfig config) throws PortletException {
        CurrentInstance.clearAll();
        super.init(config);
        Properties initParameters = new Properties();

        // Read default parameters from the context
        final PortletContext context = config.getPortletContext();
        for (final Enumeration<String> e = context.getInitParameterNames(); e
                .hasMoreElements(); ) {
            final String name = e.nextElement();
            initParameters.setProperty(name, context.getInitParameter(name));
        }

        // Override with application settings from portlet.xml
        for (final Enumeration<String> e = config.getInitParameterNames(); e
                .hasMoreElements(); ) {
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
    public void configure(WebComponent<C> webComponent, C view) {
        // Cannot use 'this' as it is only a temporary object created by
        // WebComponentExporter handling logic
        VaadinPortlet<C> portlet = VaadinPortlet.getCurrent();
        PortletResponse response = VaadinPortletResponse.getCurrentPortletResponse();
        VaadinSession session = null;
        try {
            session = portlet.getService().findVaadinSession(
                    VaadinPortletRequest.getCurrent());
        } catch (SessionExpiredException e) {
            getLogger().error("Session expired", e);
        }

        if (session != null) {
            // Initialize maps for view component, mode and window state if
            // needed and add to session
            Map<String, C> views = (Map<String, C>) session.getAttribute(getViewMapSessionKey(VIEW_SESSION_SUBKEY));
            if (views == null) {
                views = new HashMap<>();
            }
            views.put(response.getNamespace(), view);
            session.setAttribute(getViewMapSessionKey(VIEW_SESSION_SUBKEY), views);

            Map<String, String> modes = (Map<String, String>) session.getAttribute(getViewMapSessionKey(MODE_SESSION_SUBKEY));
            if (modes == null) {
                modes = new HashMap<>();
            }
            modes.put(response.getNamespace(), PortletMode.UNDEFINED.toString());
            session.setAttribute(getViewMapSessionKey(MODE_SESSION_SUBKEY), modes);

            Map<String, String> windowStates = (Map<String, String>) session.getAttribute(getViewMapSessionKey(WINDOWSTATE_SESSION_SUBKEY));
            if (windowStates == null) {
                windowStates = new HashMap<>();
            }
            windowStates.put(response.getNamespace(), WindowState.UNDEFINED.toString());
            session.setAttribute(getViewMapSessionKey(WINDOWSTATE_SESSION_SUBKEY), windowStates);
        } else {
            getLogger().error("Could not find session for portlet " + getPortletName()
                    + ", namespace " + getNamespace());
        }
    }

    /**
     * Sends the given {@link PortletModeEvent} to the given view instance of
     * this portlet.
     *
     * @param view  the view instance
     * @param event the event object
     */
    protected void fireModeChange(PortletModeHandler view,
                                  PortletModeEvent event) {
        view.portletModeChange(event);
    }

    /**
     * Sends the given {@link WindowStateEvent} to the given view instance of
     * this portlet.
     *
     * @param view  the view instance
     * @param event the event object
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
        initParameters
                .put(Constants.SERVLET_PARAMETER_COMPATIBILITY_MODE, "false");
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

        String namespace = response.getNamespace();

        if (!isPortlet3 && !actionURL.containsKey(response.getNamespace())) {
            actionURL.put(namespace, response.createActionURL().toString());
        }

        VaadinPortletSession session = null;
        try {
            session = (VaadinPortletSession)
                    getService().findVaadinSession(createVaadinRequest(request));
            if (session == null) {
                getLogger().error("Could not find session for portlet " + getPortletName()
                        + ", namespace " + getNamespace());
                return;
            }
        } catch (SessionExpiredException e) {
            getLogger().error("Session expired", e);
            return;
        }
        Map<String, C> views = (Map<String, C>) session.getAttribute(getViewMapSessionKey(VIEW_SESSION_SUBKEY));

        if (views != null) {
            if (!views.containsKey(namespace)) {
                throw new PortletException("view not initialized for namespace " + namespace);
            }

            C viewInstance = views.get(namespace);
            /*
             * Note: mode update events must be sent to handlers before
             * window state update events.
             */
            Map<String, String> portletMode = (Map<String, String>) session.getAttribute(getViewMapSessionKey(MODE_SESSION_SUBKEY));
            String oldMode = portletMode.getOrDefault(namespace,
                    PortletMode.UNDEFINED.toString());
            String newMode = request.getPortletMode().toString();
            if (!oldMode.equals(newMode)
                    && isViewInstanceOf(PortletModeHandler.class)) {
                portletMode.put(namespace, newMode);
                fireModeChange((PortletModeHandler) viewInstance,
                        new PortletModeEvent(new PortletMode(newMode),
                                new PortletMode(oldMode)));
            }

            Map<String, String> windowState = (Map<String, String>) session.getAttribute(getViewMapSessionKey(WINDOWSTATE_SESSION_SUBKEY));
            String oldWindowState = windowState.getOrDefault(namespace,
                    WindowState.UNDEFINED.toString());
            String newWindowState = request.getWindowState().toString();
            if (!oldWindowState.equals(newWindowState)
                    && isViewInstanceOf(WindowStateHandler.class)) {
                windowState.put(namespace, newWindowState);
                fireWindowStateChange(
                        (WindowStateHandler) viewInstance,
                        new WindowStateEvent(new WindowState(newWindowState),
                                new WindowState(oldWindowState)));
            }
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
     *         The original PortletRequest
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
        if (!isPortlet3 && request.getActionParameters().getNames()
                .contains(ACTION_STATE)) {
            WindowState windowState = new WindowState(
                    request.getActionParameters().getValue(ACTION_STATE));
            PortletMode portletMode = new PortletMode(
                    request.getActionParameters().getValue(ACTION_MODE));

            response.setWindowState(windowState);
            response.setPortletMode(portletMode);
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
     * <code>null</code>
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

    public void setWebComponentProviderURL(String namespace, String url) {
        webComponentProviderURL.put(namespace, url);
    }

    public String getWebComponentProviderURL(String namespace) {
        return webComponentProviderURL.get(namespace);
    }

    public void setWebComponentBootstrapHandlerURL(String namespace, String url) {
        webComponentBootstrapHandlerURL.put(namespace, url);
    }

    public String getWebComponentBootstrapHandlerURL(String namespace) {
        return webComponentBootstrapHandlerURL.get(namespace);
    }

    public void setWebComponentUIDLRequestHandlerURL(String namespace, String url) {
        webComponentUIDLRequestHandlerURL.put(namespace, url);
    }

    public String getWebComponentUIDLRequestHandlerURL(String namespace) {
        return webComponentUIDLRequestHandlerURL.get(namespace);
    }

    /**
     * Get the window state for this portlet.
     *
     * @return window state
     */
    public WindowState getWindowState() {
        return VaadinPortletRequest.getCurrent().getWindowState();
    }

    /**
     * Get the portlet mode for this portlet.
     *
     * @return portlet mode
     */
    public PortletMode getPortletMode() {
        return VaadinPortletRequest.getCurrent().getPortletMode();
    }

    /**
     * Set a new window state for this portlet
     *
     * @param newWindowState
     *         window state to set
     */
    public void setWindowState(WindowState newWindowState) {
        if (isPortlet3) {
            PortletHubUtil
                    .updatePortletState(newWindowState.toString(),
                            getPortletMode().toString());
        } else if (actionURL.containsKey(getNamespace())) {
            stateChangeAction(newWindowState.toString(),
                    getPortletMode().toString());
        }
    }

    /**
     * Set a new portlet mode for this portlet.
     *
     * @param newPortletMode
     *         portlet mode to set
     */
    public void setPortletMode(PortletMode newPortletMode) {
        if (isPortlet3) {
            PortletHubUtil
                    .updatePortletState(getWindowState().toString(),
                            newPortletMode.toString());
        } else if (actionURL.containsKey(getNamespace())) {
            stateChangeAction(getWindowState().toString(),
                    newPortletMode.toString());
        }
    }

    private void stateChangeAction(String state, String mode) {
        String namespace = getNamespace();
        if (actionURL.containsKey(namespace)) {
            String stateChangeScript = String
                    .format("location.href = '%s?%s=%s&%s=%s'", actionURL.get(namespace),
                            ACTION_STATE, state, ACTION_MODE, mode);

            UI.getCurrent().getPage().executeJs(stateChangeScript);
        }
    }

    private String getNamespace() {
        return VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    private String getViewMapSessionKey(String subKey) {
        return getClass().getName() + "-" + subKey;
    }
}

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
import javax.portlet.ActionURL;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.ExportsWebComponent;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.handler.EventHandler;
import com.vaadin.flow.portal.handler.PortletEvent;
import com.vaadin.flow.portal.handler.PortletEventListener;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.PortletModeListener;
import com.vaadin.flow.portal.handler.VaadinPortletEventContext;
import com.vaadin.flow.portal.handler.VaadinPortletEventView;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;
import com.vaadin.flow.portal.handler.WindowStateListener;
import com.vaadin.flow.portal.util.PortletHubUtil;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.shared.util.SharedUtil;

/**
 * Vaadin implementation of the {@link GenericPortlet}.
 *
 * @since
 */
@PreserveOnRefresh
public abstract class VaadinPortlet<C extends Component> extends GenericPortlet
        implements ExportsWebComponent<C> {

    private static final String VAADIN_EVENT = "vaadin.ev";
    private static final String VAADIN_UID = "vaadin.uid";
    private static final String ACTION_STATE = "state";
    private static final String ACTION_MODE = "mode";

    private VaadinPortletService vaadinService;

    private boolean isPortlet3 = false;

    // @formatter:off
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
     * ╟────────────┼───────────────────────┼────────────╢
     * ║ ┌──────────┼──────────┐ ┌──────────┼──────────┐ ║
     * ║ │ MyPortlet│(L)       │ │ MyPortlet│(R)       │ ║
     * ║ ├──────────┼──────────┤ ├──────────┼──────────┤ ║
     * ║ │ ┌────────┼────────┐ │ │ ┌────────┼────────┐ │ ║
     * ║ │ │ View(1)│        │ │ │ │ View(2)│        │ │ ║
     * ║ │ └────────┼────────┘ │ │ └────────┼────────┘ │ ║
     * ║ └──────────┼──────────┘ └──────────┼──────────┘ ║
     * ╚════════════╪═══════════════════════╪════════════╝
     *              │                       │
     * ╔════════════╪═══════════════════════╪════════════╗
     * ║ Window(2)  │                       │            ║
     * ╟────────────┼───────────────────────┼────────────╢
     * ║ ┌──────────┼──────────┐ ┌──────────┼──────────┐ ║
     * ║ │ MyPortlet│(L)       │ │ MyPortlet│(R)       │ ║
     * ║ ├──────────┼──────────┤ ├──────────┼──────────┤ ║
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
    // @formatter:on
    private static final String VIEW_CONTEXT_SESSION_SUBKEY = "viewContext";
    private static final String PORTLET_MODE_SESSION_SUBKEY = "mode";
    private static final String WINDOW_STATE_SESSION_SUBKEY = "windowState";

    private static final String WEB_COMPONENT_PROVIDER_URL_SUBKEY = "webComponentProviderURL";
    private static final String WEB_COMPONENT_BOOTSTRAP_HANDLER_URL_SUBKEY = "webComponentBootstrapHandlerURL";
    private static final String WEB_COMPONENT_UIDL_REQUEST_HANDLER_URL_SUBKEY = "v";

    private static class VaadinPortletEventContextImpl<C extends Component>
            implements VaadinPortletEventContext {

        private final C view;

        private final AtomicLong nextUid = new AtomicLong();

        private final Map<String, PortletEventListener> listeners = new HashMap<String, PortletEventListener>();

        private final Collection<WindowStateListener> windowStateListeners = new CopyOnWriteArrayList<>();

        private final Collection<PortletModeListener> portletModeListeners = new CopyOnWriteArrayList<>();

        private VaadinPortletEventContextImpl(C view) {
            this.view = view;

            if (view instanceof EventHandler) {
                EventHandler handler = (EventHandler) view;
                addGenericEventListener(handler::handleEvent);
            }
            if (view instanceof WindowStateHandler) {
                addWindowStateListener(
                        ((WindowStateHandler) view)::windowStateChange);
            }
            if (view instanceof PortletModeHandler) {
                addPortletModeListener(
                        ((PortletModeListener) view)::portletModeChange);
            }
        }

        @Override
        public void fireEvent(String eventName,
                Map<String, String> parameters) {
            StringBuilder eventBuilder = new StringBuilder();
            eventBuilder.append(PortletHubUtil.getHubString());
            eventBuilder.append("var params = hub.newParameters();");
            eventBuilder.append("params['action'] = ['send'];");
            parameters.forEach((key, value) -> eventBuilder
                    .append(String.format("params['%s'] = ['%s'];", escape(key),
                            escape(value))));
            eventBuilder.append(
                    String.format("hub.dispatchClientEvent('%s', params);",
                            escape(eventName)));

            view.getElement().executeJs(eventBuilder.toString());
        }

        @Override
        public Registration addGenericEventListener(
                PortletEventListener listener) {
            return addEventListener(".*", listener);
        }

        @Override
        public Registration addEventListener(String eventType,
                PortletEventListener listener) {
            Objects.requireNonNull(listener);

            String namespace = VaadinPortletService.getCurrentResponse()
                    .getPortletResponse().getNamespace();

            String uid = Long.toString(nextUid.getAndIncrement());
            view.getElement().executeJs(
                    "window.Vaadin.Flow.Portlets[$0].registerListener($1, $2);",
                    namespace, eventType, uid);
            listeners.put(uid, listener);
            return () -> {
                listeners.remove(uid);
                view.getElement().executeJs(
                        "window.Vaadin.Flow.Portlets[$0].unregisterListener($1);",
                        namespace, uid);

            };
        }

        @Override
        public Registration addWindowStateListener(
                WindowStateListener listener) {
            windowStateListeners.add(Objects.requireNonNull(listener));
            return () -> windowStateListeners.remove(listener);
        }

        @Override
        public Registration addPortletModeListener(
                PortletModeListener listener) {
            portletModeListeners.add(Objects.requireNonNull(listener));
            return () -> portletModeListeners.remove(listener);
        }

        private String escape(String str) {
            return str.replaceAll("([\\\\'])", "\\\\$1");
        }

        private void fireWindowStateEvent(WindowStateEvent event) {
            windowStateListeners
                    .forEach(listener -> listener.windowStateChange(event));
        }

        private void firePortletModeEvent(PortletModeEvent event) {
            portletModeListeners
                    .forEach(listener -> listener.portletModeChange(event));
        }
    }

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
        assert VaadinSession.getCurrent().hasLock();

        SerializableRunnable runnable = () -> {
            VaadinPortletEventContextImpl<C> context = new VaadinPortletEventContextImpl<>(
                    component);
            initSessionAttributes(context);
            if (component instanceof VaadinPortletEventView) {
                VaadinPortletEventView view = (VaadinPortletEventView) component;
                view.onPortletEventContextInit(context);
            }
        };
        if (component.getElement().getNode().isAttached()) {
            runnable.run();
        }
        component.getElement().addAttachListener(event -> runnable.run());
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
        VaadinPortletSession session = getSession(request, response);
        if (session == null) {
            return;
        }
        // Only if a session exists, check for mode and window state change
        String namespace = response.getNamespace();
        session.lock();
        try {
            VaadinPortletEventContextImpl<C> context = getViewContext(session,
                    namespace);
            if (context != null) {
                /*
                 * Note: mode update events must be sent to handlers before
                 * window state update events.
                 */
                updateMapAndCallHandlerIfChanged(session,
                        PORTLET_MODE_SESSION_SUBKEY, namespace,
                        request.getPortletMode().toString(),
                        PortletMode.UNDEFINED.toString(),
                        (oldMode, newMode) -> context.firePortletModeEvent(
                                new PortletModeEvent(new PortletMode(newMode),
                                        new PortletMode(oldMode))));

                updateMapAndCallHandlerIfChanged(session,
                        WINDOW_STATE_SESSION_SUBKEY, namespace,
                        request.getWindowState().toString(),
                        WindowState.UNDEFINED.toString(),
                        (oldState, newState) -> context.fireWindowStateEvent(
                                new WindowStateEvent(new WindowState(newState),
                                        new WindowState(oldState))));
            }
        } finally {
            session.unlock();
        }
    }

    private void updateMapAndCallHandlerIfChanged(VaadinPortletSession session,
            String sessionSubkey, String namespace, String newValue,
            String defaultNewValue,
            BiConsumer<String, String> valueChangeHandler) {
        Map<String, String> map = (Map<String, String>) session
                .getAttribute(getSessionAttributeKey(sessionSubkey));
        if (map == null) {
            map = new HashMap<>();
        }
        String oldValue = map.getOrDefault(namespace, defaultNewValue);
        if (!newValue.equals(oldValue)) {
            valueChangeHandler.accept(oldValue, newValue);
        }
        map.put(namespace, newValue);
        session.setAttribute(getSessionAttributeKey(sessionSubkey), map);
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
        VaadinPortletEventContextImpl<C> viewContext = getViewContext(
                VaadinPortletSession.getCurrent(), response.getNamespace());

        if (!isPortlet3 && names.contains(ACTION_STATE)) {
            WindowState windowState = new WindowState(
                    request.getActionParameters().getValue(ACTION_STATE));
            PortletMode portletMode = new PortletMode(
                    request.getActionParameters().getValue(ACTION_MODE));

            response.setWindowState(windowState);
            response.setPortletMode(portletMode);
        } else if (names.contains(VAADIN_EVENT)) {
            String event = request.getActionParameters().getValue(VAADIN_EVENT);
            String uid = request.getActionParameters().getValue(VAADIN_UID);
            Map<String, String[]> map = new HashMap<>(
                    request.getParameterMap());
            map.remove(VAADIN_EVENT);
            map.remove(VAADIN_UID);

            assert viewContext.view.getElement().getNode().isAttached();
            VaadinSession session = viewContext.view.getUI().get().getSession();
            session.access(() -> {
                PortletEventListener listener = viewContext.listeners.get(uid);
                if (listener == null) {
                    LoggerFactory.getLogger(VaadinPortlet.class).error(
                            "{} is not found for the uid='{}', event '{}' is not delivered",
                            PortletEventListener.class.getSimpleName(), uid,
                            event);
                } else {
                    listener.onPortletEvent(new PortletEvent(event, map));
                }
            });
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

    public void setWebComponentProviderURL(VaadinSession session,
            String namespace, String url) {
        session.checkHasLock();
        setSessionAttribute(session, namespace,
                WEB_COMPONENT_PROVIDER_URL_SUBKEY, url);
    }

    public String getWebComponentProviderURL(VaadinSession session,
            String namespace) {
        session.checkHasLock();
        return getSessionAttribute(session, namespace,
                WEB_COMPONENT_PROVIDER_URL_SUBKEY);
    }

    public void setWebComponentBootstrapHandlerURL(VaadinSession session,
            String namespace, String url) {
        session.checkHasLock();
        setSessionAttribute(session, namespace,
                WEB_COMPONENT_BOOTSTRAP_HANDLER_URL_SUBKEY, url);
    }

    public String getWebComponentBootstrapHandlerURL(VaadinSession session,
            String namespace) {
        session.checkHasLock();
        return getSessionAttribute(session, namespace,
                WEB_COMPONENT_BOOTSTRAP_HANDLER_URL_SUBKEY);
    }

    public void setWebComponentUIDLRequestHandlerURL(VaadinSession session,
            String namespace, String url) {
        session.checkHasLock();
        setSessionAttribute(session, namespace,
                WEB_COMPONENT_UIDL_REQUEST_HANDLER_URL_SUBKEY, url);
    }

    public String getWebComponentUIDLRequestHandlerURL(VaadinSession session,
            String namespace) {
        session.checkHasLock();
        return getSessionAttribute(session, namespace,
                WEB_COMPONENT_UIDL_REQUEST_HANDLER_URL_SUBKEY);
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
     *            window state to set
     */
    public void setWindowState(WindowState newWindowState) {
        if (isPortlet3) {
            PortletHubUtil.updatePortletState(newWindowState.toString(),
                    getPortletMode().toString());
        } else {
            stateChangeAction(newWindowState, getPortletMode());
        }
        setSessionWindowState(
                VaadinPortletSession.getCurrent(), VaadinPortletResponse
                        .getCurrentPortletResponse().getNamespace(),
                newWindowState);
    }

    /**
     * Set a new portlet mode for this portlet.
     *
     * @param newPortletMode
     *            portlet mode to set
     */
    public void setPortletMode(PortletMode newPortletMode) {
        if (isPortlet3) {
            PortletHubUtil.updatePortletState(getWindowState().toString(),
                    newPortletMode.toString());
        } else {
            stateChangeAction(getWindowState(), newPortletMode);
        }
        setSessionPortletMode(
                VaadinPortletSession.getCurrent(), VaadinPortletResponse
                        .getCurrentPortletResponse().getNamespace(),
                newPortletMode);
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

    private void stateChangeAction(WindowState state, PortletMode mode) {
        PortletResponse response = VaadinPortletResponse
                .getCurrentPortletResponse();
        if (response instanceof MimeResponse) {
            ActionURL actionURL = ((MimeResponse) response).createActionURL();
            try {
                actionURL.setPortletMode(mode);
            } catch (PortletModeException e) {
                getLogger().error("unable to create portlet mode action URL",
                        e);
            }
            try {
                actionURL.setWindowState(state);
            } catch (WindowStateException e) {
                getLogger().error("unable to create window state action URL",
                        e);
            }
            String stateChangeScript = String.format("location.href = '%s'",
                    actionURL);
            UI.getCurrent().getPage().executeJs(stateChangeScript);
        }
    }

    private VaadinPortletSession getSession(PortletRequest request,
            PortletResponse response) {
        try {
            VaadinPortletSession session = (VaadinPortletSession) getService()
                    .findVaadinSession(createVaadinRequest(request));
            if (session == null) {
                getLogger().error("Could not find session for portlet "
                        + getPortletName() + " @ " + response.getNamespace());
            }
            return session;
        } catch (SessionExpiredException e) {
            getLogger().error("Session expired", e);
            return null;
        }
    }

    private VaadinPortletEventContextImpl<C> getViewContext(
            VaadinPortletSession session, String namespace)
            throws PortletException {
        Map<String, VaadinPortletEventContextImpl<C>> viewContexts = (Map<String, VaadinPortletEventContextImpl<C>>) session
                .getAttribute(
                        getSessionAttributeKey(VIEW_CONTEXT_SESSION_SUBKEY));
        if (viewContexts != null) {
            if (!viewContexts.containsKey(namespace)) {
                throw new PortletException(
                        "view not initialized for namespace " + namespace);
            }
            return viewContexts.get(namespace);
        } else {
            return null;
        }
    }

    private void setSessionPortletMode(VaadinSession session, String namespace,
            PortletMode newPortletMode) {
        setSessionAttribute(session, namespace, PORTLET_MODE_SESSION_SUBKEY,
                newPortletMode.toString());
    }

    private void setSessionWindowState(VaadinSession session, String namespace,
            WindowState newWindowState) {
        setSessionAttribute(session, namespace, WINDOW_STATE_SESSION_SUBKEY,
                newWindowState.toString());
    }

    private <T> void setSessionAttribute(VaadinSession session,
            String namespace, String subKey, T value) {
        Map<String, T> map = (Map<String, T>) session
                .getAttribute(getSessionAttributeKey(subKey));
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(namespace, value);
        session.setAttribute(getSessionAttributeKey(subKey), map);
    }

    private <T> T getSessionAttribute(VaadinSession session, String namespace,
            String subKey) {
        Map<String, T> map = (Map<String, T>) session
                .getAttribute(getSessionAttributeKey(subKey));
        if (map != null) {
            return map.get(namespace);
        } else {
            return null;
        }
    }

    private String getSessionAttributeKey(String subKey) {
        return getClass().getName() + "-" + subKey;
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(VaadinPortlet.class);
    }

    private void initSessionAttributes(VaadinPortletEventContext context) {
        if (VaadinPortlet.getCurrent() != null) {
            // Cannot use 'this' as it is only a temporary object created by
            // WebComponentExporter handling logic
            VaadinPortlet<C> thisPortlet = VaadinPortlet.getCurrent();
            PortletRequest request = VaadinPortletRequest
                    .getCurrentPortletRequest();
            PortletResponse response = VaadinPortletResponse
                    .getCurrentPortletResponse();

            VaadinSession session = thisPortlet.getSession(request, response);
            assert (session != null);
            session.checkHasLock();

            // Initialize maps for view component, mode and window state
            String namespace = response.getNamespace();
            setSessionAttribute(session, namespace, VIEW_CONTEXT_SESSION_SUBKEY,
                    context);
            setSessionPortletMode(session, namespace, PortletMode.UNDEFINED);
            setSessionWindowState(session, namespace, WindowState.UNDEFINED);
        }
    }
}

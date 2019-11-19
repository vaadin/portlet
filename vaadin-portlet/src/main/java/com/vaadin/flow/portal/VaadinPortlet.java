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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

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
import com.vaadin.flow.portal.handler.PortletEvent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.DeploymentConfigurationFactory;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinConfigurationException;
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

    private static final String VAADIN_EVENT = "vaadin.ev";
    private static final String VAADIN_UID = "vaadin.uid";
    private static final String VAADIN_WINDOW_NAME = "vaadin.wn";
    private static final String ACTION_STATE = "state";
    private static final String ACTION_MODE = "mode";
    private static final String DEV_MODE_ERROR_MESSAGE = "<h2>⚠️Vaadin Portlet does not work in development mode running webpack-dev-server</h2>"
            + "<p>To run a portlet in development mode, you need to activate both <code>prepare-frontend</code> and <code>build-frontend</code> goals of <code>vaadin-maven-plugin</code>. "
            + "To run a portlet in production mode see <a href='https://vaadin.com/docs/v14/flow/production/tutorial-production-mode-basic.html' target='_blank'>this</a>.</p>";

    private VaadinPortletService vaadinService;

    private AtomicBoolean isPortlet3 = new AtomicBoolean();

    // @formatter:off
    /*
     * The session currently stores a number of maps with the following keys:
     *
     * "<portlet class name>-<window name>-viewContext": namespace to view context
     * "<portlet class name>-<window name>-mode": namespace to portlet mode
     * "<portlet class name>-<window name>-windowState": namespace to window state
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

    private static final String WEB_COMPONENT_PROVIDER_URL_SUBKEY = "webComponentProviderURL";
    private static final String WEB_COMPONENT_BOOTSTRAP_HANDLER_URL_SUBKEY = "webComponentBootstrapHandlerURL";
    private static final String WEB_COMPONENT_UIDL_REQUEST_HANDLER_URL_SUBKEY = "webComponentUidlRequestHandlerURL";

    @Override
    public void init(PortletConfig config) throws PortletException {
        CurrentInstance.clearAll();
        super.init(config);

        DeploymentConfiguration deploymentConfiguration = createDeploymentConfiguration(
                config);
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

        SerializableRunnable runnable = () -> initComponent(component);
        if (component.getElement().getNode().isAttached()) {
            runnable.run();
        }
        component.getElement().addAttachListener(event -> runnable.run());
    }

    private void initComponent(C component) {
        // We rely on the component being attached and the window name
        // having been retrieved here---this is due to the
        // implementation of @PreserveOnRefresh
        UI ui = component.getUI().orElseThrow(() -> new IllegalStateException(
                "Unable to initialize component, UI instance not available from "
                        + component.getClass().getName()));

        String windowName = ui.getInternals().getExtendedClientDetails()
                .getWindowName();
        String namespace = VaadinPortletResponse.getCurrentPortletResponse()
                .getNamespace();
        VaadinSession session = ui.getSession();
        PortletViewContext<C> context;
        try {
            context = getViewContext(session, namespace, windowName);
        } catch (PortletException exception) {
            throw new RuntimeException("Unable to initialize component, "
                    + "PortletException raised", exception);
        }
        PortletRequest request = VaadinPortletRequest
                .getCurrentPortletRequest();
        boolean needViewInit = false;
        if (context == null) {
            needViewInit = true;
            context = new PortletViewContext<>(component, isPortlet3,
                    request.getPortletMode(), request.getWindowState());
            setViewContext(session, namespace, windowName, context);
        }
        context.init();
        context.updateModeAndState(request.getPortletMode(),
                request.getWindowState());
        if (needViewInit && component instanceof PortletView) {
            PortletView view = (PortletView) component;
            view.onPortletViewContextInit(context);
        }
    }

    protected DeploymentConfiguration createDeploymentConfiguration(
            PortletConfig config) throws PortletException {
        try {
            return DeploymentConfigurationFactory.createDeploymentConfiguration(
                    getClass(), new VaadinPortletConfig(config));
        } catch (VaadinConfigurationException e) {
            throw new PortletException(
                    "Failed to construct DeploymentConfiguration.", e);
        }
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
        isPortlet3.set(true);
        response.addDependency("PortletHub", "javax.portlet", "3.0.0");
    }

    @Override
    protected void doDispatch(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        try {
            if (getService().getDeploymentConfiguration().enableDevServer()) {
                response.getWriter().println(DEV_MODE_ERROR_MESSAGE);
                return;
            }

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

        if (!isPortlet3.get() && names.contains(ACTION_STATE)) {
            WindowState windowState = new WindowState(
                    request.getActionParameters().getValue(ACTION_STATE));
            PortletMode portletMode = new PortletMode(
                    request.getActionParameters().getValue(ACTION_MODE));

            response.setWindowState(windowState);
            response.setPortletMode(portletMode);
        } else if (names.contains(VAADIN_EVENT)) {
            String event = request.getActionParameters().getValue(VAADIN_EVENT);
            String uid = request.getActionParameters().getValue(VAADIN_UID);
            String windowName = request.getActionParameters()
                    .getValue(VAADIN_WINDOW_NAME);
            Map<String, String[]> map = new HashMap<>(
                    request.getParameterMap());
            map.remove(VAADIN_EVENT);
            map.remove(VAADIN_UID);
            map.remove(VAADIN_WINDOW_NAME);

            VaadinPortletSession session = getSession(request, response);
            if (session == null) {
                getLogger().debug("Unable to retrieve session, cannot " +
                        "fire event '{}'", event);
                return;
            }

            PortletViewContext<C> viewContext = getViewContext(session,
                    response.getNamespace(), windowName);
            if (viewContext != null) {
                session.access(() -> viewContext.firePortletEvent(uid,
                        new PortletEvent(event, map)));
            } else {
                getLogger().debug("Unable to retrieve view context, cannot " +
                        "fire event '{}'", event);
            }
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
    public static VaadinPortlet<?> getCurrent() {
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

    void setWebComponentProviderURL(VaadinSession session,
            String namespace, String url) {
        session.checkHasLock();
        setSessionAttribute(session, namespace,
                WEB_COMPONENT_PROVIDER_URL_SUBKEY, url);
    }

    String getWebComponentProviderURL(VaadinSession session,
            String namespace) {
        session.checkHasLock();
        return getSessionAttribute(session, namespace,
                WEB_COMPONENT_PROVIDER_URL_SUBKEY);
    }

    void setWebComponentBootstrapHandlerURL(VaadinSession session,
            String namespace, String url) {
        session.checkHasLock();
        setSessionAttribute(session, namespace,
                WEB_COMPONENT_BOOTSTRAP_HANDLER_URL_SUBKEY, url);
    }

    String getWebComponentBootstrapHandlerURL(VaadinSession session,
            String namespace) {
        session.checkHasLock();
        return getSessionAttribute(session, namespace,
                WEB_COMPONENT_BOOTSTRAP_HANDLER_URL_SUBKEY);
    }

    void setWebComponentUIDLRequestHandlerURL(VaadinSession session,
            String namespace, String url) {
        session.checkHasLock();
        setSessionAttribute(session, namespace,
                WEB_COMPONENT_UIDL_REQUEST_HANDLER_URL_SUBKEY, url);
    }

    String getWebComponentUIDLRequestHandlerURL(VaadinSession session,
            String namespace) {
        session.checkHasLock();
        return getSessionAttribute(session, namespace,
                WEB_COMPONENT_UIDL_REQUEST_HANDLER_URL_SUBKEY);
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

    private PortletViewContext<C> getViewContext(VaadinSession session,
                                                 String namespace, String windowName) throws PortletException {
        Map<String, PortletViewContext<C>> viewContexts = (Map<String, PortletViewContext<C>>) session
                .getAttribute(getSessionWindowAttributeKey(windowName,
                        VIEW_CONTEXT_SESSION_SUBKEY));
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

    private void setViewContext(VaadinSession session, String namespace,
            String windowName, PortletViewContext<C> context) {
        setSessionAttribute(session, namespace,
                windowName + "-" + VIEW_CONTEXT_SESSION_SUBKEY, context);
    }

    private <T> void setSessionAttribute(VaadinSession session,
            String namespace, String subKey, T value) {
        Map<String, T> map = (Map<String, T>) session
                .getAttribute(getSessionAttributeKey(subKey));
        if (map == null) {
            map = new HashMap<>();
            session.setAttribute(getSessionAttributeKey(subKey), map);
        }
        map.put(namespace, value);
    }

    private <T> T getSessionAttribute(VaadinSession session, String namespace,
            String subKey) {
        Map<String, T> map = (Map<String, T>) session
                .getAttribute(getSessionAttributeKey(subKey));
        return map != null ? map.get(namespace) : null;
    }

    private String getSessionAttributeKey(String subKey) {
        return getClass().getName() + "-" + subKey;
    }

    private String getSessionWindowAttributeKey(String windowName,
            String subKey) {
        return getSessionAttributeKey(windowName + "-" + subKey);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(VaadinPortlet.class);
    }

}

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
import javax.portlet.PortalContext;
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
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.internal.ExportsWebComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.util.SharedUtil;

//import com.vaadin.flow.portal.impl.VaadinGateInRequest;
//import com.vaadin.flow.portal.impl.VaadinLiferayRequest;
//import com.vaadin.flow.portal.impl.VaadinWebLogicPortalRequest;
//import com.vaadin.flow.portal.impl.VaadinWebSpherePortalRequest;

/**
 * Vaadin implementation of the {@link GenericPortlet}.
 *
 * @since
 */
@PreserveOnRefresh
public abstract class VaadinPortlet<C extends Component> extends GenericPortlet
         implements ExportsWebComponent<C> {

    private VaadinPortletService vaadinService;
    private String webComponentProviderURL;
    private String webComponentBootstrapHandlerURL;
    private String webComponentUIDLRequestHandlerURL;

    // As a crutch, all events for now target the last instantiated view.
    // TODO: Create a portlet-instance mapping (#45), to be used when
    // dispatching the event
    private C viewInstance = null;
    private PortletMode mode = PortletMode.UNDEFINED;

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
    public void configure(WebComponent<C> webComponent, C component) {
        // can't use this as it is a temporary object created by
        // WebComponentEporter handling logic
        if (VaadinPortlet.getCurrent() != null) {
            VaadinPortlet.getCurrent().viewInstance = component;
        }
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);

        PortletMode oldMode = mode;
        mode = request.getPortletMode();
        if (!oldMode.equals(mode) && isViewInstanceOf(
                PortletModeHandler.class)) {
            fireModeChange(new PortletModeEvent(mode));
            // How do we get the Component??
            // This would probably need to fire a push or generate a UIDL request!
        }
    }

    protected void fireModeChange(PortletModeEvent event) {
        if (viewInstance!=null) {
            ((PortletModeHandler)viewInstance).portletModeChange(event);
        }
    }

    protected boolean isViewInstanceOf(Class<?> instance) {
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
        PortalContext portalContext = request.getPortalContext();
        String portalInfo = portalContext.getPortalInfo()
                .toLowerCase(Locale.ROOT).trim();
        VaadinPortletService service = getService();
        //
        //        if (portalInfo.contains("gatein")) {
        //            return new VaadinGateInRequest(request, service);
        //        }
        //
        //        if (portalInfo.contains("liferay")) {
        //            return new VaadinLiferayRequest(request, service);
        //        }
        //
        //        if (portalInfo.contains("websphere portal")) {
        //            return new VaadinWebSpherePortalRequest(request, service);
        //        }
        //        if (portalInfo.contains("weblogic portal")) {
        //            return new VaadinWebLogicPortalRequest(request, service);
        //        }

        return new VaadinPortletRequest(request, service);
    }

    private VaadinPortletResponse createVaadinResponse(
            PortletResponse response) {
        return new VaadinPortletResponse(response, getService());
    }

    @Override
    public void serveResource(ResourceRequest request,
            ResourceResponse response) throws PortletException, IOException {
        handleRequest(request, response);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        handleRequest(request, response);
    }

    @Override
    public void processEvent(EventRequest request, EventResponse response)
            throws PortletException, IOException {
        handleRequest(request, response);
    }

    protected void handleRequest(PortletRequest request,
            PortletResponse response) throws PortletException, IOException {

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

}

/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal;

import javax.portlet.EventRequest;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.PwaRegistry;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.Version;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.server.communication.HeartbeatHandler;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.communication.UidlRequestHandler;
import com.vaadin.flow.server.startup.PortletApplicationRouteRegistryUtil;
import com.vaadin.flow.shared.Registration;

/**
 * An implementation of {@link com.vaadin.flow.server.VaadinService} for JSR-362
 * portlet environments.
 *
 * @author Vaadin Ltd
 * @since
 */
public class VaadinPortletService extends VaadinService {
    static final String PROJECT_NAME = "vaadin-portlet";

    private static final String VERSION_PROPERTIES_NAME = "version.properties";
    private static final String PORTLET_VERSION_PROPERTY = "portlet.version";

    private static final Properties PROPERTIES_FILE = loadPropertiesFile();

    private static final String SERVLET_RESOURCES_SERVER_MESSAGE = "Servlet resources are loaded and served by the server.";

    private final VaadinPortlet portlet;

    private final ErrorHandler DEFAULT_HANDLER = new DefaultPortletErrorHandler(
            this);

    public VaadinPortletService(VaadinPortlet portlet,
            DeploymentConfiguration deploymentConfiguration) {
        super(deploymentConfiguration);
        this.portlet = portlet;
        verifyLicense(deploymentConfiguration.isProductionMode());

        Registration registration = addSessionInitListener(
                event -> event.getSession().setErrorHandler(DEFAULT_HANDLER));
        addServiceDestroyListener(event -> registration.remove());
    }

    private void verifyLicense(boolean productionMode) {
        if (!productionMode) {
            String portletVersion = getPortletVersion();

            UsageStatistics.markAsUsed(PROJECT_NAME, portletVersion);
            UsageStatistics.markAsUsed("vaadin", Version.getFullVersion());
        }
    }

    static String getPortletVersion() {
        String portletVersion = PROPERTIES_FILE
                .getProperty(PORTLET_VERSION_PROPERTY);
        if (portletVersion == null || portletVersion.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "Unable to determine Portlet version: had successfully "
                            + "loaded the resource file '%s' but failed to "
                            + "find property '%s' in it. Double check jar "
                            + "package integrity.",
                    VERSION_PROPERTIES_NAME, PORTLET_VERSION_PROPERTY));
        }
        return portletVersion;
    }

    @Override
    protected List<RequestHandler> createRequestHandlers()
            throws ServiceException {
        List<RequestHandler> handlers = super.createRequestHandlers();
        handlers.add(new PortletBootstrapHandler());
        handlers.add(new PortletWebComponentProvider());
        handlers.add(new PortletWebComponentBootstrapHandler());

        handlers.removeIf(
                requestHandler -> requestHandler instanceof UidlRequestHandler);
        handlers.add(new PortletUidlRequestHandler());

        handlers.removeIf(
                requestHandler -> requestHandler instanceof StreamRequestHandler);
        handlers.add(new PortletStreamRequestHandler());

        // HeartbeatHandler should have a higher priority because otherwise,
        // heartbeat requests are handled by PortletUidlRequestHandler or
        // PortletBootstrapHandler which causes Vaadin portlets stop responding.
        // See https://github.com/vaadin/portlet/issues/166
        handlers.removeIf(
                requestHandler -> requestHandler instanceof HeartbeatHandler);
        handlers.add(new HeartbeatHandler());
        return handlers;
    }

    /**
     * Retrieves a reference to the portlet associated with this service.
     *
     * @return A reference to the VaadinPortlet this service is using
     */
    public VaadinPortlet getPortlet() {
        return portlet;
    }

    private PortletContext getPortletContext() {
        return getPortlet().getPortletContext();
    }

    @Override
    public String getMimeType(String resourceName) {
        return getPortletContext().getMimeType(resourceName);
    }

    @Override
    protected boolean requestCanCreateSession(VaadinRequest request) {
        if (!(request instanceof VaadinPortletRequest)) {
            throw new IllegalArgumentException(
                    "Request is not a VaadinPortletRequest");
        }

        PortletRequest portletRequest = ((VaadinPortletRequest) request)
                .getPortletRequest();
        if (portletRequest instanceof RenderRequest) {
            // In most cases the first request is a render request that
            // renders the HTML fragment. This should create a Vaadin
            // session unless there is already one.
            return true;
        } else if (portletRequest instanceof EventRequest) {
            // A portlet can also be sent an event even though it has not
            // been rendered, e.g. portlet on one page sends an event to a
            // portlet on another page and then moves the user to that page.
            return true;
        }
        return false;
    }

    /**
     * Gets the currently processed portlet request. The current portlet request
     * is automatically defined when the request is started. The current portlet
     * request can not be used in e.g. background threads because of the way
     * server implementations reuse request instances.
     *
     * @return the current portlet request instance if available, otherwise
     *         <code>null</code>
     */
    public static PortletRequest getCurrentPortletRequest() {
        VaadinPortletRequest currentRequest = getCurrentRequest();
        if (currentRequest != null) {
            return currentRequest.getPortletRequest();
        } else {
            return null;
        }
    }

    /**
     * Gets the currently processed Vaadin portlet request. The current request
     * is automatically defined when the request is started. The current request
     * can not be used in e.g. background threads because of the way server
     * implementations reuse request instances.
     *
     * @return the current Vaadin portlet request instance if available,
     *         otherwise <code>null</code>
     */
    public static VaadinPortletRequest getCurrentRequest() {
        return (VaadinPortletRequest) VaadinService.getCurrentRequest();
    }

    /**
     * Gets the currently processed portlet response. The current portlet
     * response is automatically defined when the request is started. The
     * current portlet response can not be used in e.g. background threads
     * because of the way server implementations reuse response instances.
     *
     * @return the current portlet response instance if available, otherwise
     *         <code>null</code>
     */
    public static PortletResponse getCurrentPortletResponse() {
        VaadinPortletResponse currentRequest = getCurrentResponse();
        if (currentRequest != null) {
            return currentRequest.getPortletResponse();
        } else {
            return null;
        }
    }

    /**
     * Gets the currently processed Vaadin portlet response. The current
     * response is automatically defined when the request is started. The
     * current response can not be used in e.g. background threads because of
     * the way server implementations reuse response instances.
     *
     * @return the current Vaadin portlet response instance if available,
     *         otherwise <code>null</code>
     */
    public static VaadinPortletResponse getCurrentResponse() {
        return (VaadinPortletResponse) VaadinService.getCurrentResponse();
    }

    @Override
    protected VaadinSession createVaadinSession(VaadinRequest request) {
        return new VaadinPortletSession(this);
    }

    @Override
    public String getServiceName() {
        return getPortlet().getPortletName();
    }

    @Override
    protected void handleSessionExpired(VaadinRequest request,
            VaadinResponse response) {
        // TODO Figure out a better way to deal with
        // SessionExpiredExceptions
        getLogger().debug("A user session has expired");
    }

    private WrappedPortletSession getWrappedPortletSession(
            WrappedSession wrappedSession) {
        return (WrappedPortletSession) wrappedSession;
    }

    @Override
    protected void writeToHttpSession(WrappedSession wrappedSession,
            VaadinSession session) {
        getWrappedPortletSession(wrappedSession).setAttribute(
                getSessionAttributeName(), session,
                PortletSession.PORTLET_SCOPE);
    }

    @Override
    protected VaadinSession readFromHttpSession(WrappedSession wrappedSession) {
        return (VaadinSession) getWrappedPortletSession(wrappedSession)
                .getAttribute(getSessionAttributeName(),
                        PortletSession.PORTLET_SCOPE);
    }

    @Override
    protected void removeFromHttpSession(WrappedSession wrappedSession) {
        getWrappedPortletSession(wrappedSession).removeAttribute(
                getSessionAttributeName(), PortletSession.PORTLET_SCOPE);
    }

    @Override
    protected RouteRegistry getRouteRegistry() {
        return PortletApplicationRouteRegistryUtil
                .getInstance((VaadinPortletContext) getContext());
    }

    @Override
    protected PwaRegistry getPwaRegistry() {
        getLogger().debug(
                "PWA is not supported for portlets. Returning null reference");
        return null;
    }

    @Override
    public String getContextRootRelativePath(VaadinRequest request) {
        return "/";
    }

    @Override
    public String getMainDivId(VaadinSession session, VaadinRequest request) {
        PortletRequest portletRequest = ((VaadinPortletRequest) request)
                .getPortletRequest();
        /*
         * We need to generate a unique ID because some portals already create a
         * DIV with the portlet's Window ID as the DOM ID.
         */
        return "v-" + portletRequest.getWindowID();
    }

    @Override
    public URL getStaticResource(String url) {
        getLogger()
                .debug("Static resources are loaded and served by the server.");
        return null;
    }

    @Override
    public URL getResource(String url) {
        getLogger().debug(SERVLET_RESOURCES_SERVER_MESSAGE);
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String url) {
        getLogger().debug(SERVLET_RESOURCES_SERVER_MESSAGE);
        return null;
    }

    @Override
    public String resolveResource(String url) {
        getLogger().debug(SERVLET_RESOURCES_SERVER_MESSAGE);
        return null;
    }

    @Override
    protected VaadinContext constructVaadinContext() {
        return new VaadinPortletContext(getPortlet().getPortletContext());
    }

    private static Properties loadPropertiesFile() {
        ClassLoader classLoader;
        VaadinPortletService currentService = (VaadinPortletService) VaadinPortletService
                .getCurrent();
        if (currentService != null) {
            classLoader = currentService.getClassLoader();
        } else {
            classLoader = VaadinPortletService.class.getClassLoader();
        }

        Properties properties = new Properties();
        try {
            properties.load(
                    classLoader.getResourceAsStream(VERSION_PROPERTIES_NAME));
        } catch (IOException e) {
            throw new UncheckedIOException(String.format(
                    "Failed to load the resource file '%s'. Double check jar package integrity.",
                    VERSION_PROPERTIES_NAME), e);
        }
        return properties;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VaadinPortletService.class);
    }

}

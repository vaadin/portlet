/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import jakarta.portlet.PortletRequest;
import jakarta.portlet.PortletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.Mode;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.WebComponentBootstrapHandler;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.pro.licensechecker.BuildType;
import com.vaadin.pro.licensechecker.LicenseChecker;

import tools.jackson.databind.node.ObjectNode;

/**
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletWebComponentBootstrapHandler
        extends WebComponentBootstrapHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(PortletWebComponentBootstrapHandler.class);

    /**
     * Cached web-component.html content fetched from the external static
     * resources bundle WAR. Once fetched, the content does not change for the
     * lifetime of the server (it is a production build artifact).
     */
    private static volatile String externalWebComponentHtmlCache;

    PortletWebComponentBootstrapHandler() {
        super(new PortletWebComponentPageBuilder());
    }

    @Override
    protected String getServiceUrl(VaadinRequest request,
            VaadinResponse response) {
        final String namespace = ((VaadinPortletResponse) response)
                .getPortletResponse().getNamespace();
        VaadinPortletSession session = VaadinPortletSession.getCurrent();
        return Objects.requireNonNull(VaadinPortlet.getCurrent())
                .getWebComponentUIDLRequestHandlerURL(session, namespace);
    }

    @Override
    protected String modifyPath(String basePath, String path) {
        // Require that the static files are available from the server root
        path = path.replaceFirst("^.VAADIN/", "./VAADIN/");
        if (path.startsWith("./VAADIN/") || isStaticResource(path)) {
            VaadinService vaadinService = VaadinPortletService.getCurrent();
            DeploymentConfiguration deploymentConfiguration =
                    vaadinService.getDeploymentConfiguration();
            Optional<DevModeHandler> devModeHandler =
                    DevModeHandlerManager.getDevModeHandler(vaadinService);
            if (deploymentConfiguration.isProductionMode()) {
                // In production mode serve static files from the
                // dedicated URI
                String prefix =
                        getStaticResourcesMappingURI(deploymentConfiguration);
                if (!path.startsWith("./")) {
                    // Theme resources like lumo/lumo.css need the
                    // prefix but don't start with ./
                    return prefix + "./" + path;
                }
                return prefix + path;
            } else if (devModeHandler.isPresent() && checkDevServerConnection(
                    devModeHandler.get())) {
                // With dev server running request directly from dev server
                return String.format("http://localhost:%s/%s",
                        devModeHandler.get().getPort(), path);
            }
            return "/" + path;
        }
        return super.modifyPath(basePath, path);
    }

    /**
     * Checks if the given path is a static resource that should be served
     * from the static resources WAR (e.g. theme stylesheets from
     * META-INF/resources/ in JARs).
     */
    private boolean isStaticResource(String path) {
        return path.endsWith(".css") || path.endsWith(".js");
    }

    @Override
    protected void writeBootstrapPage(String contentType,
            VaadinResponse response, Element head, String serviceUrl)
            throws IOException {
        String appId = UI.getCurrent().getInternals().getAppId();
        PortletResponse resp = ((VaadinPortletResponse) response)
                .getPortletResponse();
        String portletNs = resp.getNamespace();

        Element script = head.appendElement("script");
        script.attr("type", "text/javascript");
        script.appendText(String.format(
                "window.Vaadin.Flow.Portlets =window.Vaadin.Flow.Portlets||{};"
                        + "window.Vaadin.Flow.Portlets['%s']=window.Vaadin.Flow.Portlets['%s']||{};"
                        + "window.Vaadin.Flow.Portlets['%s'].appId='%s';",
                portletNs, portletNs, portletNs, appId));

        super.writeBootstrapPage(contentType, response, head, serviceUrl);
    }

    private String getStaticResourcesMappingURI(
            DeploymentConfiguration configuration) {
        String uri = configuration.getStringProperty(
                PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING,
                "/vaadin-portlet-static/");
        if (uri.isEmpty()) {
            return "/";
        }
        if (uri.charAt(0) == '/' && uri.charAt(uri.length() - 1) == '/') {
            return uri;
        }
        StringBuilder result = new StringBuilder(uri);
        if (uri.charAt(0) != '/') {
            result.insert(0, '/');
        }
        if (uri.charAt(uri.length() - 1) != '/') {
            result.append('/');
        }
        return result.toString();
    }

    @Override
    protected BootstrapContext createAndInitUI(Class<? extends UI> uiClass,
            VaadinRequest request, VaadinResponse response,
            VaadinSession session) {
        DeploymentConfiguration config = request.getService()
                .getDeploymentConfiguration();
        if (!config.isProductionMode()) {
            // This will throw an exception on the server side during an attempt
            // to load UI. There is also a license check when the portlet is
            // written to the page. That check shows a message on the client
            // side
             LicenseChecker.checkLicense(VaadinPortletService.PROJECT_NAME,
             VaadinPortletService.getPortletVersion(), BuildType.DEVELOPMENT);
        }
        return super.createAndInitUI(uiClass, request, response, session);
    }

    private boolean checkDevServerConnection(DevModeHandler devModeHandler) {
        if (Objects.requireNonNull(VaadinPortlet.getCurrent()).getPortletContext()
                .getAttribute(DevModeHandler.class.getName()) != null) {
            return (Boolean) VaadinPortlet.getCurrent().getPortletContext()
                    .getAttribute(DevModeHandler.class.getName());
        }
        try {
            devModeHandler.prepareConnection("/", "GET").getResponseCode();
            VaadinPortlet.getCurrent().getPortletContext()
                    .setAttribute(DevModeHandler.class.getName(), true);
            return true;
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass())
                    .debug("Error checking dev server connection", e);
        }
        VaadinPortlet.getCurrent().getPortletContext()
                .setAttribute(DevModeHandler.class.getName(), false);
        return false;
    }

    /**
     * Custom page builder that loads {@code web-component.html} from the
     * external static resources URL when configured, instead of from the local
     * classpath.
     * <p>
     * In portlet deployments, the frontend bundle is served by a separate
     * static resources WAR (the "bundle WAR"). Each portlet WAR may not have
     * its own {@code build-frontend} output, or may have one with different
     * content hashes. This builder fetches {@code web-component.html} from the
     * bundle WAR to ensure the script references match the actual JS files.
     */
    static class PortletWebComponentPageBuilder
            extends BootstrapHandler.BootstrapPageBuilder {

        @Override
        public Document getBootstrapPage(
                BootstrapHandler.BootstrapContext context) {
            VaadinService service = context.getSession().getService();
            DeploymentConfiguration config =
                    service.getDeploymentConfiguration();

            try {
                String htmlContent = null;

                // Source 1: externalStatsUrl from flow-build-info.json
                // Note: flow-build-info.json key "externalStatsUrl" is mapped
                // to internal property "external.stats.url" by Flow's config
                // factory, so we must use the accessor method.
                String externalStatsUrl = config.isStatsExternal()
                        ? config.getExternalStatsUrl()
                        : null;
                if (externalStatsUrl != null
                        && !externalStatsUrl.isEmpty()) {
                    // Derive base path from stats URL:
                    // /o/vaadin-portlet-static/VAADIN/config/stats.json
                    //  -> /o/vaadin-portlet-static/
                    int vaadinIdx = externalStatsUrl.indexOf("/VAADIN/");
                    if (vaadinIdx >= 0) {
                        htmlContent = fetchExternalWebComponentHtml(
                                externalStatsUrl.substring(0, vaadinIdx + 1),
                                context.getRequest());
                    }
                }

                // Source 2: portlet.static.resources.mapping property
                // (set via JVM arg -Dvaadin.portlet.static.resources.mapping)
                // This avoids requiring externalStatsUrl in flow-build-info.json
                if (htmlContent == null) {
                    String staticMapping = config.getStringProperty(
                            PortletConstants
                                    .PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING,
                            null);
                    if (staticMapping != null && !staticMapping.isEmpty()) {
                        if (!staticMapping.startsWith("/")) {
                            staticMapping = "/" + staticMapping;
                        }
                        if (!staticMapping.endsWith("/")) {
                            staticMapping = staticMapping + "/";
                        }
                        htmlContent = fetchExternalWebComponentHtml(
                                staticMapping, context.getRequest());
                    }
                }

                // Source 3: classpath fallback
                if (htmlContent == null) {
                    htmlContent = FrontendUtils
                            .getWebComponentHtmlContent(service);
                }

                if (htmlContent == null) {
                    throw new IOException(
                            "web-component.html not found. Ensure the static "
                                    + "resources bundle WAR is deployed and "
                                    + "accessible.");
                }

                Document document = Jsoup.parse(htmlContent);
                Element head = document.head();

                if (config.isProductionMode()) {
                    // In production mode, web-component.html already includes
                    // the entry point scripts from the bundle build
                } else if (config.getMode()
                        != Mode.DEVELOPMENT_FRONTEND_LIVERELOAD) {
                    addGeneratedIndexContent(document,
                            getStatsJson(config));
                }

                head.select("script[src]").attr("data-app-id",
                        context.getUI().getInternals().getAppId());
                head.select("script[src], link[href]")
                        .attr("crossorigin", "true");

                ObjectNode initialUIDL =
                        getInitialUidl(context.getUI());

                head.prependChild(createInlineJavaScriptElement(
                        "window.JSCompiler_renameProperty = function(a) "
                                + "{ return a; }"));
                head.prependChild(
                        getBootstrapScript(initialUIDL, context));

                if (context.getPushMode().isEnabled()) {
                    head.prependChild(createJavaScriptModuleElement(
                            getPushScript(context), true));
                }

                setupCss(head, context);

                return document;
            } catch (IOException e) {
                throw new RuntimeException(
                        "Unable to read the web-component.html file.", e);
            }
        }

        /**
         * Fetches {@code web-component.html} from the external static
         * resources bundle WAR.
         *
         * @param basePath the base path to the bundle WAR, e.g.
         *                 {@code /o/vaadin-portlet-static/}
         * @param request the current portlet request (for server info)
         * @return the HTML content, or {@code null} if not available
         */
        private String fetchExternalWebComponentHtml(
                String basePath, VaadinRequest request) {
            String cached = externalWebComponentHtmlCache;
            if (cached != null) {
                return cached;
            }

            String webComponentPath = basePath + "web-component.html";

            // Get server info from the portlet request
            VaadinPortletRequest portletRequest =
                    (VaadinPortletRequest) request;
            PortletRequest pr = portletRequest.getPortletRequest();
            String scheme = pr.getScheme();
            String serverName = pr.getServerName();
            int serverPort = pr.getServerPort();

            String urlStr = scheme + "://" + serverName
                    + ":" + serverPort + webComponentPath;

            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try {
                    if (conn.getResponseCode() == 200) {
                        try (InputStream is = conn.getInputStream()) {
                            String content = new String(
                                    is.readAllBytes(),
                                    StandardCharsets.UTF_8);
                            externalWebComponentHtmlCache = content;
                            return content;
                        }
                    } else {
                        logger.warn(
                                "Failed to fetch web-component.html from {}"
                                        + ": HTTP {}",
                                urlStr, conn.getResponseCode());
                    }
                } finally {
                    conn.disconnect();
                }
            } catch (IOException e) {
                logger.warn(
                        "Failed to fetch web-component.html from {}: {}",
                        urlStr, e.getMessage());
            }

            return null;
        }
    }
}

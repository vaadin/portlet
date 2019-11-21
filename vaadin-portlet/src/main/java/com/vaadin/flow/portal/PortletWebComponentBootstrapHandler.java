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

import javax.portlet.PortletResponse;

import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.DevModeHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.WebComponentBootstrapHandler;
import com.vaadin.pro.licensechecker.LicenseChecker;

/**
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletWebComponentBootstrapHandler
        extends WebComponentBootstrapHandler {

    // TODO: Update WebComponentBootstrapHandler API to pass also
    // current VaadinReponse instance, to avoid having to rely on
    // VaadinPortletResponse.getCurrentPortletResponse() here.
    @Override
    protected String getServiceUrl(VaadinRequest request,
            VaadinResponse response) {
        final String namespace = ((VaadinPortletResponse) response)
                .getPortletResponse().getNamespace();
        VaadinPortletSession session = VaadinPortletSession.getCurrent();
        return VaadinPortlet.getCurrent()
                .getWebComponentUIDLRequestHandlerURL(session, namespace);
    }

    @Override
    protected String modifyPath(String basePath, String path) {
        // Require that the static files are available from the server root
        path = path.replaceFirst("^.VAADIN/", "./VAADIN/");
        if (path.startsWith("./VAADIN/")) {
            DeploymentConfiguration deploymentConfiguration = VaadinPortletService
                    .getCurrent().getDeploymentConfiguration();
            if (deploymentConfiguration.isProductionMode()
                    || !deploymentConfiguration.enableDevServer()) {
                // Without dev server we serve static files from the
                // dedicated URI
                return getStaticResourcesMappingURI(deploymentConfiguration)
                        + path;
            } else if (DevModeHandler.getDevModeHandler() != null
                    && checkWebpackConnection()) {
                // With dev server running request directly from dev server
                return String.format("http://localhost:%s/%s",
                        DevModeHandler.getDevModeHandler().getPort(), path);
            }
            return "/" + path;
        }
        return super.modifyPath(basePath, path);
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
                    VaadinPortletService.getPortletVersion());
        }
        return super.createAndInitUI(uiClass, request, response, session);
    }

    private boolean checkWebpackConnection() {
        if (VaadinPortlet.getCurrent().getPortletContext()
                .getAttribute(DevModeHandler.class.getName()) != null) {
            return (Boolean) VaadinPortlet.getCurrent().getPortletContext()
                    .getAttribute(DevModeHandler.class.getName());
        }
        try {
            DevModeHandler.getDevModeHandler().prepareConnection("/", "GET")
                    .getResponseCode();
            VaadinPortlet.getCurrent().getPortletContext()
                    .setAttribute(DevModeHandler.class.getName(), true);
            return true;
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass())
                    .debug("Error checking webpack dev server connection", e);
        }
        VaadinPortlet.getCurrent().getPortletContext()
                .setAttribute(DevModeHandler.class.getName(), false);
        return false;
    }
}

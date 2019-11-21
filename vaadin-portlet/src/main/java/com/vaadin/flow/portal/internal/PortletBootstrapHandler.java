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
package com.vaadin.flow.portal.internal;

import javax.portlet.PortletContext;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import java.io.IOException;
import java.io.PrintWriter;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.portal.VaadinPortlet;
import com.vaadin.flow.portal.VaadinPortletRequest;
import com.vaadin.flow.portal.VaadinPortletResponse;
import com.vaadin.flow.portal.VaadinPortletService;
import com.vaadin.flow.server.SynchronizedRequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.WebComponentProvider;
import com.vaadin.pro.licensechecker.LicenseChecker;

/**
 * Bootstrap handler for portlet bootstrapping.
 *
 * For internal use only.
 *
 * @since
 */
public class PortletBootstrapHandler extends SynchronizedRequestHandler {

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        return ((VaadinPortletRequest) request)
                .getPortletRequest() instanceof RenderRequest;
    }

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session,
            VaadinRequest request, VaadinResponse response) throws IOException {
        VaadinPortlet<?> portlet = VaadinPortlet.getCurrent();
        String tag = portlet.getTag();
        PrintWriter writer = response.getWriter();

        PortletResponse resp = ((VaadinPortletResponse) response)
                .getPortletResponse();

        PortletContext portletContext = portlet.getPortletContext();
        String scriptUrl = (String) portletContext
                .getAttribute(WebComponentProvider.class.getName());
        if (scriptUrl == null) {
            RenderResponse renderResponse = (RenderResponse) resp;
            ResourceURL url = renderResponse.createResourceURL();
            url.setResourceID("/web-component/" + tag + ".js");
            scriptUrl = url.toString();
            portlet.setWebComponentProviderURL(session, resp.getNamespace(),
                    url.toString());

            url = renderResponse.createResourceURL();
            url.setResourceID("/web-component/web-component-ui.js");
            portlet.setWebComponentBootstrapHandlerURL(session,
                    resp.getNamespace(), url.toString());

            url = renderResponse.createResourceURL();
            url.setResourceID("/uidl");
            portlet.setWebComponentUIDLRequestHandlerURL(session,
                    resp.getNamespace(), url.toString());
        }
        String namespace = resp.getNamespace();
        writer.write("<script src='" + scriptUrl + "'></script>");

        try {
            DeploymentConfiguration config = request.getService()
                    .getDeploymentConfiguration();
            if (!config.isProductionMode()) {
                // This will throw an exception which is caught below (and the
                // message will be shown in the browser).
                // There is also a license check when the UI is going to be
                // instantiated. That will throw a server side exception.
                LicenseChecker.checkLicense(VaadinPortletService.PROJECT_NAME,
                        VaadinPortletService.getPortletVersion());
            }
            String initScript = String
                    .format("<script>window.Vaadin.Flow.Portlets.registerElement('%s','%s');</script>",
                            tag, namespace);

            writer.write(initScript);
            writer.write("<" + tag + " data-portlet-id='" + namespace + "'></"
                    + tag + ">");
        } catch (Exception exception) {
            String message = exception.getMessage();
            writer.write("<div style='color:red;'>" + message + "</div>");
        }

        return true;
    }
}

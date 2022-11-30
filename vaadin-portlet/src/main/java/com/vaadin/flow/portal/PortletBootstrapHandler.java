/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletContext;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.stream.Collectors;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.SynchronizedRequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.WebComponentProvider;
import com.vaadin.pro.licensechecker.LicenseChecker;

/**
 * Bootstrap handler for portlet bootstrapping.
 * <p>
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletBootstrapHandler extends SynchronizedRequestHandler {

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        return ((VaadinPortletRequest) request)
                .getPortletRequest() instanceof RenderRequest;
    }

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session,
            VaadinRequest request, VaadinResponse response) throws IOException {
        VaadinPortlet<?> portlet = VaadinPortlet.getCurrent();
        String tag = portlet.getPortletTag();
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

            String registrationInstruction = String
                    .format("window.Vaadin.Flow.Portlets.registerElement('%s','%s','%s','%s','%s');",
                    tag, namespace,
                    Collections.list(portlet.getWindowStates("text/html"))
                            .stream()
                            .map(state -> "\"" + state.toString() + "\"")
                            .collect(Collectors.joining(",", "[", "]")),
                    Collections.list(portlet.getPortletModes("text/html"))
                            .stream().map(mode -> "\"" + mode.toString() + "\"")
                            .collect(Collectors.joining(",", "[", "]")),
                    ((RenderResponse) resp).createActionURL());
            // For liferay send accepted window states, portlet modes and action
            // url to add to client side data.

            String initScript = portlet.portletElementRegistrationScript(request,
                    scriptUrl, registrationInstruction);

            writer.printf("<script>%s</script>", initScript);
            writer.write("<" + tag + " data-portlet-id='" + namespace
                    + "' style='width: 100%;'></" + tag + ">");
        } catch (Exception exception) {
            String message = exception.getMessage();
            writer.write("<div style='color:red;'>" + message + "</div>");
        }

        return true;
    }
}

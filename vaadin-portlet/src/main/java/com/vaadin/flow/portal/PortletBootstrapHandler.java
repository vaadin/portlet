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

import javax.portlet.PortletContext;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import java.io.IOException;
import java.io.PrintWriter;

import com.vaadin.flow.server.SynchronizedRequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.WebComponentProvider;

/**
 * Bootstrap handler for portlet bootstrapping.
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
            portlet.setWebComponentProviderURL(url.toString());

            url = renderResponse.createResourceURL();
            url.setResourceID("/web-component/web-component-ui.js");
            portlet.setWebComponentBootstrapHandlerURL(url.toString());

            url = renderResponse.createResourceURL();
            url.setResourceID("/uidl");
            portlet.setWebComponentUIDLRequestHandlerURL(url.toString());
        }
        writer.write("<script src='" + scriptUrl + "'></script>");
        writer.write("<script>customElements.whenDefined('" + tag
                + "').then(function(){ var elem = document.querySelector('"
                + tag + "');  elem.constructor._getClientStrategy = "
                + "function(portletComponent){ "
                + "   var clients = elem.constructor._getClients();"
                + "   if (!clients){ return undefined;  }"
                + "   var portlet = window.Vaadin.Flow.Portlets[portletComponent.getAttribute('data-portlet-id')];"
                + "   return clients[portlet.appId]; };"
                + getRegisterHubScript(resp) + "});</script>");
        writer.write("<" + tag + " data-portlet-id='" + resp.getNamespace()
                + "'></" + tag + ">");

        return true;
    }

    /**
     * Register this portlet to the PortletHub.
     */
    private String getRegisterHubScript(PortletResponse response) {
        StringBuilder register = new StringBuilder();

        register.append("elem.afterServerUpdate=function(){");
        register.append("var ns = '%s';");
        register.append(
                "window.Vaadin.Flow.Portlets = window.Vaadin.Flow.Portlets||{};");
        register.append(
                "window.Vaadin.Flow.Portlets[ns]=window.Vaadin.Flow.Portlets[ns]||{};");
        register.append(
                "if (!window.Vaadin.Flow.Portlets[ns].hub && portlet) {");
        register.append("portlet.register(ns).then(function (hub) {");
        register.append("window.Vaadin.Flow.Portlets[ns].hub = hub;");
        register.append(
                "hub.addEventListener('portlet.onStateChange', function(type,state){});");
        register.append("hub.addEventListener('^vaadin\\..*',");
        register.append("function(type, payload){");
        register.append(getActionScript());
        register.append("});});}");
        register.append(" elem.afterServerUpdate=null;};");

        return String.format(register.toString(), response.getNamespace());
    }

    private String getActionScript() {
        StringBuilder selectAction = new StringBuilder();

        selectAction.append("var poller = function() {");
        selectAction.append(" if(hub.isInProgress()) {");
        selectAction.append("  setTimeout(poller, 10);");
        selectAction.append(" } else {");
        selectAction.append(" var params = hub.newParameters();");
        selectAction.append(
                " params['vaadin.event']=[];params['vaadin.event'][0]=type;");
        selectAction.append(" Object.getOwnPropertyNames(payload).forEach(");
        selectAction
                .append(" function(prop){ params[prop] = payload[prop]; });");
        selectAction.append("  hub.action(params).then(function(){ ");
        // call {@code action} method on the hub is not enough: it won't be an
        // UIDL request. We need to make a fake UIDL request so that the client
        // state is updated according to the server side state.
        selectAction.append("var clients = elem.constructor._getClients();");
        selectAction.append(
                "clients[window.Vaadin.Flow.Portlets[ns].appId].poll(); });");
        selectAction.append(" }");
        selectAction.append("};");
        selectAction.append("poller();");

        return selectAction.toString();
    }

}

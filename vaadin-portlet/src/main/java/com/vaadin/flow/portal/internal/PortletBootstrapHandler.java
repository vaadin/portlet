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

    private static final long serialVersionUID = -7483736532843878484L;

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
        StringBuilder initScript = new StringBuilder();

        try {
            DeploymentConfiguration config = request.getService()
                    .getDeploymentConfiguration();
            if (!config.isProductionMode()) {
                // This will throw an exception which is caught below (and the
                // message will be shown in the browser).
                // There is also a license check when the UI is going to be
                // instantiated. That will throw a server side exception.
                LicenseChecker.checkLicense(VaadinPortletService.PROJECT_NAME,
                        VaadinPortletService.getPortletVerion());
            }

            initScript.append("<script>customElements.whenDefined('")
                    .append(tag)
                    .append("').then(function(){ var elem = document.querySelector('")
                    .append(tag)
                    .append("'); elem.constructor._getClientStrategy = ")
                    .append("function(portletComponent){ ")
                    .append("   var clients = elem.constructor._getClients();")
                    .append("   if (!clients){ return undefined;  }")
                    .append("   var portlet = window.Vaadin.Flow.Portlets[portletComponent.getAttribute('data-portlet-id')];")
                    .append("   return clients[portlet.appId]; };")
                    .append(getRegisterHubScript(tag, namespace))
                    .append(initListenerRegistrationScript(namespace))
                    .append("});</script>");

            writer.write(initScript.toString());
            writer.write("<" + tag + " data-portlet-id='" + namespace + "'></"
                    + tag + ">");
        } catch (Exception exception) {
            String message = exception.getMessage();
            writer.write("<div style='color:red;'>" + message + "</div>");
        }

        return true;
    }

    /**
     * Register this portlet to the PortletHub.
     */
    private String getRegisterHubScript(String tag, String namespace) {
        StringBuilder register = new StringBuilder();

        register.append("var targetElem;");
        register.append(" var ns = '%s';");
        register.append("var allPortletElems = document.querySelectorAll('");
        register.append(tag);
        register.append("');");
        register.append("for( i =0; i<allPortletElems.length; i++){ ");
        register.append(
                " if ( allPortletElems[i].getAttribute('data-portlet-id') == ns){targetElem=allPortletElems[i];break; }}");
        register.append(
                "var afterServerUpdate = targetElem.afterServerUpdate;");
        register.append("targetElem.afterServerUpdate=function(){");
        register.append(" if (afterServerUpdate){ afterServerUpdate(); }");
        initClientPortlet(register);
        register.append(" var portletObj = window.Vaadin.Flow.Portlets[ns];");
        register.append(" if (!portletObj.hub && portlet) {");
        register.append("  portlet.register(ns).then(function (hub) {");
        register.append("  portletObj.hub = hub;");
        register.append(
                "  hub.addEventListener('portlet.onStateChange', function(type,state){});");
        register.append("  portletObj.eventPoller = %s;");
        register.append(" if (portletObj.listeners){ ");
        register.append(
                "  Object.getOwnPropertyNames(portletObj.listeners).forEach(");
        register.append(
                " function(uid){ portletObj.registerListener(portletObj.listeners[uid], uid); }");
        register.append("); delete portletObj.listeners;}");
        register.append("});");
        register.append(" targetElem.afterServerUpdate=afterServerUpdate;}};");

        return String.format(register.toString(), namespace,
                getEventPollerFunction());
    }

    private String initListenerRegistrationScript(String namespace) {
        StringBuilder addRemoveListener = new StringBuilder();

        addRemoveListener.append("var ns = '%s';");
        initClientPortlet(addRemoveListener);
        addRemoveListener
                .append("var portletObj = window.Vaadin.Flow.Portlets[ns];")
                .append("portletObj._regListener = function(eventType, uid){")
                .append(" var poller = portletObj.eventPoller;")
                .append(" var handle = portletObj.hub.addEventListener(eventType, function(type, payload){")
                .append("   poller(type, payload, uid); });")
                .append("portletObj.eventHandles = portletObj.eventHandles||{};")
                .append("portletObj.eventHandles[uid]=handle;};")
                .append("portletObj.registerListener = function(eventType, uid){")
                .append(" if ( portletObj.hub) {portletObj._regListener(eventType, uid);} ")
                .append(" else { portletObj.listeners = portletObj.listeners||{}; ")
                .append(" portletObj.listeners[uid] = eventType;} };")
                .append("portletObj._removeListener = function(uid){")
                .append(" if ( portletObj.eventHandles && portletObj.eventHandles[uid]){ ")
                .append(" portletObj.hub.removeEventListener(portletObj.eventHandles[uid]);")
                .append(" delete portletObj.eventHandles[uid];}").append("};")
                .append("portletObj.unregisterListener = function(uid){ ")
                .append(" if ( portletObj.hub) {portletObj._removeListener(uid);} else { delete portletObj.listeners[uid]; }};");
        return String.format(addRemoveListener.toString(), namespace);
    }

    private String getEventPollerFunction() {
        return new StringBuilder().append("function(type, payload, uid) {")
                .append(" if(hub.isInProgress()) {")
                .append("  setTimeout(function(){ portletObj.eventPoller(type, payload, uid); }, 10);")
                .append(" } else {")
                .append(" var params = hub.newParameters();")
                .append(" params['vaadin.ev']=[];params['vaadin.ev'][0]=type;")
                .append(" params['vaadin.uid']=[];params['vaadin.uid'][0]=uid;")
                .append(" params['vaadin.wn']=[];params['vaadin.wn'][0]=window.name;")
                .append("if (payload){ ")
                .append(" Object.getOwnPropertyNames(payload).forEach(")
                .append(" function(prop){ params[prop] = payload[prop]; });}")
                .append("  hub.action(params).then(function(){ ")
                // call {@code action} method on the hub is not enough: it won't
                // be an UIDL request. We need to make a fake UIDL request so
                // that the client state is updated according to the server side
                // state.
                .append("var clients = elem.constructor._getClients();")
                .append("clients[portletObj.appId].poll(); });").append(" }")
                .append("};").toString();
    }

    private void initClientPortlet(StringBuilder builder) {
        builder.append(
                " window.Vaadin.Flow.Portlets = window.Vaadin.Flow.Portlets||{};");
        builder.append(
                " window.Vaadin.Flow.Portlets[ns]=window.Vaadin.Flow.Portlets[ns]||{};");
    }

}

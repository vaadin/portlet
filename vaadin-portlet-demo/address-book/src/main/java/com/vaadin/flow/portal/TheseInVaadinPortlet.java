/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;

/**
 * This portlet class denotes things that would be set to the actual
 * VaadinPortlet class
 *
 * @param <VIEW>
 */
public abstract class TheseInVaadinPortlet<VIEW extends Component>
        extends VaadinPortlet<VIEW> {

    private WindowState windowState = WindowState.UNDEFINED;
    private PortletMode mode = PortletMode.UNDEFINED;

    /**
     * This should be overridden with a method that knows the VIEW component
     * instance.
     *
     * @param event
     */
    protected void fireModeChange(PortletModeEvent event) {
    }

    /**
     * This should be overridden with a method that knows the VIEW component
     * instance.
     *
     * @param event
     */
    protected void fireStateChange(WindowStateEvent event) {
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
        WindowState oldState = windowState;
        windowState = request.getWindowState();
        if (!oldState.equals(windowState) && isViewInstanceOf(
                WindowStateHandler.class)) {
            fireStateChange(new WindowStateEvent(windowState));
            // How do we get the Component??
            // This would probably need to fire a push or generate a UIDL request!
        }
    }

    /**
     * Get the current window state
     *
     * @return current window state of the portlet
     */
    public WindowState getWindowState() {
        return windowState;
    }

    /**
     * Set a new window state for this portlet
     *
     * @param state
     *         window state to set
     */
    public void setWindowState(WindowState state) {
        StringBuilder stateChange = new StringBuilder();
        stateChange.append(getHubString());
        stateChange.append("var state = hub.newState();");
        stateChange.append(String.format("state.windowState = '%s';", state));
        stateChange.append("hub.setRenderState(state);");
        stateChange.append(getReloadPoller());

        UI.getCurrent().getElement().executeJs(stateChange.toString());
    }

    /**
     * Get the current portlet mode for this portlet
     *
     * @return portlet window state
     */
    public PortletMode getPortletMode() {
        return mode;
    }

    /**
     * Set a new portlet mode for this portlet.
     *
     * @param portletMode
     *         portlet mode to set
     */
    public void setPortletMode(PortletMode portletMode) {
        StringBuilder modeChange = new StringBuilder();
        modeChange.append(getHubString());
        modeChange.append("var state = hub.newState();");
        modeChange.append(String
                .format("state.portletMode = '%s';", portletMode));
        modeChange.append("hub.setRenderState(state);");
        modeChange.append(getReloadPoller());

        UI.getCurrent().getElement().executeJs(modeChange.toString());
    }

    /**
     * This is a script that will handle page reload for page when hub has
     * completed.
     *
     * @return portlet reload poller
     */
    private String getReloadPoller() {
        StringBuilder reloader = new StringBuilder();
        reloader.append("const poller = () => {");
        reloader.append("  if(hub.isInProgress()) {");
        reloader.append("    setTimeout(poller, 10);");
        reloader.append("  } else {");
        reloader.append("    location.reload();");
        reloader.append("  }");
        reloader.append("};");
        reloader.append("poller();");
        return reloader.toString();
    }

    /**
     * Send an action event with the given parameters
     *
     * @param parameters
     *         parameters to add to event action
     */
    public void sendEvent(String eventName, Map<String, String> parameters) {
        StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append(getHubString());
        eventBuilder.append("var params = hub.newParameters();");
        eventBuilder.append("params['action'] = ['send'];");
        parameters.forEach((key, value) -> eventBuilder
                .append(String.format("params['%s'] = ['%s'];", key, value)));
        eventBuilder.append(String
                .format("hub.dispatchClientEvent('%s', params);", eventName));

        UI.getCurrent().getElement().executeJs(eventBuilder.toString());
    }


    public void registerHub() {
        registerHub(Collections.emptyMap());
    }

    /**
     * Register this portlet to the PortletHub.
     * <p>
     * eventlisteners should be in the format EventName, Event function payload which will get params type and state
     */
    public void registerHub(Map<String, String> eventListeners) {
        String portletRegistryName = VaadinPortletService
                .getCurrentResponse().getPortletResponse().getNamespace();
        StringBuilder register = new StringBuilder();

        register.append("if (!window.Vaadin.Flow.Portlets) {");
        register.append(
                "window.Vaadin.Flow['Portlets'] = {};");
        register.append("}");
        register.append("if (!window.Vaadin.Flow.Portlets.$0) {");
        register.append("if (portlet) {");
        register.append("portlet.register($0).then(function (hub) {");
        register.append("window.Vaadin.Flow.Portlets[$0] = hub;");
        register.append(
                "hub.addEventListener('portlet.onStateChange', function (type, state) {});");
        eventListeners.forEach((event, functionPayload) -> {
            register.append("hub.addEventListener('").append(event)
                    .append("',");
            register.append("function(type, state){").append(functionPayload)
                    .append("});");
        });
        register.append("});");
        register.append("}");
        register.append("}");

        UI.getCurrent().getElement()
                .executeJs(register.toString(), portletRegistryName);
    }

    /**
     * Get the string for getting the hub variable stored for hub registration.
     *
     * @return js to get hub registration object
     */
    private String getHubString() {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();
        return String.format("var hub = window.Vaadin.Flow.Portlets['%s'];",
                portletRegistryName);
    }
}

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
package com.vaadin.flow.portal.util;

import java.util.Collections;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.portal.VaadinPortletService;

public final class PortletHubUtil {

    private PortletHubUtil() {
    }

    /**
     * Register this portlet to the PortletHub.
     * <p>
     * hub registration will be stored
     */
    public static void registerHub() {
        registerHub(Collections.emptyMap());
    }

    /**
     * Register this portlet to the PortletHub.
     *
     * @param eventListeners
     *         event listeners to register for this portlet. eventListeners
     *         should be in the format EventName, Event function payload
     *         which will get params type and state
     */
    public static void registerHub(Map<String, String> eventListeners) {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();
        StringBuilder register = new StringBuilder();

        register.append("if (!window.Vaadin.Flow.Portlets) {");
        register.append("window.Vaadin.Flow['Portlets'] = {};");
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
     * Update the portlet state with the given windowState and portletMode.
     *
     * @param windowState
     *         window state to send
     * @param portletMode
     *         portlet mode to send
     */
    public static void updatePortletState(String windowState,
            String portletMode) {

        StringBuilder stateChange = new StringBuilder();
        stateChange.append(getHubString());
        stateChange.append("var state = hub.newState();");
        stateChange.append(String
                .format("state.windowState = '%s';", windowState));
        stateChange.append(String
                .format("state.portletMode = '%s';", portletMode));
        stateChange.append("hub.setRenderState(state);");
        stateChange.append(getReloadPoller());

        UI.getCurrent().getElement().executeJs(stateChange.toString());
    }

    /**
     * Get JavaScript string for getting the portlet hub registration object.
     *
     * @return registration object stored as 'hub'
     */
    public static String getHubString() {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();
        return String.format("var hub = window.Vaadin.Flow.Portlets['%s'];",
                portletRegistryName);
    }

    /**
     * This is a script that will handle page reload for page when hub has
     * completed.
     *
     * @return portlet reload poller
     */
    public static String getReloadPoller() {
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

}

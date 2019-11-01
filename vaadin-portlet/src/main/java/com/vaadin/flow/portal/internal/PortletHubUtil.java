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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.portal.VaadinPortletService;

/**
 * For internal use only.
 */
public final class PortletHubUtil {

    private PortletHubUtil() {
    }

    /**
     * Update the portlet state with the given windowState and portletMode.
     *
     * @param windowState
     *            window state to send
     * @param portletMode
     *            portlet mode to send
     */
    public static void updatePortletState(String windowState,
            String portletMode) {

        StringBuilder stateChange = new StringBuilder();
        stateChange.append(getHubString());
        stateChange.append("var state = hub.newState();");
        stateChange.append(
                String.format("state.windowState = '%s';", windowState));
        stateChange.append(
                String.format("state.portletMode = '%s';", portletMode));
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
        return String.format("var hub = window.Vaadin.Flow.Portlets['%s'].hub;",
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

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

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.portal.util.PortletHubUtil;

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
     * Send an action event with the given parameters
     *
     * @param parameters
     *         parameters to add to event action
     */
    public void sendEvent(String eventName, Map<String, String> parameters) {
        StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append(PortletHubUtil.getHubString());
        eventBuilder.append("var params = hub.newParameters();");
        eventBuilder.append("params['action'] = ['send'];");
        parameters.forEach((key, value) -> eventBuilder
                .append(String.format("params['%s'] = ['%s'];", key, value)));
        eventBuilder.append(String
                .format("hub.dispatchClientEvent('%s', params);", eventName));

        UI.getCurrent().getElement().executeJs(eventBuilder.toString());
    }
}


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
package com.vaadin.flow.portal.liferay.events;

import java.util.Collections;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;
import com.vaadin.flow.portal.lifecycle.PortletEvent;

public class LiferayVaadinIPCView extends Div implements PortletView {

    private Div info = new Div();

    public LiferayVaadinIPCView() {
        add(info);
        info.setId("response-from-plain-portlet");
    }

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        context.addEventChangeListener("plain-portlet", this::handleEvent);

        Button button = new Button("Send event to non-Vaadin portlet",
                event -> context.fireEvent("vaadin-portlet",
                        Collections.singletonMap("bar", "baz")));
        button.setId("send-to-plain");
        add(button);
    }

    private void handleEvent(PortletEvent event) {
        info.setText(event.getParameters().get("data")[0]);
    }

}

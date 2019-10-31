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
package com.vaadin.flow.portal.events;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.portal.handler.PortletEvent;
import com.vaadin.flow.portal.handler.PortletView;
import com.vaadin.flow.portal.handler.PortletViewContext;
import com.vaadin.flow.shared.Registration;

public class OtherEventTargetView extends Div implements PortletView {

    private PortletViewContext context;
    private Registration registration;

    public OtherEventTargetView() {
        Div div = new Div();
        div.setText("Other Target");
        add(div);

        NativeButton button = new NativeButton("Start listening events",
                event -> {
                    registration = context.addEventChangeListener("click",
                            this::handleEvent);
                });
        button.setId("start-listen");
        add(button);
    }

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.context = context;
    }

    private void handleEvent(PortletEvent event) {
        Div div = new Div();
        div.setClassName("other-event");
        div.setText(event.getEventName());
        add(div);
        registration.remove();
    }

}

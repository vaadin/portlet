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
package com.vaadin.flow.portal.events;

import java.util.Collections;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.portal.handler.VaadinPortletEventContext;
import com.vaadin.flow.portal.handler.VaadinPortletEventView;

public class EventSourceView extends Div implements VaadinPortletEventView {

    private VaadinPortletEventContext eventContext;

    public EventSourceView() {
        NativeButton button = new NativeButton("Send event",
                event -> eventContext.fireEvent("click",
                        Collections.singletonMap("button", "left")));
        button.setId("send-event");
        add(button);
    }

    @Override
    public void onPortletEventContextInit(VaadinPortletEventContext context) {
        this.eventContext = context;
    }

}

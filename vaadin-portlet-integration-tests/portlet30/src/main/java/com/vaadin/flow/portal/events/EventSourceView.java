/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.events;

import java.util.Collections;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;

public class EventSourceView extends Div implements PortletView {

    private PortletViewContext eventContext;

    public EventSourceView() {
        NativeButton button = new NativeButton("Send event",
                event -> eventContext.fireEvent("click",
                        Collections.singletonMap("button", "left")));
        button.setId("send-event");
        add(button);
    }

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.eventContext = context;
    }

}

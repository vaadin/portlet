/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.events;

import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.portal.PortletViewContext;
import com.vaadin.flow.portal.lifecycle.EventHandler;
import com.vaadin.flow.portal.lifecycle.PortletEvent;

public class EventTargetView extends Div implements EventHandler {

    private PortletViewContext context;

    public EventTargetView() {
        Div div = new Div();
        div.setText("Target");
        add(div);
    }

    @Override
    public void handleEvent(PortletEvent event) {
        Div div = new Div();
        div.setClassName("event");
        String[] param = event.getParameters().get("button");
        div.setText(event.getEventName()
                + (param == null ? "" : Arrays.asList(param)));

        add(div);
    }

}

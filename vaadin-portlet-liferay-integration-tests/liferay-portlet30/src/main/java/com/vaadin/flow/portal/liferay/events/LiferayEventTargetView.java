/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay.events;

import java.util.Arrays;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.portal.lifecycle.EventHandler;
import com.vaadin.flow.portal.lifecycle.PortletEvent;

public class LiferayEventTargetView extends Div implements EventHandler {

    public LiferayEventTargetView() {
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

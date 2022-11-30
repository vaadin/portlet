/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.basic;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.portal.VaadinPortlet;

public class LiferayBasicPortletContent extends VerticalLayout {

    public static final String GREETING_MESSAGE_ID = "greeting-message";

    public LiferayBasicPortletContent() {
        VaadinPortlet<?> portlet = VaadinPortlet.getCurrent();
        String name = portlet.getPortletName();
        String serverInfo = portlet.getPortletContext().getServerInfo();
        Div message = new Div();
        message.setId(GREETING_MESSAGE_ID);
        Button button = new Button("Click me", event -> message.setText(
                "Hello from " + name + " running in " + serverInfo + "!"));
        button.setId("click-button");
        add(button, message);
    }
}


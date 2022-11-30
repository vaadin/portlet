/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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

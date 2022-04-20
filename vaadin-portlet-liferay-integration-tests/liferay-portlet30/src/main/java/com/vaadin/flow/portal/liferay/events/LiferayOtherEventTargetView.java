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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;
import com.vaadin.flow.portal.lifecycle.PortletEvent;
import com.vaadin.flow.shared.Registration;

public class LiferayOtherEventTargetView extends Div implements PortletView {

    private PortletViewContext context;
    private Registration registration;

    public LiferayOtherEventTargetView() {
        Div div = new Div();
        div.setText("Other Target");
        add(div);

        NativeButton button = new NativeButton("Start listening events",
                event -> registration = context.addEventChangeListener("click",
                        this::handleEvent));
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

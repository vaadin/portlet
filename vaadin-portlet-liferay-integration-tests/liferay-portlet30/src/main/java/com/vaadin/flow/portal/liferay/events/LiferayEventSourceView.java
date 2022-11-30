/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.events;

import java.util.Collections;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;

public class LiferayEventSourceView extends Div implements PortletView {

    private PortletViewContext eventContext;

    public LiferayEventSourceView() {
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

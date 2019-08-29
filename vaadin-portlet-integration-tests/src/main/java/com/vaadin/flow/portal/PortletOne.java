package com.vaadin.flow.portal;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class PortletOne extends VerticalLayout {

    public PortletOne() {
        add(new MainView());
    }
}
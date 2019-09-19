package com.vaadin.flow.portal.modedemo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.portal.VaadinPortlet;

public class ModeDemoView extends VerticalLayout {

    public ModeDemoView() {
        Button button = new Button("Click me", ce -> {
            ModeDemoPortlet portlet = (ModeDemoPortlet) VaadinPortlet.getCurrent();
            String message = portlet.getMessage();
            Notification.show(message);
        });
        add(button);
    }
}

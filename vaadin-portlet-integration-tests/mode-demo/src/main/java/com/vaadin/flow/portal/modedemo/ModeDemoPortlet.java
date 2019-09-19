package com.vaadin.flow.portal.modedemo;

import com.vaadin.flow.portal.VaadinPortlet;

public class ModeDemoPortlet extends VaadinPortlet {
    // Message to show is shared between all portlet instances
    private String message = "This is the default message from ModeDemoPortlet";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

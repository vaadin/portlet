package com.vaadin.flow.portal;

public class MyPortlet extends VaadinPortlet {
    @Override
    public String getName() {
        // This corresponds to the war filename
        return "portlet-integration-tests";
    }

}
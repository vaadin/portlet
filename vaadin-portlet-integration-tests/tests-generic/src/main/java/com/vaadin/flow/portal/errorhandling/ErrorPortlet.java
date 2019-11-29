package com.vaadin.flow.portal.errorhandling;

import com.vaadin.flow.portal.VaadinPortlet;

public class ErrorPortlet extends VaadinPortlet<ErrorPortletContent> {
    @Override
    public String getTag() {
        return "error-portlet";
    }
}

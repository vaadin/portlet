package com.vaadin.flow.portal;

import javax.portlet.PortletException;

public class MySecondPortlet extends VaadinPortlet {

    @Override
    protected String getMainComponentTag() throws PortletException {
        return "my-second-portlet";
    }
}
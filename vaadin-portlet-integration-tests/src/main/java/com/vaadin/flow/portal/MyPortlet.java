package com.vaadin.flow.portal;

import javax.portlet.PortletException;

public class MyPortlet extends VaadinPortlet {

    @Override
    protected String getMainComponentTag() throws PortletException {
        return "my-portlet";
    }
}

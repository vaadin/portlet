package com.vaadin.flow.portal.basic;

import com.vaadin.flow.portal.VaadinPortlet;

public class BasicPortlet extends VaadinPortlet {

    public static final String TAG = "basic-portlet";

    @Override
    public String getMainComponentTag() {
        return TAG;
    }
}

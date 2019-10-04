package com.vaadin.flow.portal;

public class MySecondPortlet extends WindowStatePortlet {

    public static final String TAG = "my-second-portlet";

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

}

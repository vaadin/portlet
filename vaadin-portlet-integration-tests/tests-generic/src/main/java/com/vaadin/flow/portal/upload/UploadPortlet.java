package com.vaadin.flow.portal.upload;

import com.vaadin.flow.portal.VaadinPortlet;

public class UploadPortlet extends VaadinPortlet {

    public static final String TAG = "upload-portlet";

    @Override
    public String getMainComponentTag() {
        return TAG;
    }
}

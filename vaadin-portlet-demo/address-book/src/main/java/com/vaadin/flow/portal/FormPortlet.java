package com.vaadin.flow.portal;

public class FormPortlet extends VaadinPortlet<FormPortletView> {

    public static final String TAG = "form-portlet";

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }
}

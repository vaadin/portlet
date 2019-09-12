package com.vaadin.flow.portal.upload;


import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Theme(value = Lumo.class)
public class UploadPortletExporter extends WebComponentExporter<UploadPortletContent> {
    public UploadPortletExporter() {
        super(UploadPortlet.TAG);
    }

    @Override
    protected void configureInstance(WebComponent<UploadPortletContent> webComponent,
                                     UploadPortletContent component) {
        // No configuration required
    }
}

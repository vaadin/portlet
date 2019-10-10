package com.vaadin.flow.portal;

import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * This class defines a tag which is internally used to render the portlet.
 * Should be automated later on.
 */
@Theme(value = Lumo.class)
public class FormPortletExporter
        extends WebComponentExporter<FormPortletView> {
    public FormPortletExporter() {
        super(FormPortlet.TAG);
    }

    @Override
    protected void configureInstance(WebComponent<FormPortletView> webComponent,
            FormPortletView component) {
    }
}

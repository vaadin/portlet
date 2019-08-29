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
public class SecondPortletContentExporter extends WebComponentExporter<PortletTwo> {
    public SecondPortletContentExporter() {
        super("my-second-portlet");
    }

    @Override
    protected void configureInstance(WebComponent<PortletTwo> webComponent,
            PortletTwo component) {

    }
}
package com.vaadin.flow.portal;

import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.shared.util.SharedUtil;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * This class defines a tag which is internally used to render the portlet.
 * Should be automated later on.
 */
@Theme(value = Lumo.class)
public class PortletContentExporter extends WebComponentExporter<PortletOne> {
    public PortletContentExporter() {
        super("my-portlet");
    }

    @Override
    protected void configureInstance(WebComponent<PortletOne> webComponent,
            PortletOne component) {

    }

}

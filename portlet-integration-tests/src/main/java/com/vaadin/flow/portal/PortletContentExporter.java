package com.vaadin.flow.portal;

import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.shared.util.SharedUtil;

/**
 * This class defines a tag which is internally used to render the portlet.
 * Should be automated later on.
 */
public class PortletContentExporter extends WebComponentExporter<MainView> {
    public PortletContentExporter() {
        super(SharedUtil
                .camelCaseToDashSeparated(MainView.class.getSimpleName())
                .replaceFirst("^-", ""));
    }

    @Override
    protected void configureInstance(WebComponent<MainView> webComponent,
            MainView component) {

    }

}
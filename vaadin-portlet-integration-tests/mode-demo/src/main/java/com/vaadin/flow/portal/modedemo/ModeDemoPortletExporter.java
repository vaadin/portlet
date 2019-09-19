package com.vaadin.flow.portal.modedemo;

import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.shared.util.SharedUtil;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Theme(value = Lumo.class)
@PreserveOnRefresh
public class ModeDemoPortletExporter extends WebComponentExporter<ModeDemoPortletUI> {
    public ModeDemoPortletExporter() {
        super("mode-demo");
    }

    @Override
    protected void configureInstance(WebComponent<ModeDemoPortletUI> webComponent,
                                     ModeDemoPortletUI component) {
        // NOOP
    }
}
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
//@PreserveOnRefresh
public class GridPortletExporter extends WebComponentExporter<GridPortletView> {
    public GridPortletExporter() {
        super(GridPortlet.TAG);
    }
    @Override
    protected void configureInstance(WebComponent<GridPortletView> webComponent,
                                     GridPortletView component) {
    }

}

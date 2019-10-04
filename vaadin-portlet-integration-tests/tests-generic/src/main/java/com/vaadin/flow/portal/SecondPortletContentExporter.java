package com.vaadin.flow.portal;

import javax.portlet.WindowState;

import org.slf4j.LoggerFactory;

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
        super(MySecondPortlet.TAG);
    }

    @Override
    protected void configureInstance(WebComponent<PortletTwo> webComponent,
            PortletTwo component) {
        WindowState windowState = ((MySecondPortlet) VaadinPortlet.getCurrent())
                .getWindowState();

        if (windowState.equals(WindowState.NORMAL)) {
            component.renderNormal();
        } else if (windowState.equals(WindowState.MAXIMIZED)) {
            component.renderMaximized();
        } else if (windowState.equals(WindowState.MINIMIZED)) {
            component.renderMinimized();
        }
    }
}

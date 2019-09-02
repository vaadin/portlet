package com.vaadin.flow.portal;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;


public class MyPortlet extends VaadinPortlet {
    @Override
    public Class<? extends Component> getComponentClass() {
        return MainView.class;
    }
}

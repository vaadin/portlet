package com.vaadin.flow.portal.liferay.errorhandling;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;

public class LiferayErrorPortletContent extends Div {
    public LiferayErrorPortletContent() {
        Button button = new Button("Click me!", event -> {
            throw new RuntimeException("Exception!"); // NOSONAR
        });
        button.setId("error-button");
        add(button);
    }
}

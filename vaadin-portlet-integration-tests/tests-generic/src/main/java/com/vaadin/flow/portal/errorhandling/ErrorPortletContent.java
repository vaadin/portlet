package com.vaadin.flow.portal.errorhandling;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;

public class ErrorPortletContent extends Div {
    public ErrorPortletContent() {
        Button button = new Button("Click meh!", event -> {
            throw new RuntimeException("Exception!");
        });
        button.setId("error-button");
        add(button);
    }
}

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
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

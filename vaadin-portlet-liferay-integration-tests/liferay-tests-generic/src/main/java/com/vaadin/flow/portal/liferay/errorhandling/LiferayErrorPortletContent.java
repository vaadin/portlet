/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
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

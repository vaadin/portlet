/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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

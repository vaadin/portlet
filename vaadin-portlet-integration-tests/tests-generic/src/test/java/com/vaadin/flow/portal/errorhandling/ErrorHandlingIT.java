/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.errorhandling;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class ErrorHandlingIT extends AbstractPlutoPortalTest {
    public ErrorHandlingIT() {
        super("tests-generic", "errorhandling");
    }

    @Test
    public void exceptionIsShownInsideOfTheThrowingPortlet() {
        TestBenchElement buttonElement = getVaadinPortletRootElement().$("*")
                .id("error-button");
        buttonElement.click();

        TestBenchElement errorElement = getVaadinPortletRootElement().$("*")
                .attribute("class", "v-system-error").first();

        Assert.assertNotNull("Error should have been found", errorElement);
    }
}

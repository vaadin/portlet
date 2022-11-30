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

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

@NotThreadSafe
public class LiferayErrorHandlingIT extends AbstractLiferayPortalTest {

    @Test
    public void exceptionIsShownInsideOfTheThrowingPortlet() {
        TestBenchElement buttonElement = getVaadinPortletRootElement().$("*")
                .id("error-button");
        buttonElement.click();

        TestBenchElement errorElement = getVaadinPortletRootElement().$("*")
                .attribute("class", "v-system-error").first();

        Assert.assertNotNull("Error should have been found", errorElement);
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/errorhandling";
    }
}

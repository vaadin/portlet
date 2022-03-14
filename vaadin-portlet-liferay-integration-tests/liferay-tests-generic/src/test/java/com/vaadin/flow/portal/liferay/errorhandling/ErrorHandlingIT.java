package com.vaadin.flow.portal.liferay.errorhandling;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class ErrorHandlingIT extends AbstractLiferayPortalTest {

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

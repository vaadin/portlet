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
        ButtonElement buttonElement = getFirstPortlet()
                .$(ButtonElement.class)
                .id("error-button");
        buttonElement.click();

        TestBenchElement errorElement = getFirstPortlet()
                .$(TestBenchElement.class).attribute("class", "v-system-error")
                .first();

        Assert.assertNotNull("Error should have been found", errorElement);
    }
}

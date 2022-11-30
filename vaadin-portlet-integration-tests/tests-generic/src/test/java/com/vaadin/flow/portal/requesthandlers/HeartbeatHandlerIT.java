/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.requesthandlers;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.flow.portal.basic.BasicPortletContent;
import com.vaadin.testbench.TestBenchElement;

public class HeartbeatHandlerIT extends AbstractPlutoPortalTest {
    public HeartbeatHandlerIT() {
        super("tests-generic", "basic");
    }

    @Test
    public void basicPortlet_afterFirstHeartbeatRequest_shouldWorkNormally()
            throws InterruptedException {
        TestBenchElement buttonElement = getVaadinPortletRootElement().$("*")
                .id("click-button");

        Thread.sleep(17000);

        // In this test, we make sure that Vaadin responds after the first
        // heartbeat request. If it doesn't respond, then the test will hang
        // because of waitForVaadin. That's why it is disabled here.
        // See https://github.com/vaadin/portlet/issues/166
        testBench().disableWaitForVaadin();
        try {
            buttonElement.click();
            TestBenchElement greetingMessage = getVaadinPortletRootElement().$(
                    "*").id(BasicPortletContent.GREETING_MESSAGE_ID);
            Assert.assertTrue(
                    greetingMessage.getText().startsWith("Hello from "));
        } finally {
            testBench().enableWaitForVaadin();
        }
    }
}

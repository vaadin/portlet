/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.requesthandlers;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.flow.portal.liferay.basic.LiferayBasicPortletContent;
import com.vaadin.testbench.TestBenchElement;

@NotThreadSafe
public class LiferayHeartbeatHandlerIT extends AbstractLiferayPortalTest {

    @Test
    public void basicPortlet_afterFirstHeartbeatRequest_shouldWorkNormally()
            throws InterruptedException {
        TestBenchElement buttonElement = getVaadinPortletRootElement().$("*")
                .id("click-button");

        // In this test, we make sure that Vaadin responds after the first
        // heartbeat request. If it doesn't respond, then the test will hang
        // because of waitForVaadin. That's why it is disabled here.
        // See https://github.com/vaadin/portlet/issues/166
        testBench().disableWaitForVaadin();
        try {
            buttonElement.click();
            TestBenchElement greetingMessage = getVaadinPortletRootElement().$(
                    "*").id(LiferayBasicPortletContent.GREETING_MESSAGE_ID);
            Assert.assertTrue(
                    greetingMessage.getText().startsWith("Hello from "));
        } finally {
            testBench().enableWaitForVaadin();
        }
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/basic";
    }
}

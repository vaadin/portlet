/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.events;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;

public class IPCNotVaadinEventIT extends AbstractPlutoPortalTest {

    public IPCNotVaadinEventIT() {
        super("portlet30", "vaadin-ipc-portlet");
    }

    @Test
    public void sendEventFromNonVaadinToVaadin() throws InterruptedException {
        addPortlet("PlainPortlet");

        $(NativeButtonElement.class).attribute("id", "send-to-vaadin")
                .waitForFirst().click();

        WebElement event = getVaadinPortletRootElement().$("*")
                .id("response-from-plain-portlet");
        Assert.assertEquals("foo", event.getText());
    }

    @Test
    public void sendEventFromVaadinToNonVaadin() throws InterruptedException {
        addPortlet("PlainPortlet");

        getVaadinPortletRootElement().$("*")
                .attribute("id", "send-to-plain").waitForFirst().click();

        WebElement event = findElement(By.id("response-from-vaadin"));
        Assert.assertEquals("baz", event.getText());
    }
}

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.events;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class IPCEventIT extends AbstractPlutoPortalTest {

    public IPCEventIT() {
        super("portlet30", "event-target");
    }

    @Test
    public void sendEventFromSourceToTarget() throws InterruptedException {
        String eventSource = addVaadinPortlet("event-source");
        String otherEventTarget = addVaadinPortlet("other-event-target");

        TestBenchElement sendEvent = getVaadinPortletRootElement(eventSource).$(
                "*").attribute("id", "send-event").waitForFirst();

        sendEvent.click();

        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").exists());

        WebElement event = getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").first();
        Assert.assertEquals("click[left]", event.getText());

        Assert.assertFalse(getVaadinPortletRootElement(otherEventTarget).$("*")
                .attributeContains("class", "other-event").exists());

        // add an event listener programmatically
        getVaadinPortletRootElement(otherEventTarget).$("*").id("start-listen")
                .click();

        sendEvent.click();

        waitUntil(driver ->
                getVaadinPortletRootElement().$("*").attributeContains("class", "event").all()
                        .size() == 2);

        // event should be received by a programmatic listener
        Assert.assertTrue(getVaadinPortletRootElement(otherEventTarget).$("*")
                .attributeContains("class", "other-event").exists());

        // once event is received the programmatic listener should remove
        // itself, so no more events
        sendEvent.click();

        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").all().size() == 3);
        Assert.assertEquals(1,
                getVaadinPortletRootElement(otherEventTarget).$("*")
                        .attributeContains("class", "other-event").all()
                        .size());
    }

    @Test
    public void sendEventFromSourceToTarget_portletsOnDifferentTabsReceiveEventsIndependently()
            throws InterruptedException {
        String eventSource = addVaadinPortlet("event-source");

        String firstTab = driver.getWindowHandle();
        String secondTab = openInAnotherWindow();

        driver.switchTo().window(firstTab);
        getVaadinPortletRootElement(eventSource).$(NativeButtonElement.class)
                .attribute("id", "send-event").waitForFirst().click();

        driver.switchTo().window(secondTab);
        getVaadinPortletRootElement(eventSource).$(NativeButtonElement.class)
                .attribute("id", "send-event").waitForFirst().click();

        driver.switchTo().window(firstTab);
        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").exists());
        List<TestBenchElement> events1 = getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").all();
        Assert.assertEquals(1, events1.size());
        Assert.assertEquals("click[left]", events1.get(0).getText());

        driver.switchTo().window(secondTab);
        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").exists());
        List<TestBenchElement> events2 = getVaadinPortletRootElement().$("*")
                .attributeContains("class", "event").all();
        Assert.assertEquals(1, events2.size());
        Assert.assertEquals("click[left]", events2.get(0).getText());
    }
}

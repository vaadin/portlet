/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.portal.liferay.events;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

@NotThreadSafe
public class LiferayIPCEventIT extends AbstractLiferayPortalTest {

    @Test
    public void sendEventFromSourceToTarget() {
        TestBenchElement eventSourcePortlet = getEventSourcePortlet();

        TestBenchElement sendEvent = eventSourcePortlet.$(
                "*").attribute("id", "send-event").waitForFirst();

        sendEvent.click();

        TestBenchElement eventTargetPortlet = getEventTargetPortlet();
        TestBenchElement otherEventTargetPortlet = getOtherEventTargetPortlet();

        waitUntil(driver -> eventTargetPortlet.$("*")
                .attributeContains("class", "event").exists());

        WebElement event = eventTargetPortlet.$("*")
                .attributeContains("class", "event").first();
        Assert.assertEquals("click[left]", event.getText());

        Assert.assertFalse(otherEventTargetPortlet.$("*")
                .attributeContains("class", "other-event").exists());

        // add an event listener programmatically
        otherEventTargetPortlet.$("*").id("start-listen").click();

        sendEvent.click();

        TestBenchElement eventTargetPortlet2 = getEventTargetPortlet();

        waitUntil(driver ->
                eventTargetPortlet2.$("*").attributeContains("class",
                        "event").all().size() == 2);

        TestBenchElement otherEventTargetPortlet2 =
                getOtherEventTargetPortlet();

        // event should be received by a programmatic listener
        Assert.assertTrue(otherEventTargetPortlet2.$("*")
                .attributeContains("class", "other-event").exists());

        // once event is received the programmatic listener should remove
        // itself, so no more events
        sendEvent.click();

        TestBenchElement eventTargetPortlet3 = getEventTargetPortlet();
        TestBenchElement otherEventTargetPortlet3 =
                getOtherEventTargetPortlet();

        waitUntil(driver -> eventTargetPortlet3.$("*")
                .attributeContains("class", "event").all().size() == 3);
        Assert.assertEquals(1,
                otherEventTargetPortlet3.$("*")
                        .attributeContains("class", "other-event").all()
                        .size());
    }

    @Test
    public void sendEventFromSourceToTarget_portletsOnDifferentTabsReceiveEventsIndependently() {
        String firstTab = driver.getWindowHandle();
        String secondTab = openInAnotherWindow();

        driver.switchTo().window(firstTab);
        getEventSourcePortlet().$(NativeButtonElement.class)
                .attribute("id", "send-event").waitForFirst().click();

        driver.switchTo().window(secondTab);
        getEventSourcePortlet().$(NativeButtonElement.class)
                .attribute("id", "send-event").waitForFirst().click();

        driver.switchTo().window(firstTab);
        TestBenchElement eventTargetPortlet = getEventTargetPortlet();
        waitUntil(driver -> eventTargetPortlet.$("*")
                .attributeContains("class", "event").exists());
        List<TestBenchElement> events1 = eventTargetPortlet.$("*")
                .attributeContains("class", "event").all();
        Assert.assertEquals(1, events1.size());
        Assert.assertEquals("click[left]", events1.get(0).getText());

        driver.switchTo().window(secondTab);
        TestBenchElement eventTargetPortlet2 = getEventTargetPortlet();
        waitUntil(driver -> eventTargetPortlet2.$("*")
                .attributeContains("class", "event").exists());
        List<TestBenchElement> events2 = eventTargetPortlet2.$("*")
                .attributeContains("class", "event").all();
        Assert.assertEquals(1, events2.size());
        Assert.assertEquals("click[left]", events2.get(0).getText());
    }

    private TestBenchElement getOtherEventTargetPortlet() {
        return getVaadinPortletRootElementByStaticPart(
                "othereventtarget_WAR_liferayportlet30");
    }

    private TestBenchElement getEventTargetPortlet() {
        return getVaadinPortletRootElementByStaticPart(
                "eventtarget_WAR_liferayportlet30");
    }

    private TestBenchElement getEventSourcePortlet() {
        return getVaadinPortletRootElementByStaticPart(
                "eventsource_WAR_liferayportlet30");
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/ipcevent";
    }
}

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.events;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;

@NotThreadSafe
public class LiferayIPCNotVaadinEventIT extends AbstractLiferayPortalTest {

    @Test
    public void sendEventFromNonVaadinToVaadin() {
        $(NativeButtonElement.class).attribute("id", "send-to-vaadin")
                .waitForFirst().click();

        WebElement event = getVaadinPortletRootElementByStaticPart("vaadinipcportlet_WAR_liferayportlet30")
                .$("*").attribute("id", "response-from-plain-portlet").waitForFirst();
        Assert.assertEquals("foo", event.getText());
    }

    @Test
    public void sendEventFromVaadinToNonVaadin() {

        getVaadinPortletRootElementByStaticPart("vaadinipcportlet_WAR_liferayportlet30").$("*")
                .attribute("id", "send-to-plain").waitForFirst().click();

        WebElement event = findElement(By.id("response-from-vaadin"));
        Assert.assertEquals("baz", event.getText());
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/ipceventnotvaadin";
    }
}

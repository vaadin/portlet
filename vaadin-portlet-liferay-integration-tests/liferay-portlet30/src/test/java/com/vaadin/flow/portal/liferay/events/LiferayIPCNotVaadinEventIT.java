/*
 * Copyright 2000-2019 Vaadin Ltd.
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

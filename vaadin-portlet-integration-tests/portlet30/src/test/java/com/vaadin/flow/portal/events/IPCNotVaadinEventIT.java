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

        getVaadinPortletRootElement().$("*").first().$(ButtonElement.class)
                .attribute("id", "send-to-plain").waitForFirst().click();

        WebElement event = findElement(By.id("response-from-vaadin"));
        Assert.assertEquals("baz", event.getText());
    }
}

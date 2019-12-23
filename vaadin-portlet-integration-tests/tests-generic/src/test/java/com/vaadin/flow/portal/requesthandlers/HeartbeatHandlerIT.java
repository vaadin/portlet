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

package com.vaadin.flow.portal.requesthandlers;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.flow.portal.basic.BasicPortletContent;

public class HeartbeatHandlerIT extends AbstractPlutoPortalTest {
    public HeartbeatHandlerIT() {
        super("tests-generic", "basic");
    }

    @Test
    public void basicPortlet_afterFirstHeartbeatRequest_shouldWorkNormally()
            throws InterruptedException {
        ButtonElement buttonElement = getFirstPortlet().$(ButtonElement.class)
                .first();

        Thread.sleep(35000);

        // In this test, we make sure that Vaadin responds after the first
        // heartbeat request. If it doesn't respond, then the test will hang
        // because of waitForVaadin. That's why it is disabled here.
        // See https://github.com/vaadin/portlet/issues/166
        testBench().disableWaitForVaadin();
        try {
            buttonElement.click();
            DivElement greetingMessage = getFirstPortlet().$(DivElement.class)
                    .id(BasicPortletContent.GREETING_MESSAGE_ID);
            Assert.assertTrue(
                    greetingMessage.getText().startsWith("Hello from "));
        } finally {
            testBench().enableWaitForVaadin();
        }
    }
}

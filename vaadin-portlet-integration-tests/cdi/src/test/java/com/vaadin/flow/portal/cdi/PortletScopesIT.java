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
package com.vaadin.flow.portal.cdi;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class PortletScopesIT extends AbstractPlutoPortalTest {

    public PortletScopesIT() {
        super("cdi", "portlet-scopes");
    }

    @Test
    public void preDefinedRequestBeansReflectPortletState() {
        // initially expect normal window state and view mode
        waitUntil(driver -> WindowState.NORMAL.toString()
                .equals(getVaadinPortletRootElement().$("*")
                        .id(PortletScopesView.WINDOW_STATE_LABEL_ID).getText())
                && PortletMode.VIEW.toString()
                .equals(getVaadinPortletRootElement().$("*")
                        .id(PortletScopesView.PORTLET_MODE_LABEL_ID)
                        .getText()));

        // maximize and switch to edit mode
        setWindowStateInPortal(WindowState.MAXIMIZED);
        setPortletModeInPortal(PortletMode.EDIT);

        // then expect maximized window state and edit mode
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getVaadinPortletRootElement().$("*")
                        .id(PortletScopesView.WINDOW_STATE_LABEL_ID).getText())
                && PortletMode.EDIT.toString()
                .equals(getVaadinPortletRootElement().$("*")
                        .id(PortletScopesView.PORTLET_MODE_LABEL_ID)
                        .getText()));
    }

    @Test
    public void refreshCounterIsSessionScoped() {
        // store value session-scoped attach counter
        int counter1 = getAttachCounter();

        // open in a new window, store value session-scoped attach counter
        String firstWindow = driver.getWindowHandle();
        openInAnotherWindow();
        int counter2 = getAttachCounter();
        Assert.assertTrue(counter1 < counter2);

        // go back to first new, refresh, check counter increased
        driver.switchTo().window(firstWindow);
        driver.navigate().refresh();
        int counter3 = getAttachCounter();
        Assert.assertTrue(counter2 < counter3);
    }

    private int getAttachCounter() {
        return Integer.parseInt(getVaadinPortletRootElement().$("*").first()
                .$(SpanElement.class)
                .attribute("id", PortletScopesView.ATTACH_COUNTER_LABEL_ID)
                .waitForFirst().getText());
    }

}

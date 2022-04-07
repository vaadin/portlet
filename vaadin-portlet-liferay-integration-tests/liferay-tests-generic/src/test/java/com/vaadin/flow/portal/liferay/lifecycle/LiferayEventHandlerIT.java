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
package com.vaadin.flow.portal.liferay.lifecycle;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

@NotThreadSafe
public class LiferayEventHandlerIT extends AbstractLiferayPortalTest {

    @Test
    public void modeUpdatedInPortal_noWindowStateHandlerCalled() {
        setPortletModeInPortal(PortletMode.EDIT);
        waitUntil(driver -> PortletMode.EDIT.toString()
                .equals(getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals("",
                getLabelContent(LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID));
    }

    @Test
    public void windowStateUpdatedInPortal_noModeStateHandlerCalled() {
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals("",
                getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID));
    }

    @Test
    public void modeUpdatedInPortal_eventHandlerCalled() {
        // This test is neutral on whether an event for the initial mode is sent

        setPortletModeInPortal(PortletMode.EDIT);
        waitUntil(driver -> PortletMode.EDIT.toString()
                .equals(getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.EDIT.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());

        setPortletModeInPortal(PortletMode.HELP);
        waitUntil(driver -> PortletMode.HELP.toString()
                .equals(getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.HELP.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());

        setPortletModeInPortal(PortletMode.VIEW);
        waitUntil(driver -> PortletMode.VIEW.toString()
                .equals(getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.VIEW.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());
    }

    @Test
    public void windowStateChangedInPortal_eventHandlerCalled() {
        // This test is neutral on whether an event for the initial state is
        // sent
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals(WindowState.MAXIMIZED.toString(),
                getVaadinPortletRootElement().$("*").id("request-state-info")
                        .getText());

        setWindowStateInPortal(WindowState.NORMAL);
        waitUntil(driver -> WindowState.NORMAL.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals(WindowState.NORMAL.toString(),
                getVaadinPortletRootElement().$("*").id("request-state-info")
                        .getText());

        setWindowStateInPortal(WindowState.MINIMIZED);
        waitUntil(driver -> getVaadinPortletRootElements().isEmpty());
    }

    @Test
    public void windowStateAndModeChangedInPortal_portletStateIsPreservedtOnRefresh() {
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));

        driver.navigate().refresh();

        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));
    }

    @Test
    public void windowStateAndModeChangedInPortal_portletsOnDifferentTabsReceiveEventsIndependently() {
        String firstTab = driver.getWindowHandle();

        String secondTab = openInAnotherWindow();
        setWindowStateInPortal(WindowState.MAXIMIZED);
        setPortletModeInPortal(PortletMode.EDIT);

        driver.switchTo().window(firstTab);
        setWindowStateInPortal(WindowState.MINIMIZED);

        driver.switchTo().window(secondTab);
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));
        waitUntil(driver -> PortletMode.EDIT.toString()
                .equals(getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));

        driver.switchTo().window(firstTab);
        waitUntil(driver -> !$(TestBenchElement.class).attribute("id",
                LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID).exists());
        waitUntil(driver -> !$(TestBenchElement.class).attribute("id",
                LiferayEventHandlerContent.MODE_LABEL_ID).exists());
    }

    protected String getLabelContent(String id) {
        return getVaadinPortletRootElement().$("*").id(id).getText();
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/eventhandler";
    }
}

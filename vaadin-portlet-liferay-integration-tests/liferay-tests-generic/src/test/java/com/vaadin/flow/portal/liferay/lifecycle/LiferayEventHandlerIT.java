/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
        waitUntil(driver -> PortletMode.EDIT.toString().equals(
                getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals("", getLabelContent(
                LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID));
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
        waitUntil(driver -> PortletMode.EDIT.toString().equals(
                getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.EDIT.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());

        setPortletModeInPortal(PortletMode.HELP);
        waitUntil(driver -> PortletMode.HELP.toString().equals(
                getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.HELP.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());

        setPortletModeInPortal(PortletMode.VIEW);
        waitUntil(driver -> PortletMode.VIEW.toString().equals(
                getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));
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
        waitUntil(
                driver -> WindowState.NORMAL.toString().equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals(WindowState.NORMAL.toString(),
                getVaadinPortletRootElement().$("*").id("request-state-info")
                        .getText());

        setWindowStateInPortal(WindowState.MINIMIZED);
        waitUntil(driver -> getVaadinPortletRootElements().isEmpty());
    }

    @Test
    public void windowStateAndModeChangedInPortal_portletStateIsPreservedOnRefresh() {
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)));

        driver.navigate().refresh();

        Assert.assertTrue(waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals(getLabelContent(
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID))));
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
        waitUntil(driver -> PortletMode.EDIT.toString().equals(
                getLabelContent(LiferayEventHandlerContent.MODE_LABEL_ID)));

        driver.switchTo().window(firstTab);
        waitUntil(driver -> !$(TestBenchElement.class)
                .attribute("id",
                        LiferayEventHandlerContent.WINDOW_STATE_LABEL_ID)
                .exists());
        waitUntil(driver -> !$(TestBenchElement.class)
                .attribute("id", LiferayEventHandlerContent.MODE_LABEL_ID)
                .exists());
    }

    protected String getLabelContent(String id) {
        return getVaadinPortletRootElement().$("*").id(id).getText();
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/eventhandler";
    }
}

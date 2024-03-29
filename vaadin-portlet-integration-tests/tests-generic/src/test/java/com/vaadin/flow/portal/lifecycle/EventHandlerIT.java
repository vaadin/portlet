/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.lifecycle;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class EventHandlerIT extends AbstractPlutoPortalTest {

    public EventHandlerIT() {
        super("tests-generic", "eventhandler");
    }

    @Test
    public void modeUpdatedInPortal_noWindowStateHandlerCalled() {
        setPortletModeInPortal(PortletMode.EDIT);
        waitUntil(driver -> PortletMode.EDIT.toString()
                .equals(getLabelContent(EventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals("",
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID));
    }

    @Test
    public void windowStateUpdatedInPortal_noModeStateHandlerCalled() {
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals("",
                getLabelContent(EventHandlerContent.MODE_LABEL_ID));
    }

    @Test
    public void modeUpdatedInPortal_eventHandlerCalled() {
        // This test is neutral on whether an event for the initial mode is sent

        setPortletModeInPortal(PortletMode.EDIT);
        waitUntil(driver -> PortletMode.EDIT.toString()
                .equals(getLabelContent(EventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.EDIT.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());

        setPortletModeInPortal(PortletMode.HELP);
        waitUntil(driver -> PortletMode.HELP.toString()
                .equals(getLabelContent(EventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.HELP.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());

        setPortletModeInPortal(PortletMode.VIEW);
        waitUntil(driver -> PortletMode.VIEW.toString()
                .equals(getLabelContent(EventHandlerContent.MODE_LABEL_ID)));
        Assert.assertEquals(PortletMode.VIEW.toString(),
                getVaadinPortletRootElement().$("*").id("request-mode-info")
                        .getText());
    }

    @Test
    public void windowStateChangedInPortal_eventHandlerCalled() {
        // This test is neutral on whether an event for the initial state is
        // sent
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals(WindowState.MAXIMIZED.toString(),
                getVaadinPortletRootElement().$("*").id("request-state-info")
                        .getText());

        setWindowStateInPortal(WindowState.NORMAL);
        waitUntil(driver -> WindowState.NORMAL.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));
        Assert.assertEquals(WindowState.NORMAL.toString(),
                getVaadinPortletRootElement().$("*").id("request-state-info")
                        .getText());

        setWindowStateInPortal(WindowState.MINIMIZED);
        waitUntil(driver -> getVaadinPortletRootElements().isEmpty());
    }

    @Test
    public void windowStateAndModeChangedInPortal_portletStateIsPreservedtOnRefresh() {
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));

        driver.navigate().refresh();

        waitUntil(driver -> WindowState.MAXIMIZED.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));
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
        waitUntil(driver -> WindowState.MAXIMIZED.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));
        waitUntil(driver -> PortletMode.EDIT.toString()
                .equals(getLabelContent(EventHandlerContent.MODE_LABEL_ID)));

        driver.switchTo().window(firstTab);
        waitUntil(driver -> !$(TestBenchElement.class)
                .attribute("id", EventHandlerContent.WINDOW_STATE_LABEL_ID)
                .exists());
        waitUntil(driver -> !$(TestBenchElement.class)
                .attribute("id", EventHandlerContent.MODE_LABEL_ID).exists());
    }

    protected String getLabelContent(String id) {
        return waitUntil(driver -> getVaadinPortletRootElement().$("*").id(id))
                .getText();
    }
}

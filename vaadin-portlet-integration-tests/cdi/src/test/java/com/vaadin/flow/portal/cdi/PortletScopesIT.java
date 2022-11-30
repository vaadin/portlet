/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
        return Integer.parseInt(getVaadinPortletRootElement().$("*")
                .attribute("id", PortletScopesView.ATTACH_COUNTER_LABEL_ID)
                .waitForFirst().getText());
    }

}

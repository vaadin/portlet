/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class MinimizedStateRenderIT extends AbstractPlutoPortalTest {
    public MinimizedStateRenderIT() {
        super("tests-generic", "minimized-state-render");
    }

    @Test
    public void switchBetweenNormalAndMinimized() {
        clickButtonAndCheckState(MinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());
        clickButtonAndCheckState(MinimizedStateRenderView.NORMALIZE_BUTTON_ID,
                WindowState.NORMAL.toString());
    }

    @Test
    public void switchBetweenMaximizedAndMinimized() {
        clickButtonAndCheckState(MinimizedStateRenderView.MAXIMIZE_BUTTON_ID,
                WindowState.MAXIMIZED.toString());
        clickButtonAndCheckState(MinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());
        clickButtonAndCheckState(MinimizedStateRenderView.MAXIMIZE_BUTTON_ID,
                WindowState.MAXIMIZED.toString());
    }

    @Test
    public void switchBetweenViewAndEditModesInMinimizedState() {
        clickButtonAndCheckState(MinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());

        clickPortletModeChangeButtonAndCheckMode(PortletMode.EDIT.toString());
        clickPortletModeChangeButtonAndCheckMode(PortletMode.VIEW.toString());
    }

    private void clickPortletModeChangeButtonAndCheckMode(String mode) {
        TestBenchElement portletModeChangeButton = getVaadinPortletRootElement().$(
                "*").id(MinimizedStateRenderView.PORTLET_MODE_CHANGE);
        portletModeChangeButton.click();
        TestBenchElement modeInfo = getVaadinPortletRootElement().$("*")
                .id(MinimizedStateRenderView.MODE_INFO_ID);
        Assert.assertEquals(mode, modeInfo.getText());
    }

    private void clickButtonAndCheckState(String buttonId, String state) {
        TestBenchElement button = getVaadinPortletRootElement().$("*")
                .id(buttonId);

        button.click();

        TestBenchElement stateInfo = getVaadinPortletRootElement().$("*")
                .id(MinimizedStateRenderView.STATE_INFO_ID);
        Assert.assertEquals(state, stateInfo.getText());
    }
}

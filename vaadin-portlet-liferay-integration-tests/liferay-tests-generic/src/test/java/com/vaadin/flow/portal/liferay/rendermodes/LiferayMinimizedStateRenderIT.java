/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

@NotThreadSafe
public class LiferayMinimizedStateRenderIT extends AbstractLiferayPortalTest {

    @Test
    public void switchBetweenNormalAndMinimized() {
        clickButtonAndCheckState(LiferayMinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());
        clickButtonAndCheckState(LiferayMinimizedStateRenderView.NORMALIZE_BUTTON_ID,
                WindowState.NORMAL.toString());
    }

    @Test
    public void switchBetweenMaximizedAndMinimized() {
        clickButtonAndCheckState(LiferayMinimizedStateRenderView.MAXIMIZE_BUTTON_ID,
                WindowState.MAXIMIZED.toString());
        clickButtonAndCheckState(LiferayMinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());
        clickButtonAndCheckState(LiferayMinimizedStateRenderView.MAXIMIZE_BUTTON_ID,
                WindowState.MAXIMIZED.toString());
    }

    @Test
    public void switchBetweenViewAndEditModesInMinimizedState() {
        clickButtonAndCheckState(LiferayMinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());

        clickPortletModeChangeButtonAndCheckMode(PortletMode.EDIT.toString());
        clickPortletModeChangeButtonAndCheckMode(PortletMode.VIEW.toString());
    }

    private void clickPortletModeChangeButtonAndCheckMode(String mode) {
        TestBenchElement portletModeChangeButton = getVaadinPortletRootElement().$(
                "*").id(LiferayMinimizedStateRenderView.PORTLET_MODE_CHANGE);
        portletModeChangeButton.click();
        TestBenchElement modeInfo = getVaadinPortletRootElement().$("*")
                .id(LiferayMinimizedStateRenderView.MODE_INFO_ID);
        Assert.assertEquals(mode, modeInfo.getText());
    }

    private void clickButtonAndCheckState(String buttonId, String state) {
        TestBenchElement button = getVaadinPortletRootElement().$("*")
                .id(buttonId);

        button.click();

        TestBenchElement stateInfo = getVaadinPortletRootElement().$("*")
                .id(LiferayMinimizedStateRenderView.STATE_INFO_ID);
        Assert.assertEquals(state, stateInfo.getText());
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/minimized-state-render";
    }
}

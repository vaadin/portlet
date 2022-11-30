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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class HubRenderIT extends AbstractPlutoPortalTest {

    public HubRenderIT() {
        super("portlet30", "render");
    }

    @Test
    public void changeModeAndState_modeAndStateAreKept() {
        TestBenchElement stateChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.WINDOW_STATE_CHANGE);
        TestBenchElement modeChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());

        stateChange.click();

        waitForPageRefresh();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.PORTLET_MODE_CHANGE);

        WebElement stateInfo = getVaadinPortletRootElement().$("*")
                .id("state-info");
        Assert.assertEquals(WindowState.MAXIMIZED.toString(),
                stateInfo.getText());

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());
        Assert.assertEquals("VIEW", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        modeChange.click();

        waitForPageRefresh();

        stateChange = getVaadinPortletRootElement().$(ButtonElement.class)
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$(ButtonElement.class)
                .id(RenderView.PORTLET_MODE_CHANGE);

        WebElement modeInfo = getVaadinPortletRootElement().$("*")
                .id("mode-info");
        Assert.assertEquals(PortletMode.EDIT.toString(), modeInfo.getText());

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        stateChange.click();

        waitForPageRefresh();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.PORTLET_MODE_CHANGE);

        stateInfo = getVaadinPortletRootElement().$("*").id("state-info");
        Assert.assertEquals(WindowState.NORMAL.toString(), stateInfo.getText());

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertTrue(isNormalWindowState());
    }

    private void waitForPageRefresh() {
        // Wait for a moment so the page refresh is done before continuing
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getWindowMode() {
        SelectElement modeSelector = $(TestBenchElement.class).attribute("name",
                "modeSelectionForm").first().$(SelectElement.class).first();
        return modeSelector.getSelectedText().toUpperCase(Locale.ENGLISH);
    }

    private boolean isNormalWindowState() {
        return findElements(By.id("portlets-left-column")).size() > 0;
    }

}

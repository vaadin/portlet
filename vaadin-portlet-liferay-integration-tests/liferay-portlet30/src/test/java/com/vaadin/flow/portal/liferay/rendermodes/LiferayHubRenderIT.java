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
package com.vaadin.flow.portal.liferay.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class LiferayHubRenderIT extends AbstractLiferayPortalTest {

    @Test
    public void changeModeAndState_modeAndStateAreKept() {
        TestBenchElement stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        TestBenchElement modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_EDIT, modeChange.getText());

        stateChange.click();

        waitForPageRefresh();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        WebElement stateInfo = getVaadinPortletRootElement().$("*")
                .id("state-info");
        Assert.assertEquals(WindowState.MAXIMIZED.toString(),
                stateInfo.getText());

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_EDIT, modeChange.getText());
        Assert.assertEquals("VIEW", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        modeChange.click();

        waitForPageRefresh();

        stateChange = getVaadinPortletRootElement().$(ButtonElement.class)
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$(ButtonElement.class)
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        WebElement modeInfo = getVaadinPortletRootElement().$("*")
                .id("mode-info");
        Assert.assertEquals(PortletMode.EDIT.toString(), modeInfo.getText());

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        stateChange.click();

        waitForPageRefresh();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        stateInfo = getVaadinPortletRootElement().$("*").id("state-info");
        Assert.assertEquals(WindowState.NORMAL.toString(), stateInfo.getText());

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_VIEW, modeChange.getText());
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

    @Override
    protected String getFriendlyUrl() {
        return "test/hubrender";
    }
}

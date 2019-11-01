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
package com.vaadin.flow.portal.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class HubRenderIT extends AbstractPlutoPortalTest {

    public HubRenderIT() {
        super("render");
    }

    @Test
    public void changeModeAndState_modeAndStateAreKept() {
        ButtonElement stateChange = $(ButtonElement.class)
                .id(RenderView.WINDOW_STATE_CHANGE);
        ButtonElement modeChange = $(ButtonElement.class)
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());

        stateChange.click();

        waitUntil(ExpectedConditions.presenceOfElementLocated(
                By.id(RenderView.WINDOW_STATE_CHANGE)));

        stateChange = $(ButtonElement.class).id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = $(ButtonElement.class).id(RenderView.PORTLET_MODE_CHANGE);

        WebElement stateInfo = findElement(By.id("state-info"));
        Assert.assertEquals(WindowState.MAXIMIZED.toString(),
                stateInfo.getText());

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());
        Assert.assertEquals("VIEW", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        modeChange.click();

        waitUntil(ExpectedConditions.presenceOfElementLocated(
                By.id(RenderView.WINDOW_STATE_CHANGE)));

        stateChange = $(ButtonElement.class).id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = $(ButtonElement.class).id(RenderView.PORTLET_MODE_CHANGE);

        WebElement modeInfo = findElement(By.id("mode-info"));
        Assert.assertEquals(PortletMode.EDIT.toString(), modeInfo.getText());

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        stateChange.click();

        waitUntil(ExpectedConditions.presenceOfElementLocated(
                By.id(RenderView.WINDOW_STATE_CHANGE)));

        stateChange = $(ButtonElement.class).id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = $(ButtonElement.class).id(RenderView.PORTLET_MODE_CHANGE);

        stateInfo = findElement(By.id("state-info"));
        Assert.assertEquals(WindowState.NORMAL.toString(), stateInfo.getText());

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertTrue(isNormalWindowState());
    }

    private String getWindowMode() {
        SelectElement modeSelector = $(TestBenchElement.class)
                .attribute("name", "modeSelectionForm").first()
                .$(SelectElement.class).first();
        return modeSelector.getSelectedText().toUpperCase(Locale.ENGLISH);
    }

    private boolean isNormalWindowState() {
        return findElements(By.id("portlets-left-column")).size() > 0;
    }

}

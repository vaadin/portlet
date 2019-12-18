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

    public void switchBetweenViewAndEditModesInMinimizedState() {
        clickButtonAndCheckState(MinimizedStateRenderView.MINIMIZE_BUTTON_ID,
                WindowState.MINIMIZED.toString());

        clickPortletModeChangeButtonAndCheckMode(PortletMode.EDIT.toString());
        clickPortletModeChangeButtonAndCheckMode(PortletMode.VIEW.toString());
    }

    private void clickPortletModeChangeButtonAndCheckMode(String mode) {
        ButtonElement portletModeChangeButton = getFirstPortlet().$(ButtonElement.class)
                .id(MinimizedStateRenderView.PORTLET_MODE_CHANGE);
        portletModeChangeButton.click();
        TestBenchElement modeInfo = getFirstPortlet()
                .findElement(By.id(MinimizedStateRenderView.MODE_INFO_ID));
        Assert.assertEquals(mode, modeInfo.getText());
    }

    private void clickButtonAndCheckState(String buttonId, String state) {
        ButtonElement button = getFirstPortlet().$(ButtonElement.class)
                .id(buttonId);

        button.click();

        TestBenchElement stateInfo = getFirstPortlet()
                .findElement(By.id(MinimizedStateRenderView.STATE_INFO_ID));
        Assert.assertEquals(state, stateInfo.getText());
    }
}

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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

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

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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class RenderIT extends AbstractPlutoPortalTest {

    public RenderIT() {
        super("tests-generic", "render");
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

        stateChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());
        Assert.assertEquals("VIEW", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        modeChange.click();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertFalse(isNormalWindowState());

        stateChange.click();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowMode());
        Assert.assertTrue(isNormalWindowState());
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

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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;

public class RenderIT extends AbstractPlutoPortalTest {

    public RenderIT() {
        super("render");
    }

    @Test
    public void changeModeAndState_modeAndStateAreKept() {
        ButtonElement stateChange = $(ButtonElement.class)
                .id(RenderView.WINDOW_STATE_CHANGE);
        ButtonElement modeChange = $(ButtonElement.class)
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());

        stateChange.click();

        stateChange = $(ButtonElement.class)
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = $(ButtonElement.class)
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_VIEW, modeChange.getText());

        modeChange.click();

        stateChange = $(ButtonElement.class)
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = $(ButtonElement.class)
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());

        stateChange.click();

        stateChange = $(ButtonElement.class)
                .id(RenderView.WINDOW_STATE_CHANGE);
        modeChange = $(ButtonElement.class)
                .id(RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(RenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(RenderView.MODE_EDIT, modeChange.getText());
    }

}
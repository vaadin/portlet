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
package com.vaadin.flow.portal.handler;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.junit.Test;

import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class EventHandlerIT extends AbstractPlutoPortalTest {

    public EventHandlerIT() {
        super("eventhandler");
    }

    @Test
    public void testModeChange() {
        // This test is neutral on whether an event for the initial mode is sent

        setPortletModeInPortal(PortletMode.EDIT);
        waitUntil(driver -> PortletMode.EDIT.toString().equals(
                getLabelContent(EventHandlerContent.MODE_LABEL_ID)));

        setPortletModeInPortal(PortletMode.HELP);
        waitUntil(driver -> PortletMode.HELP.toString().equals(
                getLabelContent(EventHandlerContent.MODE_LABEL_ID)));

        setPortletModeInPortal(PortletMode.VIEW);
        waitUntil(driver -> PortletMode.VIEW.toString().equals(
                getLabelContent(EventHandlerContent.MODE_LABEL_ID)));
    }

    @Test
    public void testWindowStateChange() {
        // This test is neutral on whether an event for the initial state is
        // sent
        setWindowStateInPortal(WindowState.MAXIMIZED);
        waitUntil(driver -> WindowState.MAXIMIZED.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));

        setWindowStateInPortal(WindowState.NORMAL);
        waitUntil(driver -> WindowState.NORMAL.toString().equals(
                getLabelContent(EventHandlerContent.WINDOW_STATE_LABEL_ID)));

        setWindowStateInPortal(WindowState.MINIMIZED);
        waitUntil(driver -> !$(TestBenchElement.class)
                .attribute("id", EventHandlerContent.WINDOW_STATE_LABEL_ID)
                .exists());
    }

    protected String getLabelContent(String id) {
        return $(TestBenchElement.class).id(id).getText();
    }

}
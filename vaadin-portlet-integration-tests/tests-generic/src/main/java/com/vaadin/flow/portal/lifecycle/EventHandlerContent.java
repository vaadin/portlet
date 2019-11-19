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
package com.vaadin.flow.portal.lifecycle;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.portal.VaadinPortletRequest;

public class EventHandlerContent extends Div
        implements PortletModeHandler, WindowStateHandler {

    static final String MODE_LABEL_ID = "mode_label_id";
    static final String WINDOW_STATE_LABEL_ID = "window_state_label_id";

    private final Span modeLabel;
    private final Span windowStateLabel;

    private Div requestStateInfo;
    private Div requestModeInfo;

    public EventHandlerContent() {
        modeLabel = new Span();
        modeLabel.setId(MODE_LABEL_ID);
        add(modeLabel);

        windowStateLabel = new Span();
        windowStateLabel.setId(WINDOW_STATE_LABEL_ID);
        add(windowStateLabel);

        requestStateInfo = new Div();
        requestStateInfo.setId("request-state-info");
        requestModeInfo = new Div();
        requestModeInfo.setId("request-mode-info");
        add(requestStateInfo, requestModeInfo);
    }

    @Override
    public void portletModeChange(PortletModeEvent event) {
        modeLabel.setText(event.getPortletMode().toString());
        requestModeInfo.setText(
                VaadinPortletRequest.getCurrent().getPortletMode().toString());
    }

    @Override
    public void windowStateChange(WindowStateEvent event) {
        windowStateLabel.setText(event.getWindowState().toString());
        requestStateInfo.setText(
                VaadinPortletRequest.getCurrent().getWindowState().toString());
    }
}

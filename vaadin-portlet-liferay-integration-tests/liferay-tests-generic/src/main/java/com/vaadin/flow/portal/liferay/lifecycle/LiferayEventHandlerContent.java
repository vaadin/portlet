/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay.lifecycle;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.portal.VaadinPortletRequest;
import com.vaadin.flow.portal.lifecycle.PortletModeEvent;
import com.vaadin.flow.portal.lifecycle.PortletModeHandler;
import com.vaadin.flow.portal.lifecycle.WindowStateEvent;
import com.vaadin.flow.portal.lifecycle.WindowStateHandler;

public class LiferayEventHandlerContent extends Div
        implements PortletModeHandler, WindowStateHandler {

    static final String MODE_LABEL_ID = "mode_label_id";
    static final String WINDOW_STATE_LABEL_ID = "window_state_label_id";

    private final Span modeLabel;
    private final Span windowStateLabel;

    private Div requestStateInfo;
    private Div requestModeInfo;

    public LiferayEventHandlerContent() {
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

/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;

public class LiferayRenderView extends VerticalLayout implements PortletView {

    public static final String STATE_MAXIMIZE = "Maximize";
    public static final String STATE_NORMALIZE = "Normalize";
    public static final String MODE_VIEW = "To view mode";
    public static final String MODE_EDIT = "To edit mode";
    public static final String WINDOW_STATE_CHANGE = "window-state-change";
    public static final String PORTLET_MODE_CHANGE = "portlet-mode-change";

    private Button windowState;
    private Button portletMode;

    private PortletViewContext context;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.context = context;
        windowState = new Button(
                WindowState.NORMAL.equals(context.getWindowState())
                        ? STATE_MAXIMIZE
                        : STATE_NORMALIZE,
                event -> switchWindowState());
        windowState.setId(WINDOW_STATE_CHANGE);

        portletMode = new Button(
                PortletMode.EDIT.equals(context.getPortletMode()) ? MODE_VIEW
                        : MODE_EDIT,
                event -> switchPortletMode());
        portletMode.setId(PORTLET_MODE_CHANGE);

        add(windowState, portletMode);
    }

    private void switchWindowState() {
        if (WindowState.NORMAL.equals(context.getWindowState())) {
            context.setWindowState(WindowState.MAXIMIZED);
            windowState.setText(STATE_NORMALIZE);
        } else if (WindowState.MAXIMIZED.equals(context.getWindowState())) {
            context.setWindowState(WindowState.NORMAL);
            windowState.setText(STATE_MAXIMIZE);
        }
    }

    private void switchPortletMode() {
        if (PortletMode.EDIT.equals(context.getPortletMode())) {
            context.setPortletMode(PortletMode.VIEW);
            portletMode.setText(MODE_EDIT);
        } else {
            context.setPortletMode(PortletMode.EDIT);
            portletMode.setText(MODE_VIEW);
        }
    }
}

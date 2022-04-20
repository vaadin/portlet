/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;

public class LiferayMinimizedStateRenderView extends VerticalLayout
        implements PortletView {

    public static final String STATE_MINIMIZE = "Minimize";
    public static final String STATE_NORMALIZE = "Normalize";
    public static final String STATE_MAXIMIZE = "Maximize";
    public static final String MINIMIZE_BUTTON_ID = "minimize-button";
    public static final String NORMALIZE_BUTTON_ID = "normalize-button";
    public static final String MAXIMIZE_BUTTON_ID = "maximize-button";
    public static final String MODE_VIEW = "To view mode";
    public static final String MODE_EDIT = "To edit mode";
    public static final String PORTLET_MODE_CHANGE = "portlet-mode-change";
    public static final String STATE_INFO_ID = "state-info";
    public static final String MODE_INFO_ID = "mode-info";

    private Button portletMode;

    private PortletViewContext context;

    @Override
    public void onPortletViewContextInit(PortletViewContext context) {
        this.context = context;
        Button minimize = new Button(STATE_MINIMIZE, event -> minimize());
        minimize.setId(MINIMIZE_BUTTON_ID);
        Button normalize = new Button(STATE_NORMALIZE, event -> normalize());
        normalize.setId(NORMALIZE_BUTTON_ID);
        Button maximize = new Button(STATE_MAXIMIZE, event -> maximize());
        maximize.setId(MAXIMIZE_BUTTON_ID);

        portletMode = new Button(
                PortletMode.EDIT.equals(context.getPortletMode()) ? MODE_VIEW
                        : MODE_EDIT,
                event -> switchPortletMode());
        portletMode.setId(PORTLET_MODE_CHANGE);

        Div stateInfo = new Div();
        stateInfo.setId(STATE_INFO_ID);
        Div modeInfo = new Div();
        modeInfo.setId(MODE_INFO_ID);

        context.addPortletModeChangeListener(
                event -> modeInfo.setText(event.getPortletMode().toString()));
        context.addWindowStateChangeListener(
                event -> stateInfo.setText(event.getWindowState().toString()));

        add(minimize, normalize, maximize, portletMode, stateInfo, modeInfo);
    }

    private void minimize() {
        context.setWindowState(WindowState.MINIMIZED);
    }

    private void normalize() {
        context.setWindowState(WindowState.NORMAL);
    }

    private void maximize() {
        context.setWindowState(WindowState.MAXIMIZED);
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

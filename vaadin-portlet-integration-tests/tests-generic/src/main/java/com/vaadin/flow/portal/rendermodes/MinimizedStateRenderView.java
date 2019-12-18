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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.portal.PortletView;
import com.vaadin.flow.portal.PortletViewContext;

public class MinimizedStateRenderView extends VerticalLayout
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
    private Div stateInfo;
    private Div modeInfo;

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

        stateInfo = new Div();
        stateInfo.setId(STATE_INFO_ID);
        modeInfo = new Div();
        modeInfo.setId(MODE_INFO_ID);

        context.addPortletModeChangeListener(
                event -> modeInfo.setText(event.getPortletMode().toString()));
        context.addWindowStateChangeListener(
                event -> stateInfo.setText(event.getWindowState().toString()));

        add(minimize, normalize, maximize, portletMode, stateInfo);
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

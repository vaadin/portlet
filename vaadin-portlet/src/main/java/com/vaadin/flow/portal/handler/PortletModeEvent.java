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

/**
 * An event of this class is fired when the {@link PortletMode} of the portlet
 * is updated to a mode different from its current mode.
 */
public class PortletModeEvent {

    private final PortletMode portletMode;
    private final PortletMode prevPortletMode;

    /**
     * Creates a new event.
     *
     * @param portletMode
     *            the updated portlet mode
     */
    public PortletModeEvent(PortletMode portletMode,
            PortletMode prevPortletMode) {
        this.portletMode = portletMode;
        this.prevPortletMode = prevPortletMode;
    }

    /**
     * The new {@link PortletMode} of the portlet.
     *
     * @return the update portlet mode
     */
    public PortletMode getPortletMode() {
        return portletMode;
    }

    /**
     * Whether the portlet is in view ({@link PortletMode#VIEW}) mode.
     *
     * @return true iff the portlet is in view mode
     */
    public boolean isViewMode() {
        return PortletMode.VIEW.equals(portletMode);
    }

    /**
     * Whether the portlet is in edit ({@link PortletMode#EDIT}) mode.
     *
     * @return true iff the portlet is in edit mode
     */
    public boolean isEditMode() {
        return PortletMode.EDIT.equals(portletMode);
    }

    /**
     * Whether the portlet is in help ({@link PortletMode#HELP}) mode.
     *
     * @return true iff the portlet is in help mode
     */
    public boolean isHelpMode() {
        return PortletMode.HELP.equals(portletMode);
    }

    /**
     * The {@link PortletMode} of the portlet just before the update that
     * triggered this event.
     *
     * @return the previous window state.
     */
    public PortletMode getPreviousPortletMode() {
        return prevPortletMode;
    }

}

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.lifecycle;

import javax.portlet.PortletMode;
import java.io.Serializable;

/**
 * An event of this class is fired when the {@link PortletMode} of the portlet
 * is updated to a mode different from its current mode.
 *
 * @author Vaadin Ltd
 * @since
 */
public class PortletModeEvent implements Serializable {
    private final String portletMode;
    private final String prevPortletMode;

    private final boolean fromClient;

    /**
     * Creates a new event.
     *
     * @param newMode
     *            the updated portlet mode
     * @param oldMode
     *            previous mode value
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public PortletModeEvent(PortletMode newMode, PortletMode oldMode,
            boolean fromClient) {
        this.portletMode = newMode != null ? newMode.toString() : null;
        this.prevPortletMode = oldMode != null ? oldMode.toString() : null;
        this.fromClient = fromClient;
    }

    /**
     * The new {@link PortletMode} of the portlet.
     *
     * @return the update portlet mode
     */
    public PortletMode getPortletMode() {
        return new PortletMode(portletMode);
    }

    /**
     * Whether the portlet is in view ({@link PortletMode#VIEW}) mode.
     *
     * @return true iff the portlet is in view mode
     */
    public boolean isViewMode() {
        return PortletMode.VIEW.toString().equals(portletMode);
    }

    /**
     * Whether the portlet is in edit ({@link PortletMode#EDIT}) mode.
     *
     * @return true iff the portlet is in edit mode
     */
    public boolean isEditMode() {
        return PortletMode.EDIT.toString().equals(portletMode);
    }

    /**
     * Whether the portlet is in help ({@link PortletMode#HELP}) mode.
     *
     * @return true iff the portlet is in help mode
     */
    public boolean isHelpMode() {
        return PortletMode.HELP.toString().equals(portletMode);
    }

    /**
     * The {@link PortletMode} of the portlet just before the update that
     * triggered this event.
     *
     * @return the previous window state.
     */
    public PortletMode getPreviousPortletMode() {
        return new PortletMode(prevPortletMode);
    }

    /**
     * Checks if this event originated from the client side.
     *
     * @return <code>true</code> if the event originated from the client side,
     *         <code>false</code> otherwise
     */
    public boolean isFromClient() {
        return fromClient;
    }

}

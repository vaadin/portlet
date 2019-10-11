package com.vaadin.flow.portal.handler;

import javax.portlet.PortletMode;

public class PortletModeEvent {

    private final PortletMode portletMode;

    public PortletModeEvent(PortletMode portletMode) {
        this.portletMode = portletMode;
    }

    public PortletMode getPortletMode() {
        return portletMode;
    }

    public boolean isEditMode() {
        return PortletMode.EDIT.equals(portletMode);
    }
}

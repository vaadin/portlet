/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.lifecycle;

import java.io.Serializable;

/**
 * A listener for portlet events.
 *
 * @see com.vaadin.flow.portal.PortletViewContext
 * @see com.vaadin.flow.portal.lifecycle.EventHandler
 *
 * @author Vaadin Ltd
 * @since
 *
 */
@FunctionalInterface
public interface PortletEventListener extends Serializable {

    /**
     * This method gets called when an IPC event is received.
     *
     * @param event
     */
    void onPortletEvent(PortletEvent event);
}

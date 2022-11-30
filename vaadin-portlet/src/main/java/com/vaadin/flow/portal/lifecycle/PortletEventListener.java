/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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

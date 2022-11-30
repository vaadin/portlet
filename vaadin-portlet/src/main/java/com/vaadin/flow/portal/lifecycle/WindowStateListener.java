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
 * A listener for window state change events.
 *
 * @see com.vaadin.flow.portal.PortletViewContext
 * @see com.vaadin.flow.portal.lifecycle.WindowStateHandler
 *
 * @author Vaadin Ltd
 * @since
 * 
 */
@FunctionalInterface
public interface WindowStateListener extends Serializable {

    /**
     * Invoked when the window state changes.
     *
     * @param event
     *            the event object
     */
    void windowStateChange(WindowStateEvent event);
}

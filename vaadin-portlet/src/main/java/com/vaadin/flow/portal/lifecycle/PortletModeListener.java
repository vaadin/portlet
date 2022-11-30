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

import java.io.Serializable;

/**
 * A listener portlet mode change events.
 *
 * @see com.vaadin.flow.portal.PortletViewContext
 * @see com.vaadin.flow.portal.lifecycle.PortletModeHandler
 * @author Vaadin Ltd
 * @since
 */
public interface PortletModeListener extends Serializable {

    /**
     * Invoked when the portlet mode changes.
     *
     * @param event
     *            the event object
     */
    void portletModeChange(PortletModeEvent event);
}

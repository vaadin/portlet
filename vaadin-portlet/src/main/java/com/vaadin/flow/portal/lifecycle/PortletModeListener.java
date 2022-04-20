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

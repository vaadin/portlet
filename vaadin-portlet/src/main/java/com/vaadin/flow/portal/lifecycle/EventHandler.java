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
 * Component that implements this interface and is the view given as type
 * parameter for {@link com.vaadin.flow.portal.VaadinPortlet} will receive
 * <i>all</i> events sent using the Portlet Hub, including events sent via
 * {@link com.vaadin.flow.portal.PortletViewContext#fireEvent(String, java.util.Map)}.
 *
 * @author Vaadin Ltd
 * @since
 */
@FunctionalInterface
public interface EventHandler extends Serializable {

    /**
     * This method gets called when an IPC event is received.
     *
     * @param event
     */
    void handleEvent(PortletEvent event);
}

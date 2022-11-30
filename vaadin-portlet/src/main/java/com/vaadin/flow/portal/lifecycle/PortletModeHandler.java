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
 * Add this interface to a {@link com.vaadin.flow.portal.VaadinPortlet} view
 * (the {@link com.vaadin.flow.component.Component} subclass passed for the type
 * parameter {@code C}) to handle changes in {@link javax.portlet.PortletMode}.
 *
 * @see PortletModeListener
 * @author Vaadin Ltd
 * @since
 */
@FunctionalInterface
public interface PortletModeHandler extends Serializable {

    /**
     * Invoked when the portlet mode changes.
     *
     * @param event
     *            the wevent object
     */
    void portletModeChange(PortletModeEvent event);

}

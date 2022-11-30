/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import java.io.Serializable;

/**
 * Provides a context to for portlet instance's related actions (like fire an
 * event, registering listeners, updating modes).
 *
 * @see PortletViewContext
 * @author Vaadin Ltd
 * @since
 *
 */
public interface PortletView extends Serializable {

    /**
     * This method gets called once for a portlet component if it implements
     * this interface.
     * <p>
     * Implement the interface and the method to be able to store the
     * {@code context} object and use it to invoke contextual methods.
     *
     * @param context
     *            a portlet context
     */
    void onPortletViewContextInit(PortletViewContext context);
}

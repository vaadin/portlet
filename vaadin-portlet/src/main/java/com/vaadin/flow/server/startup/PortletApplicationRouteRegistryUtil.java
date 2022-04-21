/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.server.startup;

import com.vaadin.flow.portal.VaadinPortletContext;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry.ApplicationRouteRegistryWrapper;

public class PortletApplicationRouteRegistryUtil {

    /**
     * Gets the route registry for the given Vaadin context. If the Vaadin
     * context has no route registry, a new instance is created and assigned to
     * the context.
     *
     * @param context
     *            the vaadin context for which to get a route registry, not
     *            <code>null</code>
     * @return a registry instance for the given servlet context, not
     *         <code>null</code>
     */
    public static ApplicationRouteRegistry getInstance(
            VaadinPortletContext context) {
        assert context != null;

        ApplicationRouteRegistryWrapper attribute;
        synchronized (context) {
            attribute = context
                    .getAttribute(ApplicationRouteRegistryWrapper.class);

            if (attribute == null) {
                attribute = new ApplicationRouteRegistryWrapper(
                        createRegistry(context));
                context.setAttribute(attribute);
            }
        }

        return attribute.getRegistry();
    }

    private static ApplicationRouteRegistry createRegistry(
            VaadinContext context) {
        return new ApplicationRouteRegistry(context);
    }
}

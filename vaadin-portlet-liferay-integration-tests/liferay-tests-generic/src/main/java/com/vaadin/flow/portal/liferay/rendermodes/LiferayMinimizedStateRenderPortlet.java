/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import jakarta.portlet.Portlet;
import jakarta.portlet.annotations.Dependency;
import jakarta.portlet.annotations.PortletConfiguration;

import com.vaadin.flow.portal.VaadinLiferayPortlet;

@PortletConfiguration(
        portletName = "LiferayMinimizedStateRenderPortlet",
        dependencies =
        @Dependency(name = "PortletHub", scope = "jakarta.portlet", version = "3.0.0")
)
public class LiferayMinimizedStateRenderPortlet
        extends VaadinLiferayPortlet<LiferayMinimizedStateRenderView>
        implements Portlet {
    @Override
    protected boolean shouldRenderMinimized() {
        return true;
    }
}

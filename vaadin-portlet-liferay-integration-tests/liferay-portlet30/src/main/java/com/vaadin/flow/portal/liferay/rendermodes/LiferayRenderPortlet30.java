/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import javax.portlet.annotations.Dependency;
import javax.portlet.annotations.PortletConfiguration;

import com.vaadin.flow.portal.VaadinLiferayPortlet;

@PortletConfiguration(
        portletName = "LiferayRenderPortlet30", publicParams = "param",
        dependencies = @Dependency(name = "PortletHub", scope = "javax.portlet",
                version = "3.0.0"))
public class LiferayRenderPortlet30 extends VaadinLiferayPortlet<LiferayPortlet30RenderView> {
}

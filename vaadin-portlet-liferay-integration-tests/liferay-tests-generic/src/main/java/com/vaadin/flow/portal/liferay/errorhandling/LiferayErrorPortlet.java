/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay.errorhandling;

import javax.portlet.annotations.Dependency;
import javax.portlet.annotations.PortletConfiguration;

import com.vaadin.flow.portal.VaadinLiferayPortlet;

@PortletConfiguration(
        portletName = "LiferayBasicPortlet",
        dependencies =
        @Dependency(name = "PortletHub", scope = "javax.portlet", version = "3.0.0")
)
public class LiferayErrorPortlet extends VaadinLiferayPortlet<LiferayErrorPortletContent> {
}

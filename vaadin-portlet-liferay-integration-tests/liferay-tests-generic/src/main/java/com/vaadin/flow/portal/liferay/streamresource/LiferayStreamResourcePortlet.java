package com.vaadin.flow.portal.liferay.streamresource;

import javax.portlet.annotations.Dependency;
import javax.portlet.annotations.PortletConfiguration;

import com.vaadin.flow.portal.VaadinLiferayPortlet;

@PortletConfiguration(
        portletName = "LiferayStreamResourcePortlet",
        dependencies =
        @Dependency(name = "PortletHub", scope = "javax.portlet", version = "3.0.0")
)
public class LiferayStreamResourcePortlet
        extends VaadinLiferayPortlet<LiferayStreamResourceContent> {
}

package com.vaadin.flow.portal.liferay;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

public class LayoutInfo implements Serializable {
    private final String name;
    private final Collection<PortletInfo> portletInfos;
    private final String friendlyUrl;

    public LayoutInfo(String name, String friendlyUrl) {
        this.name = name;
        this.friendlyUrl = friendlyUrl;
        portletInfos = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public String getFriendlyUrl() {
        return friendlyUrl;
    }

    public void addPortlet(PortletInfo portletInfo) {
        portletInfos.add(portletInfo);
    }

    public Collection<PortletInfo> getPortletInfos() {
        return portletInfos;
    }

    public boolean isEmpty() {
        return portletInfos.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LayoutInfo that = (LayoutInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(portletInfos, that.portletInfos) && Objects.equals(friendlyUrl, that.friendlyUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, portletInfos, friendlyUrl);
    }
}

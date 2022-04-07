package com.vaadin.flow.portal.liferay;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PortletInfo implements Serializable {
    private String id;
    private String name;
    private String friendlyUrl;
    private final Map<String, String> preferences;

    public PortletInfo() {
        preferences = new HashMap<>();
    }

    public void setPortletName(String name) {
        this.name = name;
    }

    public String getPortletName() {
        return name;
    }

    public void setPortletFriendlyUrl(String friendlyUrl) {
        this.friendlyUrl = friendlyUrl;
    }

    public String getPortletFriendlyUrl() {
        return friendlyUrl;
    }

    public void addPortletPreference(String key, String value) {
        getPortletPreferences().put(key, value);
    }

    public String getPortletPreference(String key) {
        return getPortletPreferences().get(key);
    }

    public Map<String, String> getPortletPreferences() {
        return preferences;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PortletInfo that = (PortletInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
                && Objects.equals(friendlyUrl, that.friendlyUrl)
                && Objects.equals(preferences, that.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, friendlyUrl, preferences);
    }
}

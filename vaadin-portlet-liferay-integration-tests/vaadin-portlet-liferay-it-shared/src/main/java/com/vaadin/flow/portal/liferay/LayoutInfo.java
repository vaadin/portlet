/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LayoutInfo that = (LayoutInfo) o;
        return Objects.equals(name, that.name)
                && Objects.equals(portletInfos, that.portletInfos)
                && Objects.equals(friendlyUrl, that.friendlyUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, portletInfos, friendlyUrl);
    }
}

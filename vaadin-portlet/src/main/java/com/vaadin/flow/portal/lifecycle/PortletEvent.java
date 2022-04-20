/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.lifecycle;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic IPC event.
 *
 * @author Vaadin Ltd
 * @since
 *
 */
public class PortletEvent implements Serializable {

    private final String eventName;
    private final Map<String, String[]> parameters;

    /**
     * Creates a new event instance using {@code eventName} and
     * {@code parameters}.
     *
     * @param eventName
     *            an event name
     * @param parameters
     *            event parameters
     */
    public PortletEvent(String eventName, Map<String, String[]> parameters) {
        this.eventName = eventName;
        this.parameters = new HashMap<>(parameters);
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, String[]> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}

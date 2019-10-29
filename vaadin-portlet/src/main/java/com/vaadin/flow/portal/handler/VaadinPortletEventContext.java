/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.portal.handler;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import java.io.Serializable;
import java.util.Map;

import com.vaadin.flow.portal.VaadinPortlet;
import com.vaadin.flow.shared.Registration;

/**
 * A portlet event context object allows to fire and send portlet events.
 *
 * @author Vaadin Ltd
 * @since
 *
 * @see VaadinPortletEventView#onPortletEventContextInit(VaadinPortletEventContext)
 *
 */
public interface VaadinPortletEventContext extends Serializable {

    /**
     * Fires an event with the given {@code parameters} and {@code eventName}.
     * <p>
     * Any such event will be sent to the server as an action event for any
     * {@link VaadinPortlet}. Such event will be handled by the
     * {@link VaadinPortlet#processAction(ActionRequest, ActionResponse)}
     * method.
     * <p>
     * By default {@link VaadinPortlet} calls
     * {@link EventHandler#handleEvent(PortletEvent)} method on portlet
     * component if it implements {@link EventHandler} interface.
     *
     * @param eventName
     *            an event name
     * @param parameters
     *            parameters to add to event action
     */
    void fireEvent(String eventName, Map<String, String> parameters);

    /**
     * Adds a listener which will receive any {@link PortletEvent}.
     *
     * @see #addEventChangeListener(String, PortletEventChangeListener)
     * @see EventHandler
     *
     * @param listener
     *            a portlet event listener, not {@code null}
     * @return an event registration handle for removing the listener
     */
    Registration addGenericEventChangeListener(PortletEventChangeListener listener);

    /**
     * Adds a listener which will receive only events with the given
     * {@code eventType}.
     * <p>
     * {@code eventType} can be a regular expression, e.g.
     * {@code "^myCompany\..*"}. registers a listener for all event types
     * beginning with {@code "myCompany."}.
     *
     * @see #addGenericEventChangeListener(PortletEventChangeListener)
     *
     * @param eventType
     *            an event type to listen
     * @param listener
     *            a portlet event listener, not {@code null}
     * @return an event registration handle for removing the listener
     */
    Registration addEventChangeListener(String eventType,
            PortletEventChangeListener listener);

    /**
     * Adds a window state listener.
     *
     * @see WindowStateChangeHandler
     *
     * @param listener
     *            a window state listener, not {@code null}
     * @return a registration handle for removing the listener
     */
    Registration addWindowStateChangeListener(WindowStateChangeListener listener);

    /**
     * Adds a portlet mode listener.
     *
     * @see PortletModeChangeHandler
     *
     * @param listener
     *            a portlet mode listener, not {@code null}
     * @return a registration handle for removing the listener
     */
    Registration addPortletModeChangeListener(PortletModeChangeListener listener);

}

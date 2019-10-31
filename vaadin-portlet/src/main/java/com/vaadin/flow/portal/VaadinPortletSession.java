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
package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.StateAwareResponse;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.internal.PortletStreamResourceRegistry;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.shared.Registration;

/**
 * An implementation of {@link VaadinSession} for JSR-286 portlet environments.
 *
 * Only the documented parts of this class should be considered as stable public
 * API.
 *
 * Note also that some methods and/or nested interfaces might move to
 * {@link VaadinPortletService} in future minor or major versions of Vaadin. In
 * these cases, a deprecated redirection for backwards compatibility will be
 * used in VaadinPortletSession for a transition period.
 *
 * @since 7.0
 */
@SuppressWarnings("serial")
public class VaadinPortletSession extends VaadinSession {

    private final Map<String, QName> eventActionDestinationMap = new HashMap<>();

    private final Map<String, String> sharedParameterActionNameMap = new HashMap<>();

    /**
     * Create a portlet service session for the given portlet service.
     *
     * @param service
     *         the portlet service to which the new session belongs
     */
    public VaadinPortletSession(VaadinPortletService service) {
        super(service);
    }

    @Override
    protected StreamResourceRegistry createStreamResourceRegistry() {
        return new PortletStreamResourceRegistry(this);
    }

    /**
     * Returns the underlying portlet session.
     *
     * @return portlet session
     */
    public PortletSession getPortletSession() {
        WrappedSession wrappedSession = getSession();
        PortletSession session = ((WrappedPortletSession) wrappedSession)
                .getPortletSession();
        return session;
    }

    private PortletResponse getCurrentResponse() {
        VaadinPortletResponse currentResponse = (VaadinPortletResponse) CurrentInstance
                .get(VaadinResponse.class);

        if (currentResponse != null) {
            return currentResponse.getPortletResponse();
        } else {
            return null;
        }
    }

    /**
     * Returns the JSR-286 portlet configuration that provides access to the
     * portlet context and init parameters.
     *
     * @return portlet configuration
     */
    public PortletConfig getPortletConfig() {
        VaadinPortletResponse response = (VaadinPortletResponse) CurrentInstance
                .get(VaadinResponse.class);
        return response.getService().getPortlet().getPortletConfig();
    }

    /**
     * Creates a new action URL.
     *
     * Creating an action URL is only supported when processing a suitable
     * request (render or resource request, including normal Vaadin UIDL
     * processing) and will return null if not processing a suitable request.
     *
     * @param action
     *         the action parameter (javax.portlet.action parameter value in
     *         JSR-286)
     * @return action URL or null if called outside a MimeRequest (outside a
     * UIDL request or similar)
     */
    public PortletURL generateActionURL(String action) {
        PortletURL url = null;
        PortletResponse response = getCurrentResponse();
        if (response instanceof MimeResponse) {
            url = ((MimeResponse) response).createActionURL();
            url.setParameter("javax.portlet.action", action);
        } else {
            return null;
        }
        return url;
    }

    /**
     * Sends a portlet event to the indicated destination.
     *
     * Internally, an action may be created and opened, as an event cannot be
     * sent directly from all types of requests.
     *
     * Sending portlet events from background threads is not supported.
     *
     * The event destinations and values need to be kept in the context until
     * sent. Any memory leaks if the action fails are limited to the session.
     *
     * Event names for events sent and received by a portlet need to be declared
     * in portlet.xml .
     *
     * @param uI
     *         a window in which a temporary action URL can be opened if
     *         necessary
     * @param name
     *         event name
     * @param value
     *         event value object that is Serializable and, if appropriate,
     *         has a valid JAXB annotation
     */
    public void sendPortletEvent(UI uI, QName name, Serializable value)
            throws IllegalStateException {
        PortletResponse response = getCurrentResponse();
        if (response instanceof MimeResponse) {
            String actionKey = "" + System.currentTimeMillis();
            while (eventActionDestinationMap.containsKey(actionKey)) {
                actionKey += ".";
            }
            PortletURL actionUrl = generateActionURL(actionKey);
            if (actionUrl != null) {
                eventActionDestinationMap.put(actionKey, name);
                uI.getPage().setLocation(actionUrl.toString());
            } else {
                // this should never happen as we already know the response is a
                // MimeResponse
                throw new IllegalStateException(
                        "Portlet events can only be sent from a portlet request");
            }
        } else if (response instanceof StateAwareResponse) {
            ((StateAwareResponse) response).setEvent(name, value);
        } else {
            throw new IllegalStateException(
                    "Portlet events can only be sent from a portlet request");
        }
    }

    /**
     * Sets a shared portlet parameter.
     *
     * Internally, an action may be created and opened, as shared parameters
     * cannot be set directly from all types of requests.
     *
     * Setting shared render parameters from background threads is not
     * supported.
     *
     * The parameters and values need to be kept in the context until sent. Any
     * memory leaks if the action fails are limited to the session.
     *
     * Shared parameters set or read by a portlet need to be declared in
     * portlet.xml .
     *
     * @param uI
     *         a window in which a temporary action URL can be opened if
     *         necessary
     * @param name
     *         parameter identifier
     * @param value
     *         parameter value
     */
    public void setSharedRenderParameter(UI uI, String name, String value)
            throws IllegalStateException {
        PortletResponse response = getCurrentResponse();
        if (response instanceof MimeResponse) {
            String actionKey = "" + System.currentTimeMillis();
            while (sharedParameterActionNameMap.containsKey(actionKey)) {
                actionKey += ".";
            }
            PortletURL actionUrl = generateActionURL(actionKey);
            if (actionUrl != null) {
                sharedParameterActionNameMap.put(actionKey, name);
                uI.getPage().setLocation(actionUrl.toString());
            } else {
                // this should never happen as we already know the response is a
                // MimeResponse
                throw new IllegalStateException(
                        "Shared parameters can only be set from a portlet request");
            }
        } else if (response instanceof StateAwareResponse) {
            ((StateAwareResponse) response).setRenderParameter(name, value);
        } else {
            throw new IllegalStateException(
                    "Shared parameters can only be set from a portlet request");
        }
    }

    /**
     * Sets the portlet mode. This may trigger a new render request.
     *
     * Currently, this is only supported when working with a
     * {@link StateAwareResponse} (an action request or an event request).
     * Portlet mode change in background threads is not supported.
     *
     * Portlet modes used by a portlet need to be declared in portlet.xml .
     *
     * @param uI
     *         a window in which the render URL can be opened if necessary
     * @param portletMode
     *         the portlet mode to switch to
     * @throws PortletModeException
     *         if the portlet mode is not allowed for some reason
     *         (configuration, permissions etc.)
     * @throws IllegalStateException
     *         if not processing a request of the correct type
     */
    public void setPortletMode(UI uI, PortletMode portletMode)
            throws IllegalStateException, PortletModeException {
        PortletResponse response = getCurrentResponse();
        if (response instanceof MimeResponse) {
            PortletURL url = ((MimeResponse) response).createRenderURL();
            url.setPortletMode(portletMode);
            throw new IllegalStateException(
                    "Portlet mode change is currently only supported when processing event and action requests");
            // UI.open(new ExternalResource(url.toString()));
        } else if (response instanceof StateAwareResponse) {
            ((StateAwareResponse) response).setPortletMode(portletMode);
        } else {
            throw new IllegalStateException(
                    "Portlet mode can only be changed from a portlet request");
        }
    }

    /**
     * Gets the currently used session.
     *
     * @return the current session instance if available, otherwise
     *         <code>null</code>
     */
    public static VaadinPortletSession getCurrent() {
        return (VaadinPortletSession) VaadinSession.getCurrent();
    }
}

/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.portlet.ActionURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.portal.lifecycle.EventHandler;
import com.vaadin.flow.portal.lifecycle.PortletEvent;
import com.vaadin.flow.portal.lifecycle.PortletEventListener;
import com.vaadin.flow.portal.lifecycle.PortletModeEvent;
import com.vaadin.flow.portal.lifecycle.PortletModeHandler;
import com.vaadin.flow.portal.lifecycle.PortletModeListener;
import com.vaadin.flow.portal.lifecycle.WindowStateEvent;
import com.vaadin.flow.portal.lifecycle.WindowStateHandler;
import com.vaadin.flow.portal.lifecycle.WindowStateListener;
import com.vaadin.flow.shared.Registration;

/**
 * For internal use only.
 *
 * @param <C>
 */
public final class PortletViewContext<C extends Component> implements Serializable {

    private final C view;

    private final AtomicBoolean isPortlet3;

    private final AtomicLong nextUid = new AtomicLong();

    private final Map<String, Pair<String, PortletEventListener>> eventListeners = new HashMap<>();

    private final Collection<WindowStateListener> windowStateListeners = new CopyOnWriteArrayList<>();

    private final Collection<PortletModeListener> portletModeListeners = new CopyOnWriteArrayList<>();

    private String portletMode;

    private String windowState;


    PortletViewContext(C view, AtomicBoolean portlet3,
                       PortletMode portletMode, WindowState windowState) {

        this.view = view;
        isPortlet3 = portlet3;
        this.portletMode = portletMode.toString();
        this.windowState = windowState.toString();

        if (view instanceof EventHandler) {
            EventHandler handler = (EventHandler) view;
            doAddGenericEventListener(handler::handleEvent);
        }
        if (view instanceof WindowStateHandler) {
            doAddWindowStateChangeListener(
                    ((WindowStateHandler) view)::windowStateChange);
        }
        if (view instanceof PortletModeHandler) {
            doAddPortletModeChangeListener(
                    ((PortletModeHandler) view)::portletModeChange);
        }
    }

    /**
     * (Re)initializes the context.
     */
    void init() {
        eventListeners.forEach((uid, pair) -> registerEventChangeListener(uid,
                pair.getFirst(), pair.getSecond()));
    }

    /**
     * Fires an event with the given {@code parameters} and {@code eventName}.
     * <p>
     * Any such event will be sent to the server as an action event for any
     * {@link VaadinPortlet}. Such event will be handled by the
     * {@link VaadinPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)}
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
    public void fireEvent(String eventName, Map<String, String> parameters) {
        executeJS(getFireEventScript(eventName, parameters));
    }

    /**
     * Adds a listener which will receive any {@link PortletEvent}.
     *
     * @see #addEventChangeListener(String, PortletEventListener)
     * @see EventHandler
     *
     * @param listener
     *            a portlet event listener, not {@code null}
     * @return an event registration handle for removing the listener
     */
    public Registration addGenericEventListener(PortletEventListener listener) {
        return doAddEventChangeListener(".*", listener);
    }

    /**
     * Adds a listener which will receive only events with the given
     * {@code eventType}.
     * <p>
     * {@code eventType} can be a regular expression, e.g.
     * {@code "^myCompany\..*"}. registers a listener for all event types
     * beginning with {@code "myCompany."}.
     *
     * @see #addGenericEventListener(PortletEventListener)
     *
     * @param eventType
     *            an event type to listen
     * @param listener
     *            a portlet event listener, not {@code null}
     * @return an event registration handle for removing the listener
     */
    public Registration addEventChangeListener(String eventType,
            PortletEventListener listener) {
        return doAddEventChangeListener(eventType, listener);
    }

    /**
     * Adds a window state listener.
     *
     * @see WindowStateHandler
     *
     * @param listener
     *            a window state listener, not {@code null}
     * @return a registration handle for removing the listener
     */
    public Registration addWindowStateChangeListener(
            WindowStateListener listener) {
        return doAddWindowStateChangeListener(listener);
    }

    /**
     * Adds a portlet mode listener.
     *
     * @see PortletModeHandler
     *
     * @param listener
     *            a portlet mode listener, not {@code null}
     * @return a registration handle for removing the listener
     */
    public Registration addPortletModeChangeListener(
            PortletModeListener listener) {
        return doAddPortletModeChangeListener(listener);
    }

    /**
     * Get the window state for the portlet instance represented by the context.
     *
     * @return window state
     */
    public WindowState getWindowState() {
        return new WindowState(windowState);
    }

    /**
     * Get the portlet mode for the portlet instance represented by the context.
     *
     * @return portlet mode
     */
    public PortletMode getPortletMode() {
        return new PortletMode(portletMode);
    }

    /**
     * Set a new window state for the portlet instance represented by the
     * context.
     *
     * @param newWindowState
     *            window state to set
     */
    public void setWindowState(WindowState newWindowState) {
        if (isPortlet3.get()) {
            updatePortletState(newWindowState.toString(),
                    getPortletMode().toString());
        } else {
            stateChangeAction(newWindowState, getPortletMode());
        }
        if (!windowState.equals(newWindowState.toString())) {
            WindowState oldValue = getWindowState();
            windowState = newWindowState.toString();
            fireWindowStateEvent(
                    new WindowStateEvent(newWindowState, oldValue, false));
        }
    }

    /**
     * Set a new portlet mode for the portlet instance represented by the
     * context.
     *
     * @param newPortletMode
     *            portlet mode to set
     */
    public void setPortletMode(PortletMode newPortletMode) {
        if (isPortlet3.get()) {
            updatePortletState(getWindowState().toString(),
                    newPortletMode.toString());
        } else {
            stateChangeAction(getWindowState(), newPortletMode);
        }
        if (!portletMode.equals(newPortletMode.toString())) {
            PortletMode oldValue = getPortletMode();
            portletMode = newPortletMode.toString();
            firePortletModeEvent(
                    new PortletModeEvent(newPortletMode, oldValue, false));
        }
    }

    private void updatePortletState(String windowState, String portletMode) {
        String updateScript = getUpdatePortletStateScript(windowState,
                portletMode);
        executeJS(updateScript);
    }

    /**
     * Fires a window state change event.
     *
     * @param event
     *            a window state change event.
     */
    void fireWindowStateEvent(WindowStateEvent event) {
        windowStateListeners
                .forEach(listener -> listener.windowStateChange(event));
    }

    /**
     * Fires a portlet mode change event.
     *
     * @param event
     *            a portlet mode change event
     */
    void firePortletModeEvent(PortletModeEvent event) {
        portletModeListeners
                .forEach(listener -> listener.portletModeChange(event));
    }

    /**
     * Fires a portlet IPC event.
     *
     * @param event
     *            a portlet IPC event
     * @param uid
     *            the uid of listener which should be notified
     */
    void firePortletEvent(String uid, PortletEvent event) {
        assert view.getElement().getNode().isAttached();
        Pair<String, PortletEventListener> pair = eventListeners.get(uid);
        if (pair == null) {
            getLogger().error(
                    "{} is not found for the uid='{}', event '{}' is not delivered",
                    PortletEventListener.class.getSimpleName(), uid,
                    event.getEventName());
        } else {
            pair.getSecond().onPortletEvent(event);
        }
    }

    /**
     * Updates portlet mode and window state values.
     *
     * @param portletMode
     *            a portlet mode value
     * @param windowState
     *            a window state value
     */
    void updateModeAndState(PortletMode portletMode, WindowState windowState) {
        PortletMode oldMode = getPortletMode();
        this.portletMode = portletMode.toString();
        WindowState oldState = getWindowState();
        this.windowState = windowState.toString();
        /*
         * Note: mode update events must be sent to handlers before window state
         * update events.
         */
        if (!oldMode.equals(portletMode)) {
            firePortletModeEvent(
                    new PortletModeEvent(portletMode, oldMode, true));
        }
        if (!oldState.equals(windowState)) {
            fireWindowStateEvent(
                    new WindowStateEvent(windowState, oldState, true));
        }
    }

    private Registration doAddEventChangeListener(String eventType,
            PortletEventListener listener) {
        String uid = Long.toString(nextUid.getAndIncrement());
        String namespace = registerEventChangeListener(uid, eventType,
                listener);
        eventListeners.put(uid, new Pair<>(eventType, listener));
        return () -> {
            eventListeners.remove(uid);
            executeJS("window.Vaadin.Flow.Portlets[$0].unregisterListener($1);",
                    namespace, uid);
        };
    }

    private Registration doAddWindowStateChangeListener(
            WindowStateListener listener) {
        windowStateListeners.add(Objects.requireNonNull(listener));
        return () -> windowStateListeners.remove(listener);
    }

    private Registration doAddPortletModeChangeListener(
            PortletModeListener listener) {
        portletModeListeners.add(Objects.requireNonNull(listener));
        return () -> portletModeListeners.remove(listener);
    }

    private Registration doAddGenericEventListener(
            PortletEventListener listener) {
        return doAddEventChangeListener(".*", listener);
    }
    
    private void stateChangeAction(WindowState state, PortletMode mode) {
        PortletResponse response = VaadinPortletResponse
                .getCurrentPortletResponse();
        if (response instanceof MimeResponse) {
            ActionURL actionURL = ((MimeResponse) response).createActionURL();
            try {
                actionURL.setPortletMode(mode);
            } catch (PortletModeException e) {
                getLogger().error("unable to create portlet mode action URL",
                        e);
            }
            try {
                actionURL.setWindowState(state);
            } catch (WindowStateException e) {
                getLogger().error("unable to create window state action URL",
                        e);
            }
            String stateChangeScript = String.format("location.href = '%s'",
                    actionURL);
            executeJS(stateChangeScript);
        }
    }

    private String registerEventChangeListener(String uid, String eventType,
            PortletEventListener listener) {
        Objects.requireNonNull(listener);

        String namespace = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();

        executeJS("window.Vaadin.Flow.Portlets[$0].registerListener($1, $2);",
                namespace, eventType, uid);
        return namespace;
    }

    /**
     * Executes JavaScript only if the {@code view} is attached. Normally
     * JavaScript executions are queued to wait for attach event but we don't
     * want that - the portlet is either present or it is not.
     * 
     * @param script
     *            JavaScript string
     * @param params
     *            Parameters
     * @see com.vaadin.flow.dom.Element#executeJs(String,
     *      java.io.Serializable...) for more information
     */
    private void executeJS(String script, Serializable... params) {
        if (view != null && view.getElement().getNode().isAttached()
                && script != null && !script.isEmpty()) {
            view.getElement().executeJs(script, params);
        }
    }

    private static String escape(String str) {
        return str.replaceAll("([\\\\'])", "\\\\$1");
    }
    
    /**
     * Get JavaScript string for getting the portlet hub registration object.
     *
     * @return registration object stored as 'hub'
     */
    private static String getPortletHubRegistrationScript() {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();
        return String.format("var hub = window.Vaadin.Flow.Portlets['%s'].hub;",
                portletRegistryName);
    }

    /**
     * Get the script to update the portlet state with the given windowState 
     * and portletMode.
     *
     * @param windowState
     *            window state to send
     * @param portletMode
     *            portlet mode to send
     */
    private static String getUpdatePortletStateScript(String windowState,
            String portletMode) {
        return getPortletHubRegistrationScript() 
                + "var state = hub.newState();"
                + String.format("state.windowState = '%s';", windowState)
                + String.format("state.portletMode = '%s';", portletMode)
                + "hub.setRenderState(state);" 
                + getReloadPollingScript();
    }

    /**
     * Get the script to fire a Portlet Hub event
     * 
     * @param eventName
     *            Event name
     * @param parameters
     *            Event parameters
     * @return event firing script
     */
    private static String getFireEventScript(String eventName,
            Map<String, String> parameters) {
        StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append(getPortletHubRegistrationScript());
        eventBuilder.append("var params = hub.newParameters();");
        eventBuilder.append("params['action'] = ['send'];");
        parameters.forEach((key, value) -> eventBuilder.append(String
                .format("params['%s'] = ['%s'];", escape(key), escape(value))));
        eventBuilder.append(String.format(
                "hub.dispatchClientEvent('%s', params);", escape(eventName)));

        return eventBuilder.toString();
    }

    /**
     * Script that will handle page reload for page when hub has completed.
     *
     * @return portlet reload polling script
     */
    private static String getReloadPollingScript() {
        return "var poller = function () {"
                + "  if(hub.isInProgress()) {"
                + "    setTimeout(poller, 10);"
                + "  } else {"
                + "    location.reload();"
                + "  }"
                + "};"
                + "poller();";
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(PortletViewContext.class);
    }

}

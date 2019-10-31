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

import javax.portlet.ActionURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.portal.handler.EventHandler;
import com.vaadin.flow.portal.handler.PortletEvent;
import com.vaadin.flow.portal.handler.PortletEventListener;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.PortletModeListener;
import com.vaadin.flow.portal.handler.PortletViewContext;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;
import com.vaadin.flow.portal.handler.WindowStateListener;
import com.vaadin.flow.portal.util.PortletHubUtil;
import com.vaadin.flow.shared.Registration;

class PortletViewContextImpl<C extends Component>
        implements PortletViewContext {

    private final C view;

    private final AtomicBoolean isPortlet3;

    private final AtomicLong nextUid = new AtomicLong();

    private final Map<String, Pair<String, PortletEventListener>> eventListeners = new HashMap<>();

    private final Collection<WindowStateListener> windowStateListeners = new CopyOnWriteArrayList<>();

    private final Collection<PortletModeListener> portletModeListeners = new CopyOnWriteArrayList<>();

    private PortletMode portletMode = null;

    private WindowState windowState = null;

    PortletViewContextImpl(C view, AtomicBoolean portlet3) {
        this.view = view;
        isPortlet3 = portlet3;

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

    @Override
    public void fireEvent(String eventName, Map<String, String> parameters) {
        StringBuilder eventBuilder = new StringBuilder();
        eventBuilder.append(PortletHubUtil.getHubString());
        eventBuilder.append("var params = hub.newParameters();");
        eventBuilder.append("params['action'] = ['send'];");
        parameters.forEach((key, value) -> eventBuilder.append(String
                .format("params['%s'] = ['%s'];", escape(key), escape(value))));
        eventBuilder.append(String.format(
                "hub.dispatchClientEvent('%s', params);", escape(eventName)));

        view.getElement().executeJs(eventBuilder.toString());
    }

    @Override
    public Registration addGenericEventListener(PortletEventListener listener) {
        return doAddEventChangeListener(".*", listener);
    }

    @Override
    public Registration addEventChangeListener(String eventType,
            PortletEventListener listener) {
        return doAddEventChangeListener(eventType, listener);
    }

    @Override
    public Registration addWindowStateChangeListener(
            WindowStateListener listener) {
        return doAddWindowStateChangeListener(listener);
    }

    @Override
    public Registration addPortletModeChangeListener(
            PortletModeListener listener) {
        return doAddPortletModeChangeListener(listener);
    }

    /**
     * Get the window state for this portlet.
     *
     * @return window state
     */
    @Override
    public WindowState getWindowState() {
        return VaadinPortletRequest.getCurrent().getWindowState();
    }

    /**
     * Get the portlet mode for this portlet.
     *
     * @return portlet mode
     */
    @Override
    public PortletMode getPortletMode() {
        return VaadinPortletRequest.getCurrent().getPortletMode();
    }

    /**
     * Set a new window state for this portlet
     *
     * @param newWindowState
     *            window state to set
     */
    @Override
    public void setWindowState(WindowState newWindowState) {
        if (isPortlet3.get()) {
            PortletHubUtil.updatePortletState(newWindowState.toString(),
                    getPortletMode().toString());
        } else {
            stateChangeAction(newWindowState, getPortletMode());
        }
    }

    /**
     * Set a new portlet mode for this portlet.
     *
     * @param newPortletMode
     *            portlet mode to set
     */
    @Override
    public void setPortletMode(PortletMode newPortletMode) {
        if (isPortlet3.get()) {
            PortletHubUtil.updatePortletState(getWindowState().toString(),
                    newPortletMode.toString());
        } else {
            stateChangeAction(getWindowState(), newPortletMode);
        }
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
            LoggerFactory.getLogger(VaadinPortlet.class).error(
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
        PortletMode oldMode = this.portletMode;
        this.portletMode = portletMode;
        WindowState oldState = this.windowState;
        this.windowState = windowState;
        /*
         * Note: mode update events must be sent to handlers before window state
         * update events.
         */
        if (!Objects.equals(portletMode, oldMode)) {
            firePortletModeEvent(new PortletModeEvent(portletMode, oldMode));
        }
        if (!Objects.equals(windowState, oldState)) {
            fireWindowStateEvent(new WindowStateEvent(windowState, oldState));
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
            view.getElement().executeJs(
                    "window.Vaadin.Flow.Portlets[$0].unregisterListener($1);",
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

    private String escape(String str) {
        return str.replaceAll("([\\\\'])", "\\\\$1");
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
            UI.getCurrent().getPage().executeJs(stateChangeScript);
        }
    }

    private String registerEventChangeListener(String uid, String eventType,
            PortletEventListener listener) {
        Objects.requireNonNull(listener);

        String namespace = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();

        view.getElement().executeJs(
                "window.Vaadin.Flow.Portlets[$0].registerListener($1, $2);",
                namespace, eventType, uid);
        return namespace;
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(PortletViewContextImpl.class);
    }

}
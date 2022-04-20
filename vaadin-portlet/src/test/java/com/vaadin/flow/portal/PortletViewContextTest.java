/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.portlet.PortletMode;
import javax.portlet.PortletResponse;
import javax.portlet.WindowState;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.lifecycle.EventHandler;
import com.vaadin.flow.portal.lifecycle.PortletEvent;
import com.vaadin.flow.portal.lifecycle.PortletModeEvent;
import com.vaadin.flow.portal.lifecycle.PortletModeHandler;
import com.vaadin.flow.portal.lifecycle.WindowStateEvent;
import com.vaadin.flow.portal.lifecycle.WindowStateHandler;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.shared.Registration;

public class PortletViewContextTest {

    public static class TestComponent extends Div
            implements EventHandler, PortletModeHandler, WindowStateHandler {

        private WindowStateEvent stateEvent;

        private PortletModeEvent modeEvent;

        private PortletEvent portletEvent;

        @Override
        public void windowStateChange(WindowStateEvent event) {
            stateEvent = event;
        }

        @Override
        public void portletModeChange(PortletModeEvent event) {
            modeEvent = event;
        }

        @Override
        public void handleEvent(PortletEvent event) {
            portletEvent = event;
        }

    }

    private VaadinPortletService service;
    private VaadinPortletSession session;
    private UI ui = new UI();
    private String namespace = "namespace-foo";
    private VaadinPortletResponse response;
    private VaadinPortletRequest request;

    @Before
    public void setUp() throws SessionExpiredException {
        service = Mockito.mock(VaadinPortletService.class);

        session = new VaadinPortletSession(service) {
            @Override
            public boolean hasLock() {
                return true;
            }

            @Override
            public void lock() {
            }

            @Override
            public void unlock() {
            }
        };

        ui.getInternals().setSession(session);
        UI.setCurrent(ui);

        VaadinPortletService.setCurrent(service);

        response = Mockito.mock(VaadinPortletResponse.class);
        CurrentInstance.set(VaadinResponse.class, response);
        PortletResponse portletResponse = Mockito.mock(PortletResponse.class);
        Mockito.when(response.getPortletResponse()).thenReturn(portletResponse);

        Mockito.when(portletResponse.getNamespace()).thenReturn(namespace);

        request = Mockito.mock(VaadinPortletRequest.class);
        CurrentInstance.set(VaadinRequest.class, request);
        Mockito.when(request.getPortletMode()).thenReturn(PortletMode.VIEW);
        Mockito.when(request.getWindowState()).thenReturn(WindowState.NORMAL);
    }

    @After
    public void tearDown() {
        CurrentInstance.clearAll();
    }

    @Test
    public void registerEventHandlerInCtor_handlerIsCalledOnEvent() {
        TestComponent component = new TestComponent();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        String uid = assertJsHubRegistration(".*");

        PortletEvent event = Mockito.mock(PortletEvent.class);
        context.firePortletEvent(uid, event);

        Assert.assertSame(event, component.portletEvent);
    }

    @Test
    public void registerPortletModeHandlerInCtor_handlerIsCalledOnEvent() {
        TestComponent component = new TestComponent();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        PortletModeEvent event = Mockito.mock(PortletModeEvent.class);
        context.firePortletModeEvent(event);

        Assert.assertSame(event, component.modeEvent);
    }

    @Test
    public void registerWindowStateHandlerInCtor_handlerIsCalledOnEvent() {
        TestComponent component = new TestComponent();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        WindowStateEvent event = Mockito.mock(WindowStateEvent.class);
        context.fireWindowStateEvent(event);

        Assert.assertSame(event, component.stateEvent);
    }

    @Test
    public void addPortletModeListener_listenerIsCalledOnEvent() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<PortletModeEvent> listener = new AtomicReference<>();
        context.addPortletModeChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        PortletModeEvent event = Mockito.mock(PortletModeEvent.class);
        context.firePortletModeEvent(event);

        Assert.assertSame(event, listener.get());
    }

    @Test
    public void addWindowStateListener_listenerIsCalledOnEvent() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<WindowStateEvent> listener = new AtomicReference<>();
        context.addWindowStateChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        WindowStateEvent event = Mockito.mock(WindowStateEvent.class);
        context.fireWindowStateEvent(event);

        Assert.assertSame(event, listener.get());
    }

    @Test
    public void updateModeAndState_listenerIsNotCalledOnInitialChangeAndIfNoStateChange() {
        Div component = new Div();
        ui.add(component);

        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.EDIT,
                WindowState.NORMAL);

        AtomicReference<WindowStateEvent> windowListener = new AtomicReference<>();
        context.addWindowStateChangeListener(
                event -> Assert.assertNull(windowListener.getAndSet(event)));

        AtomicReference<PortletModeEvent> portletListener = new AtomicReference<>();
        context.addPortletModeChangeListener(
                event -> Assert.assertNull(portletListener.getAndSet(event)));

        context.updateModeAndState(PortletMode.VIEW, WindowState.MAXIMIZED);

        Assert.assertEquals(WindowState.MAXIMIZED,
                windowListener.get().getWindowState());

        Assert.assertEquals(PortletMode.VIEW,
                portletListener.get().getPortletMode());

        // listeners doesn't throw because they have not been called (they
        // already has a value)
        context.updateModeAndState(PortletMode.VIEW, WindowState.MAXIMIZED);
    }

    @Test
    public void updateModeAndState_eventsAreFromClient() {
        Div component = new Div();
        ui.add(component);

        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.MAXIMIZED);

        AtomicReference<WindowStateEvent> windowListener = new AtomicReference<>();
        context.addWindowStateChangeListener(
                event -> Assert.assertNull(windowListener.getAndSet(event)));

        AtomicReference<PortletModeEvent> portletListener = new AtomicReference<>();
        context.addPortletModeChangeListener(
                event -> Assert.assertNull(portletListener.getAndSet(event)));

        context.updateModeAndState(PortletMode.EDIT, WindowState.NORMAL);

        Assert.assertTrue(windowListener.get().isFromClient());

        Assert.assertTrue(portletListener.get().isFromClient());
    }

    @Test
    public void setWindowState_portlet2_serverEventIsFired() {
        setWindowState_serverEventIsFired(false);
    }

    @Test
    public void setWindowState_portlet3_serverEventIsFired() {
        setWindowState_serverEventIsFired(true);
    }

    @Test
    public void portletMode_portlet2_serverEventIsFired() {
        portletMode_serverEventIsFired(false);
    }

    @Test
    public void portletMode_portlet3_serverEventIsFired() {
        portletMode_serverEventIsFired(false);
    }

    @Test
    public void addRemovePortletModeListener_listenerIsNotCalledAfterRemoval() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<PortletModeEvent> listener = new AtomicReference<>();
        Registration registration = context.addPortletModeChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        registration.remove();
        PortletModeEvent event = Mockito.mock(PortletModeEvent.class);
        context.firePortletModeEvent(event);

        Assert.assertNull(listener.get());
    }

    @Test
    public void addRemoveWindowStateListener_listenerIsNotCalledAfterRemoval() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<WindowStateEvent> listener = new AtomicReference<>();
        Registration registration = context.addWindowStateChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        registration.remove();
        WindowStateEvent event = Mockito.mock(WindowStateEvent.class);
        context.fireWindowStateEvent(event);

        Assert.assertNull(listener.get());
    }

    @Test
    public void addPortletEventListener_listenerIsCalledOnEvent() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<PortletEvent> listener = new AtomicReference<>();
        context.addEventChangeListener("bar",
                event -> Assert.assertNull(listener.getAndSet(event)));

        String uid = assertJsHubRegistration("bar");

        PortletEvent event = Mockito.mock(PortletEvent.class);
        context.firePortletEvent(uid, event);

        Assert.assertSame(event, listener.get());
    }

    @Test
    public void addRemovePortletEventListener_listenerIsNotCalledAfterRemoval() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<PortletEvent> listener = new AtomicReference<>();
        Registration registration = context.addEventChangeListener("bar",
                event -> Assert.assertNull(listener.getAndSet(event)));

        String uid = assertJsHubRegistration("bar");

        registration.remove();

        PortletEvent event = Mockito.mock(PortletEvent.class);
        context.firePortletEvent(uid, event);

        Assert.assertNull(listener.get());
    }

    @Test
    public void reinitAfteraddPortletEventListener_jsRegistrationIsRedone() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        context.addEventChangeListener("bar", event -> {
        });

        String uid = assertJsHubRegistration("bar");

        context.init();

        Assert.assertEquals(uid, assertJsHubRegistration("bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void fireEventInPortlet20Mode_exceptionIsRaised() {
        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(false), PortletMode.VIEW,
                WindowState.NORMAL);
        context.fireEvent("test", Collections.emptyMap());
    }

    @Test
    public void getWindowState_returnInitialWindowState() {
        Div component = new Div();
        ui.add(component);

        Mockito.when(request.getWindowState()).thenReturn(WindowState.NORMAL);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        Assert.assertEquals(WindowState.NORMAL, context.getWindowState());
    }

    @Test
    public void getWindowState_updateState_returnUpdatedWindowState() {
        Div component = new Div();
        ui.add(component);

        Mockito.when(request.getWindowState()).thenReturn(WindowState.NORMAL);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        context.setWindowState(WindowState.MAXIMIZED);

        Assert.assertEquals(WindowState.MAXIMIZED, context.getWindowState());
    }

    @Test
    public void getPortletMode_returnInitialPortletMode() {
        Div component = new Div();
        ui.add(component);

        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        Assert.assertEquals(PortletMode.VIEW, context.getPortletMode());
    }

    @Test
    public void getPortletMode_updateMode_returnUpdatedModeState() {
        Div component = new Div();
        ui.add(component);

        Mockito.when(request.getPortletMode()).thenReturn(PortletMode.EDIT);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(true), PortletMode.VIEW,
                WindowState.NORMAL);

        context.setPortletMode(PortletMode.VIEW);
        Assert.assertEquals(PortletMode.VIEW, context.getPortletMode());
    }

    private String assertJsHubRegistration(String event) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        PendingJavaScriptInvocation invocation = ui.getInternals()
                .dumpPendingJavaScriptInvocations().get(0);
        String expression = invocation.getInvocation().getExpression();
        Assert.assertThat(expression,
                Matchers.containsString("].registerListener"));
        // the first param is namespace
        Assert.assertEquals(namespace,
                invocation.getInvocation().getParameters().get(0));
        // the second param is event type
        Assert.assertEquals(event,
                invocation.getInvocation().getParameters().get(1));
        String uid = invocation.getInvocation().getParameters().get(2)
                .toString();
        return uid;
    }

    private void setWindowState_serverEventIsFired(boolean portlet3) {
        Mockito.when(request.getPortletMode()).thenReturn(PortletMode.VIEW);

        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(portlet3), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<WindowStateEvent> windowListener = new AtomicReference<>();
        context.addWindowStateChangeListener(
                event -> Assert.assertNull(windowListener.getAndSet(event)));

        context.setWindowState(WindowState.MAXIMIZED);
        Assert.assertFalse(windowListener.get().isFromClient());
        Assert.assertEquals(WindowState.MAXIMIZED,
                windowListener.get().getWindowState());
    }

    private void portletMode_serverEventIsFired(boolean portlet3) {
        Mockito.when(request.getWindowState()).thenReturn(WindowState.NORMAL);

        Div component = new Div();
        ui.add(component);
        PortletViewContext context = new PortletViewContext(
                component, new AtomicBoolean(portlet3), PortletMode.VIEW,
                WindowState.NORMAL);

        AtomicReference<PortletModeEvent> listener = new AtomicReference<>();
        context.addPortletModeChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        context.setPortletMode(PortletMode.EDIT);
        Assert.assertFalse(listener.get().isFromClient());
        Assert.assertEquals(PortletMode.EDIT, listener.get().getPortletMode());
    }
}

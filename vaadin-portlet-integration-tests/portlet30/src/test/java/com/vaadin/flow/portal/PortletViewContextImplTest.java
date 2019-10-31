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

import javax.portlet.PortletResponse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.handler.EventHandler;
import com.vaadin.flow.portal.handler.PortletEvent;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.shared.Registration;

public class PortletViewContextImplTest {

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

        VaadinPortletService.setCurrent(service);

        VaadinPortletResponse response = Mockito
                .mock(VaadinPortletResponse.class);
        CurrentInstance.set(VaadinResponse.class, response);
        PortletResponse portletResponse = Mockito.mock(PortletResponse.class);
        Mockito.when(response.getPortletResponse()).thenReturn(portletResponse);

        Mockito.when(portletResponse.getNamespace()).thenReturn(namespace);

    }

    @After
    public void tearDown() {
        CurrentInstance.clearAll();
    }

    @Test
    public void registerEventHandlerInCtor_handlerIsCalledOnEvent() {
        TestComponent component = new TestComponent();
        ui.add(component);
        PortletViewContextImpl<TestComponent> context = new PortletViewContextImpl<TestComponent>(
                component, new AtomicBoolean());

        String uid = assertJsHubRegistration(".*");

        PortletEvent event = Mockito.mock(PortletEvent.class);
        context.firePortletEvent(uid, event);

        Assert.assertSame(event, component.portletEvent);
    }

    @Test
    public void registerPortletModeHandlerInCtor_handlerIsCalledOnEvent() {
        TestComponent component = new TestComponent();
        ui.add(component);
        PortletViewContextImpl<TestComponent> context = new PortletViewContextImpl<TestComponent>(
                component, new AtomicBoolean());

        PortletModeEvent event = Mockito.mock(PortletModeEvent.class);
        context.firePortletModeEvent(event);

        Assert.assertSame(event, component.modeEvent);
    }

    @Test
    public void registerWindowStateHandlerInCtor_handlerIsCalledOnEvent() {
        TestComponent component = new TestComponent();
        ui.add(component);
        PortletViewContextImpl<TestComponent> context = new PortletViewContextImpl<TestComponent>(
                component, new AtomicBoolean());

        WindowStateEvent event = Mockito.mock(WindowStateEvent.class);
        context.fireWindowStateEvent(event);

        Assert.assertSame(event, component.stateEvent);
    }

    @Test
    public void addPortletModeListener_listenerIsCalledOnEvent() {
        Div component = new Div();
        ui.add(component);
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

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
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

        AtomicReference<WindowStateEvent> listener = new AtomicReference<>();
        context.addWindowStateChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        WindowStateEvent event = Mockito.mock(WindowStateEvent.class);
        context.fireWindowStateEvent(event);

        Assert.assertSame(event, listener.get());
    }

    @Test
    public void addRemovePortletModeListener_listenerIsNotCalledAfterRemoval() {
        Div component = new Div();
        ui.add(component);
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

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
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

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
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

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
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

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
        PortletViewContextImpl<Div> context = new PortletViewContextImpl<Div>(
                component, new AtomicBoolean());

        context.addEventChangeListener("bar", event -> {
        });

        String uid = assertJsHubRegistration("bar");

        context.init();

        Assert.assertEquals(uid, assertJsHubRegistration("bar"));
    }

    private String assertJsHubRegistration(String event) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        PendingJavaScriptInvocation invocation = ui.getInternals()
                .dumpPendingJavaScriptInvocations().get(0);
        String expression = invocation.getInvocation().getExpression();
        Assert.assertThat(expression,
                CoreMatchers.containsString("].registerListener"));
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
}

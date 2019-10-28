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
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.VaadinPortletEventContext;
import com.vaadin.flow.portal.handler.VaadinPortletEventView;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

public class VaadinPortletTest {

    private VaadinPortlet<TestComponent> portlet = new VaadinPortlet<TestComponent>() {

        @Override
        protected void doDispatch(RenderRequest request,
                RenderResponse response) throws PortletException, IOException {
        }

        @Override
        protected String getTitle(RenderRequest request) {
            return "";
        }
    };

    private static class TestComponent extends Div
            implements VaadinPortletEventView {

        private VaadinPortletEventContext context;

        @Override
        public void onPortletEventContextInit(
                VaadinPortletEventContext context) {
            this.context = context;
        }

    }

    private TestComponent component;

    @Before
    public void setUp() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);

        VaadinPortletService service = Mockito.mock(VaadinPortletService.class);
        Mockito.when(service.getPortlet()).thenReturn(portlet);
        VaadinPortletService.setCurrent(service);

        VaadinSession.setCurrent(session);

        UI ui = new UI();

        component = new TestComponent();
        ui.add(component);
        portlet.configure(null, component);

    }

    @After
    public void tearDown() {
        VaadinSession.setCurrent(null);
        VaadinPortletService.setCurrent(null);
    }

    @Test
    public void addWindowStateListener_stateIsChanged_listenerIsCalled()
            throws PortletException, IOException {
        Assert.assertNotNull(component.context);

        AtomicReference<WindowStateEvent> listener = new AtomicReference<>();
        component.context.addWindowStateListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        requestModeAndState("foo", "bar");

        Assert.assertNotNull(listener.get());
        Assert.assertEquals("bar", listener.get().getWindowState().toString());
    }

    @Test
    public void addWindowStateListener_unregister_listenerIsNotCalled()
            throws PortletException, IOException {
        AtomicReference<WindowStateEvent> listener = new AtomicReference<>();
        Registration registration = component.context.addWindowStateListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        requestModeAndState("foo", "bar");

        registration.remove();

        listener.set(null);

        requestModeAndState("foo", "baz");

        Assert.assertNull(listener.get());
    }

    @Test
    public void addPortletModeListener_modeIsChanged_listenerIsCalled()
            throws PortletException, IOException {
        Assert.assertNotNull(component.context);

        AtomicReference<PortletModeEvent> listener = new AtomicReference<>();
        component.context.addPortletModeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        requestModeAndState("foo", "bar");

        Assert.assertNotNull(listener.get());
        Assert.assertEquals("foo", listener.get().getPortletMode().toString());
    }

    @Test
    public void addPortletModeListener_unregister_listenerIsNotCalled()
            throws PortletException, IOException {
        Assert.assertNotNull(component.context);

        AtomicReference<PortletModeEvent> listener = new AtomicReference<>();
        Registration registration = component.context.addPortletModeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        requestModeAndState("foo", "bar");

        registration.remove();

        listener.set(null);

        requestModeAndState("baz", "bar");

        Assert.assertNull(listener.get());
    }

    private void requestModeAndState(String portletMode, String windowState)
            throws PortletException, IOException {
        RenderRequest request = Mockito.mock(RenderRequest.class);
        RenderResponse response = Mockito.mock(RenderResponse.class);
        ActionURL url = Mockito.mock(ActionURL.class);
        Mockito.when(url.toString()).thenReturn("");
        Mockito.when(response.createActionURL()).thenReturn(url);

        PortletMode mode = Mockito.mock(PortletMode.class);
        Mockito.when(mode.toString()).thenReturn(portletMode);
        Mockito.when(request.getPortletMode()).thenReturn(mode);

        WindowState state = Mockito.mock(WindowState.class);
        Mockito.when(state.toString()).thenReturn(windowState);
        Mockito.when(request.getWindowState()).thenReturn(state);

        portlet.render(request, response);
    }
}

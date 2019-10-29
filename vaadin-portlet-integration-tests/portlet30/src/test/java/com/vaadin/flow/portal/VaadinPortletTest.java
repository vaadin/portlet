package com.vaadin.flow.portal;

import javax.portlet.ActionURL;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
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
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.VaadinPortletEventContext;
import com.vaadin.flow.portal.handler.VaadinPortletEventView;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
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

        @Override
        protected VaadinPortletService getService() {
            return service;
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

    private String namespace = "namespace-foo";

    private TestComponent component;
    private VaadinPortletService service;

    @Before
    public void setUp() throws SessionExpiredException {
        service = Mockito.mock(VaadinPortletService.class);

        VaadinPortletSession session = new VaadinPortletSession(service) {
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

        Mockito.when(service.getPortlet()).thenReturn(portlet);
        Mockito.when(service.findVaadinSession(Mockito.any()))
                .thenReturn(session);

        VaadinPortletService.setCurrent(service);

        VaadinPortletResponse response = Mockito
                .mock(VaadinPortletResponse.class);
        CurrentInstance.set(VaadinResponse.class, response);
        PortletResponse portletResponse = Mockito.mock(PortletResponse.class);
        Mockito.when(response.getPortletResponse()).thenReturn(portletResponse);

        Mockito.when(portletResponse.getNamespace()).thenReturn(namespace);

        VaadinPortletRequest request = Mockito.mock(VaadinPortletRequest.class);

        PortletRequest portletRequest = Mockito.mock(PortletRequest.class);
        Mockito.when(request.getPortletRequest()).thenReturn(portletRequest);
        CurrentInstance.set(VaadinRequest.class, request);

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
        component.context.addWindowStateChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        requestModeAndState("foo", "bar");

        Assert.assertNotNull(listener.get());
        Assert.assertEquals("bar", listener.get().getWindowState().toString());
    }

    @Test
    public void addWindowStateListener_unregister_listenerIsNotCalled()
            throws PortletException, IOException {
        AtomicReference<WindowStateEvent> listener = new AtomicReference<>();
        Registration registration = component.context
                .addWindowStateChangeListener(
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
        component.context.addPortletModeChangeListener(
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
        Registration registration = component.context
                .addPortletModeChangeListener(
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

        Mockito.when(response.getNamespace()).thenReturn(namespace);

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
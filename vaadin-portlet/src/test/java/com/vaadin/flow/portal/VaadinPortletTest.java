package com.vaadin.flow.portal;

import javax.portlet.ActionURL;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.lifecycle.PortletModeEvent;
import com.vaadin.flow.portal.lifecycle.WindowStateEvent;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

public class VaadinPortletTest {

    private VaadinPortlet<TestComponent> portlet = new VaadinPortlet<TestComponent>() {

        @Override
        protected String getTitle(RenderRequest request) {
            return "";
        }

        @Override
        protected VaadinPortletService getService() {
            return service;
        }
    };

    private static class TestComponent extends Div implements PortletView {

        private PortletViewContext context;

        private int initCounts;

        @Override
        public void onPortletViewContextInit(PortletViewContext context) {
            this.context = context;
            initCounts++;
        }

    }

    private String namespace = "namespace-foo";

    private TestComponent component;
    private VaadinPortletService service;
    private UI ui;

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
        RenderResponse portletResponse = Mockito.mock(RenderResponse.class);
        Mockito.when(response.getPortletResponse()).thenReturn(portletResponse);

        Mockito.when(portletResponse.getNamespace()).thenReturn(namespace);

        VaadinPortletRequest request = Mockito.mock(VaadinPortletRequest.class);

        RenderRequest portletRequest = Mockito.mock(RenderRequest.class);
        Mockito.when(portletRequest.getPortletMode())
                .thenReturn(PortletMode.VIEW);
        Mockito.when(portletRequest.getWindowState())
                .thenReturn(WindowState.NORMAL);
        Mockito.when(request.getPortletRequest()).thenReturn(portletRequest);
        CurrentInstance.set(VaadinRequest.class, request);

        VaadinSession.setCurrent(session);

        ui = new UI() {
            @Override
            public VaadinSession getSession() {
                return session;
            }
        };
        UI.setCurrent(ui);

        ExtendedClientDetails details = Mockito
                .mock(ExtendedClientDetails.class);
        Mockito.when(details.getWindowName()).thenReturn("");
        ui.getInternals().setExtendedClientDetails(details);

        Mockito.when(request.getPortletMode()).thenReturn(PortletMode.VIEW);
        Mockito.when(request.getWindowState()).thenReturn(WindowState.NORMAL);

        component = new TestComponent();
        ui.add(component);
        portlet.configure(null, component);
    }

    @After
    public void tearDown() {
        CurrentInstance.clearAll();
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

    @Test
    public void configure_onPortletViewContextInitIsCalledOnce_listenersAreNotCalledTwice()
            throws PortletException, IOException {
        Assert.assertEquals(1, component.initCounts);

        AtomicReference<PortletModeEvent> listener = new AtomicReference<>();
        component.context.addPortletModeChangeListener(
                event -> Assert.assertNull(listener.getAndSet(event)));

        // re-attach
        requestModeAndState("foo", "bar");

        Assert.assertEquals(1, component.initCounts);

        listener.set(null);
        // fire an event one more time, listener should not throw ( should not
        // be called twice)
        requestModeAndState("foo", "bar");
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

        PortletRequest portletRequest = Mockito.mock(PortletRequest.class);
        Mockito.when(portletRequest.getPortletMode()).thenReturn(mode);
        Mockito.when(portletRequest.getWindowState()).thenReturn(state);
        Mockito.when(VaadinPortletRequest.getCurrentPortletRequest())
                .thenReturn(portletRequest);

        Mockito.when(VaadinPortletResponse.getCurrentPortletResponse())
                .thenReturn(response);

        // detach
        ui.remove(component);
        // attach
        ui.add(component);
    }

    @Test
    public void doDispatch_devServerEnabled_showErrorMessage()
            throws PortletException, IOException, NoSuchFieldException,
            IllegalAccessException {
        DeploymentConfiguration configuration = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(configuration.enableDevServer()).thenReturn(true);

        Mockito.when(service.getDeploymentConfiguration())
                .thenReturn(configuration);

        VaadinPortletResponse response = (VaadinPortletResponse) CurrentInstance
                .get(VaadinResponse.class);
        RenderResponse renderResponse = Mockito.mock(RenderResponse.class);
        Mockito.when(response.getPortletResponse()).thenReturn(renderResponse);
        Mockito.when(renderResponse.getNamespace()).thenReturn(namespace);
        StringWriter stringWriter = new StringWriter();
        Mockito.when(renderResponse.getWriter())
                .thenReturn(new PrintWriter(stringWriter));

        VaadinPortletRequest request = (VaadinPortletRequest) CurrentInstance
                .get(VaadinRequest.class);
        RenderRequest renderRequest = Mockito.mock(RenderRequest.class);
        Mockito.when(request.getPortletRequest()).thenReturn(renderRequest);

        portlet.doDispatch(renderRequest, renderResponse);

        Field devModeErrorMessageField = VaadinPortlet.class
                .getDeclaredField("DEV_MODE_ERROR_MESSAGE");
        devModeErrorMessageField.setAccessible(true);
        String expectedDevModeErrorMessage = (String) devModeErrorMessageField
                .get(null);
        Assert.assertEquals(
                "When dev server is enabled, DEV_MODE_ERROR_MESSAGE should be shown in the portlet.",
                expectedDevModeErrorMessage, stringWriter.toString().trim());
    }
}

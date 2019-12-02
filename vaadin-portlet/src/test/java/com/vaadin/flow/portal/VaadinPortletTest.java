package com.vaadin.flow.portal;

import javax.portlet.ActionParameters;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.portal.VaadinPortlet.PortletWebComponentExporter;
import com.vaadin.flow.portal.lifecycle.PortletEvent;
import com.vaadin.flow.portal.lifecycle.PortletModeEvent;
import com.vaadin.flow.portal.lifecycle.WindowStateEvent;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

public class VaadinPortletTest {

    private class TestVaadinPortlet extends VaadinPortlet<TestComponent> {

        private class TestWebComponentExporter
                extends PortletWebComponentExporter {

            private TestWebComponentExporter(String tag) {
                super(tag);
            }

            @Override
            protected void configureInstance(
                    WebComponent<TestComponent> webComponent,
                    TestComponent component) {
                super.configureInstance(webComponent, component);
            }

            @Override
            protected Class<TestComponent> getComponentClass() {
                return super.getComponentClass();
            }

        }

        private TestWebComponentExporter exporter;

        @Override
        protected String getTitle(RenderRequest request) {
            return "";
        }

        @Override
        protected VaadinPortletService getService() {
            return service;
        }

        @Override
        public WebComponentExporter<TestComponent> create() {
            exporter = new TestWebComponentExporter(getPortletTag());
            return exporter;
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

    private static class TestMYPortlet extends VaadinPortlet<Div> {

    }

    private static class Wrapper {

        private static class TestMYPortlet extends VaadinPortlet<Div> {

        }

    }

    private static class Special$Character extends VaadinPortlet<Div> {

    }

    private String namespace = "namespace-foo";

    private TestVaadinPortlet portlet;
    private TestComponent component;
    private VaadinPortletService service;
    private UI ui;

    @Before
    public void setUp() throws SessionExpiredException {
        portlet = new TestVaadinPortlet();
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

        portlet.create();
        component = new TestComponent();
        ui.add(component);
        portlet.exporter.configureInstance(null, component);
    }

    @After
    public void tearDown() {
        CurrentInstance.clearAll();
    }

    @Test
    public void createExporter_getComponentClass_componentClassIsDetected() {
        Assert.assertEquals(TestComponent.class,
                portlet.exporter.getComponentClass());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void createExporter_exporterIsNotExtended_componentClassIsDetected() {
        TestMYPortlet portlet = new TestMYPortlet();

        PortletWebComponentExporter exporter = (PortletWebComponentExporter) portlet
                .create();
        Assert.assertEquals(Div.class, exporter.getComponentClass());
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

    @Test
    public void getTag_tagNameDoNoContainUpperCaseLetters() {
        TestMYPortlet portlet = new TestMYPortlet();
        String tag = portlet.getPortletTag();
        Assert.assertFalse(tag.chars().anyMatch(Character::isUpperCase));
    }

    @Test
    public void getTag_sameSimpleClassNamesDoNotCollide() {
        TestMYPortlet portlet = new TestMYPortlet();
        String tag = portlet.getPortletTag();
        Assert.assertNotEquals(tag,
                new Wrapper.TestMYPortlet().getPortletTag());
    }

    @Test
    public void getTag_tagNameDoNoContainUpperCaseLettersAndDollarSign() {
        Special$Character portlet = new Special$Character();
        String tag = portlet.getPortletTag();
        Assert.assertFalse(tag.chars().anyMatch(Character::isUpperCase));
        Assert.assertFalse(tag.chars().anyMatch(ch -> ch == '$'));
    }

    @Test
    public void processAction_eventIsFiredAndNoExceptions()
            throws PortletException, SessionExpiredException {
        VaadinSession.setCurrent(null);
        ReentrantLock lock = new ReentrantLock();
        VaadinPortletSession session = new VaadinPortletSession(service) {

            @Override
            public Object getAttribute(String name) {
                return super.getAttribute(name);
            }

            @Override
            public Lock getLockInstance() {
                return lock;
            };
        };

        Map<String, PortletViewContext> viewContexts = new HashMap<>();

        Div div = new Div();
        ui.add(div);
        PortletViewContext context = new PortletViewContext(div,
                new AtomicBoolean(true), PortletMode.UNDEFINED,
                WindowState.UNDEFINED);
        viewContexts.put(namespace, context);

        AtomicReference<PortletEvent> listener = new AtomicReference<>();
        context.addEventChangeListener("foo",
                event -> Assert.assertNull(listener.getAndSet(event)));
        ui.getInternals().setSession(session);

        ActionParameters params = Mockito.mock(ActionParameters.class);

        session.accessSynchronously(() -> {
            session.setAttribute(
                    TestVaadinPortlet.class.getName() + "-bar-viewContext",
                    viewContexts);
            Mockito.when(params.getValue("vaadin.uid"))
                    .thenReturn(getListenerUid());
        });

        VaadinSession.setCurrent(session);
        Mockito.when(service.findVaadinSession(Mockito.any()))
                .thenReturn(session);

        ActionRequest request = Mockito.mock(ActionRequest.class);
        ActionResponse response = Mockito.mock(ActionResponse.class);

        Mockito.when(response.getNamespace()).thenReturn(namespace);

        Mockito.when(request.getActionParameters()).thenReturn(params);

        Mockito.when(params.getNames())
                .thenReturn(Collections.singleton("vaadin.ev"));

        Mockito.when(params.getValue("vaadin.ev")).thenReturn("foo");
        Mockito.when(params.getValue("vaadin.wn")).thenReturn("bar");
        portlet.processAction(request, response);

        Assert.assertNotNull(listener.get());
    }

    private String getListenerUid() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        PendingJavaScriptInvocation invocation = ui.getInternals()
                .dumpPendingJavaScriptInvocations().get(0);
        String uid = invocation.getInvocation().getParameters().get(2)
                .toString();
        return uid;
    }
}

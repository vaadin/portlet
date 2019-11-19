package com.vaadin.flow.portal;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class PortletClassesSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.of(
                "com\\.vaadin\\.flow\\.portal\\.PortletConstants",
                // these 2 can be made serializable, if we introduce a
                // serializable wrapper for the wrapped portlet request and
                // response
                "com\\.vaadin\\.flow\\.portal\\.VaadinPortletRequest",
                "com\\.vaadin\\.flow\\.portal\\.VaadinPortletResponse",
                // this can be made serializable once VaadinPortletRequest is
                // serializable
                "com\\.vaadin\\.flow\\.portal\\." +
                        "PortletStreamReceiverHandler\\$StreamRequestContext"

        );
    }

    @Override
    protected Pattern getJarPattern() {
        return Pattern.compile("(.*vaadin.*)/(.*flow.*)/(.*portal.*)\\.jar");

    }

    @Override
    protected Stream<String> getBasePackages() {
        return Stream.of("com.vaadin.flow.portal");
    }

    @Test
    public void serializePortletViewContextImpl() throws Throwable {
        PortletMode portletMode = PortletMode.VIEW;
        WindowState windowState = WindowState.NORMAL;

        ComponentParameter view = new ComponentParameter();
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        PortletViewContext<ComponentParameter> original = new PortletViewContext<>(
                view, atomicBoolean, portletMode, windowState);

        PortletViewContext<ComponentParameter> deserialized = serializeAndDeserialize(
                original);

        Assert.assertEquals(portletMode, deserialized.getPortletMode());
        Assert.assertEquals(windowState, deserialized.getWindowState());

        // assert that view component has been deserialized and listener is
        // registered
        deserialized.fireWindowStateEvent(new WindowStateEvent(
                WindowState.MAXIMIZED, WindowState.MINIMIZED, false));
        Assert.assertEquals(1, ComponentParameter.integer.get());
    }

    @Tag("component-parameter")
    public static class ComponentParameter extends Component implements WindowStateHandler {
        public static AtomicInteger integer = new AtomicInteger();

        @Override
        public void windowStateChange(WindowStateEvent event) {
            integer.incrementAndGet();
        }
    }
}

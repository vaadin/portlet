package com.vaadin.flow.portal;

import javax.portlet.ResourceURL;
import javax.portlet.filter.ResourceURLWrapper;
import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.VaadinResponse;

public class PortletStreamResourceRegistryTest {

    private VaadinPortletService service;
    private UI ui;
    private PortletStreamResourceRegistry registry;
    private final StreamResourceMock streamResourceMock = new StreamResourceMock();
    private final String resourceId = streamResourceMock.getId();

    @Before
    public void init() {
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

            @Override
            public void checkHasLock() {

            }
        };

        ui = Mockito.mock(UI.class);
        Mockito.when(ui.getUIId()).thenReturn(42);

        registry = new PortletStreamResourceRegistry(session);
    }

    @Test
    public void getResourceUri_nonMimeContent_returnsDirectUrl() {
        VaadinPortletResponse responseMock = Mockito.mock(VaadinPortletResponse.class);

        VaadinResponse vaadinResponse = CurrentInstance.get(VaadinResponse.class);
        UI currentUI = CurrentInstance.get(UI.class);
        try {
            CurrentInstance.set(VaadinResponse.class, responseMock);
            CurrentInstance.set(UI.class, this.ui);
            StreamRegistration registration = registry.registerResource(streamResourceMock);
            URI resourceUri = registration.getResourceUri();
            String expected = String.format(
                    "VAADIN/dynamic/resource/42/%s/test.xml", resourceId);
            Assert.assertEquals(expected, resourceUri.toString());
        } finally {
            if (vaadinResponse != null) {
                CurrentInstance.set(VaadinResponse.class, vaadinResponse);
            }
            if (currentUI != null) {
                CurrentInstance.set(UI.class, currentUI);
            }
        }
    }

    private static final class StreamResourceMock extends AbstractStreamResource {
        @Override
        public String getName() {
            return "test.xml";
        }
    }

    private static final class ResourceUrlMock extends ResourceURLWrapper {

        private String url;

        public ResourceUrlMock(String url) {
            super(Mockito.mock(ResourceURL.class));
            this.url = url;
        }

        @Override
        public void setResourceID(String resourceID) {
            url += resourceID;
        }

        @Override
        public String toString() {
            return url;
        }
    }
}
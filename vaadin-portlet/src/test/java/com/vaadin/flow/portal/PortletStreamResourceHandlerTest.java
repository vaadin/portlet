/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.server.StreamResource;

public class PortletStreamResourceHandlerTest {

    private PortletStreamResourceHandler handler = new PortletStreamResourceHandler();
    private VaadinPortletSession session;
    private VaadinPortletService service;

    @Before
    public void init() {
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

            @Override
            public void checkHasLock() {

            }
        };
    }

    @Test
    public void handleRequest_shouldApplyStreamResourceHeaders()
            throws IOException {
        PortletRequest portletRequest = Mockito.mock(PortletRequest.class);
        VaadinPortletRequest request = new VaadinPortletRequest(portletRequest,
                service);
        MimeResponse portletResponse = Mockito.mock(MimeResponse.class);
        VaadinPortletResponse response = new VaadinPortletResponse(
                portletResponse, service);

        StreamResource resource = new StreamResource("export.xlsx",
                () -> new ByteArrayInputStream(new byte[0]));
        resource.setContentType("application/xls");
        String headerName = "Content-Disposition";
        String headerValue = "attachment;filename=export.xlsx";
        resource.setHeader(headerName, headerValue);
        handler.handleRequest(session, request, response, resource);
        Mockito.verify(portletResponse, Mockito.atLeastOnce())
                .setProperty(headerName, headerValue);
    }

}

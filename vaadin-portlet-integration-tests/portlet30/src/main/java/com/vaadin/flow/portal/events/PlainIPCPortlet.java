/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.events;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.annotations.Dependency;
import javax.portlet.annotations.LocaleString;
import javax.portlet.annotations.PortletConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

@PortletConfiguration(portletName = "PlainPortlet", publicParams = "param", dependencies = @Dependency(name = "PortletHub", scope = "javax.portlet", version = "3.0.0"))
public class PlainIPCPortlet extends GenericPortlet {

    private static final String PARAM = "param";

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        String barValue = request.getActionParameters().getValue("bar");

        response.getRenderParameters().setValue(PARAM, barValue);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setContentType("text/html");

        String namespace = response.getNamespace();

        OutputStream outputStream = response.getPortletOutputStream();
        outputStream.write(getContent(namespace,
                request.getRenderParameters().getValue(PARAM))
                        .getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    private String getContent(String namespace, String param)
            throws IOException {
        InputStream inputStream = PlainIPCPortlet.class
                .getResourceAsStream("/plain-portlet.html");

        String html = IOUtils.readLines(inputStream, StandardCharsets.UTF_8)
                .stream().collect(Collectors.joining());

        html = html.replace("%ns%", namespace);
        html = html.replace("%param%", param == null ? "" : param);
        return html;
    }
}

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

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.StreamReceiverHandler;
import com.vaadin.flow.server.communication.StreamRequestHandler;

/**
 * Request handler for portlet uploads.
 * <p>
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletStreamRequestHandler extends StreamRequestHandler {

    private static final char PATH_SEPARATOR = '/';

    private final PortletStreamResourceHandler resourceHandler = new PortletStreamResourceHandler();
    private StreamReceiverHandler receiverHandler;

    public PortletStreamRequestHandler() {
        this(new PortletStreamReceiverHandler());
    }

    protected PortletStreamRequestHandler(
            StreamReceiverHandler receiverHandler) {
        super(receiverHandler);
        this.receiverHandler = receiverHandler;
    }

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return false;
        }
        // remove leading '/'
        if(pathInfo.startsWith(Character.toString(PATH_SEPARATOR))) {
            pathInfo = pathInfo.substring(1);
        }

        if (!pathInfo.startsWith(DYN_RES_PREFIX)) {
            return false;
        }

        Optional<AbstractStreamResource> abstractStreamResource;
        session.lock();
        try {
            abstractStreamResource = PortletStreamRequestHandler
                    .getPathUri(pathInfo)
                    .flatMap(session.getResourceRegistry()::getResource);
            if (!abstractStreamResource.isPresent()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Resource is not found for path=" + pathInfo);
                return true;
            }
        } finally {
            session.unlock();
        }

        if (abstractStreamResource.isPresent()) {
            AbstractStreamResource resource = abstractStreamResource.get();
            if (resource instanceof StreamResource) {
                resourceHandler.handleRequest(session, request, response,
                        (StreamResource) resource);
            } else if (resource instanceof StreamReceiver) {
                StreamReceiver streamReceiver = (StreamReceiver) resource;
                String[] parts = parsePortletPath(pathInfo);

                receiverHandler.handleRequest(session, request, response,
                        streamReceiver, parts[0], parts[1]);
            } else {
                getLogger().warn("Received unknown stream resource.");
            }
        }
        return true;
    }

    /**
     * Parse the pathInfo for id data.
     * <p>
     * URI pattern: VAADIN/dynamic/resource/[UIID]/[SECKEY]/[NAME]
     *
     * @see #generateURI
     */
    private String[] parsePortletPath(String pathInfo) {
        // strip away part until the data we are interested starts
        int startOfData = pathInfo.indexOf(DYN_RES_PREFIX)
                + DYN_RES_PREFIX.length();

        String uppUri = pathInfo.substring(startOfData);
        // [0] UIid, [1] security key, [2] name
        return uppUri.split("/", 3);
    }

    private static String encodeString(String name)
            throws UnsupportedEncodingException {
        return URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
    }

    private static Optional<URI> getPathUri(String path) {
        int index = path.lastIndexOf('/');
        boolean hasPrefix = index >= 0;
        if (!hasPrefix) {
            getLogger().info("Unsupported path structure, path={}", path);
            return Optional.empty();
        }
        String prefix = path.substring(0, index + 1);
        String name = path.substring(prefix.length());
        try {
            URI uri = new URI(prefix + encodeString(name));
            return Optional.of(uri);
        } catch (UnsupportedEncodingException e) {
            // UTF8 has to be supported
            throw new VaadinPortletException(e);
        } catch (URISyntaxException e) {
            getLogger().info(
                    "Path '{}' is not correct URI (it violates RFC 2396)", path,
                    e);
            return Optional.empty();
        }
    }

    private static Logger getLogger() {
        return LoggerFactory
                .getLogger(PortletStreamRequestHandler.class.getName());
    }
}

/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 * <p>
 * This program is available under Vaadin Commercial License and Service Terms.
 * <p>
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.UrlUtil;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.flow.server.*;
import com.vaadin.flow.server.communication.StreamReceiverHandler;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.communication.StreamResourceHandler;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.portlet.ClientDataRequest;
import jakarta.portlet.PortletRequest;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Optional;

import static com.vaadin.flow.server.Constants.*;

/**
 * Request handler for portlet uploads.
 * Adjusted StreamRequestHandler for Portlet use
 * <p>
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletStreamRequestHandler extends StreamRequestHandler {

    private static final char PATH_SEPARATOR = '/';

    /**
     * Dynamic resource URI prefix.
     */
    public static final String DYN_RES_PREFIX = "VAADIN/dynamic/resource/";

    private final PortletStreamResourceHandler resourceHandler = new PortletStreamResourceHandler();
    private StreamReceiverHandler receiverHandler;

    /**
     * Create a new stream request handler with the default
     * StreamReceiverHandler.
     */

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
        assert pathInfo.startsWith(Character.toString(PATH_SEPARATOR));
        pathInfo = pathInfo.substring(1);

        if (!pathInfo.startsWith(DYN_RES_PREFIX)) {
            return false;
        }

        Optional<AbstractStreamResource> abstractStreamResource;
        session.lock();
        try {
            abstractStreamResource = getPathUri(pathInfo)
                    .flatMap(session.getResourceRegistry()::getResource);
            if (abstractStreamResource.isEmpty()) {
                response.sendError(HttpStatusCode.NOT_FOUND.getCode(),
                        "Resource is not found for path=" + pathInfo);
                return true;
            }
        } finally {
            session.unlock();
        }

        AbstractStreamResource resource = abstractStreamResource.get();
        if (resource instanceof StreamResourceRegistry.ElementStreamResource elementRequest) {
            callElementResourceHandler(session, request, response, elementRequest, pathInfo);
        } else if (resource instanceof StreamResource streamResource) {
            resourceHandler.handleRequest(session, request, response, streamResource);
        } else if (resource instanceof StreamReceiver streamReceiver) {
            PathData parts = parsePath(pathInfo);

            receiverHandler.handleRequest(session, request, response,
                    streamReceiver, parts.UIid, parts.securityKey);
        } else {
            getLogger().warn("Received unknown stream resource.");
        }
        return true;
    }

    private void callElementResourceHandler(VaadinSession session,
                                            VaadinRequest request, VaadinResponse response,
                                            StreamResourceRegistry.ElementStreamResource elementRequest,
                                            String pathInfo) throws IOException {
        Element owner = elementRequest.getOwner();
        StateNode node = owner.getNode();

        session.lock();
        try {
            if (blockInert(elementRequest, node)
                || blockDisabled(elementRequest, node) || !node.isAttached()
                || !node.isVisible()) {
                response.sendError(HttpStatusCode.FORBIDDEN.getCode(),
                        "Resource not available");
                return;
            }

            if (elementRequest.getElementRequestHandler() instanceof UploadHandler) {
                // Validate upload security key. Else respond with
                // FORBIDDEN.
                PathData parts = parsePath(pathInfo);
                String secKey = elementRequest.getId();
                if (secKey == null || !MessageDigest.isEqual(
                        secKey.getBytes(StandardCharsets.UTF_8),
                        parts.securityKey.getBytes(StandardCharsets.UTF_8))) {
                    LoggerFactory.getLogger(PortletStreamResourceHandler.class).warn(
                            "Received incoming stream with faulty security key.");
                    response.sendError(HttpStatusCode.FORBIDDEN.getCode(),
                            "Resource not available");
                    return;
                }

                elementRequest.getOwner().getComponent()
                        .ifPresent(value ->
                                ComponentUtil.setData(value, "uiid", parts.UIid));

                if (node == null) {
                    session.getErrorHandler()
                            .error(new ErrorEvent(new UploadException(
                                    "File upload ignored because the node for the upload owner component was not found")));
                    response.sendError(HttpStatusCode.FORBIDDEN.getCode(),
                            "Resource not available");
                    return;
                }
                if (!node.isAttached()) {
                    session.getErrorHandler()
                            .error(new ErrorEvent(new UploadException(
                                    "Warning: file upload ignored for "
                                    + node.getId()
                                    + " because the component was disabled")));
                    response.sendError(HttpStatusCode.FORBIDDEN.getCode(),
                            "Resource not available");
                    return;
                }
            }
        } finally {
            session.unlock();
        }

        // Vaadin's TransferUtil.isMultipartContent() checks for HttpServletRequest,
        // which fails for portlet requests. For multipart portlet uploads, we must
        // extract the parts ourselves and create UploadEvents with proper Part objects
        // so that UploadEvent.getInputStream() returns the file content rather than
        // the raw multipart body (which includes WebKit form boundaries).
        if (elementRequest.getElementRequestHandler() instanceof UploadHandler uploadHandler
            && request instanceof VaadinPortletRequest portletRequest) {
            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                handlePortletMultipartUpload(uploadHandler, portletRequest,
                        response, session, elementRequest.getOwner());
                return;
            }
        }

        elementRequest.getElementRequestHandler().handleRequest(request,
                response, session, elementRequest.getOwner());
    }

    /**
     * Handles multipart uploads for portlet requests by extracting parts from
     * the portlet's ClientDataRequest and creating UploadEvents with proper
     * Part objects, bypassing TransferUtil which only supports HttpServletRequest.
     */
    private void handlePortletMultipartUpload(UploadHandler handler,
                                              VaadinPortletRequest request,
                                              VaadinResponse response,
                                              VaadinSession session,
                                              Element owner) throws IOException {
        PortletRequest portletRequest = request.getPortletRequest();
        Collection<Part> parts;
        try {
            parts = ((ClientDataRequest) portletRequest).getParts();
        } catch (Exception e) {
            throw new IOException("Failed to get multipart parts from portlet request", e);
        }

        if (parts.isEmpty()) {
            getLogger().warn("Multipart portlet request has no parts");
            handler.responseHandled(false, response);
            return;
        }

        try {
            for (Part part : parts) {
                // Skip non-file form fields
                if (part.getSubmittedFileName() == null) {
                    continue;
                }

                UploadEvent event = new UploadEvent(request, response, session,
                        part.getSubmittedFileName(), part.getSize(),
                        part.getContentType(), owner, null, part);

                Component ownerComponent = owner.getComponent().orElse(null);
                try {
                    if (ownerComponent != null) {
                        ComponentUtil.fireEvent(ownerComponent, new UploadStartEvent(ownerComponent));
                    }
                    handler.handleUploadRequest(event);
                } finally {
                    if (ownerComponent != null) {
                        ComponentUtil.fireEvent(ownerComponent, new UploadCompleteEvent(ownerComponent));
                    }
                }
            }
            handler.responseHandled(true, response);
        } catch (Exception e) {
            getLogger().error("Exception during portlet upload", e);
            handler.responseHandled(false, response);
        }
    }

    private static boolean blockDisabled(
            StreamResourceRegistry.ElementStreamResource elementRequest,
            StateNode node) {
        return !node.isEnabled() && elementRequest.getElementRequestHandler()
                                            .getDisabledUpdateMode() == DisabledUpdateMode.ONLY_WHEN_ENABLED;
    }

    private static boolean blockInert(
            StreamResourceRegistry.ElementStreamResource elementRequest,
            StateNode node) {
        return node.isInert()
               && !elementRequest.getElementRequestHandler().isAllowInert();
    }

    private record PathData(String UIid, String securityKey, String fileName) implements Serializable {
    }

    /**
     * Parse the pathInfo for id data.
     * <p>
     * URI pattern: VAADIN/dynamic/resource/[UIID]/[SECKEY]/[NAME]
     *
     * @see #generateURI
     */
    private PathData parsePath(String pathInfo) {
        // strip away part until the data we are interested starts
        int startOfData = pathInfo.indexOf(DYN_RES_PREFIX)
                          + DYN_RES_PREFIX.length();

        String uppUri = pathInfo.substring(startOfData);
        // [0] UIid, [1] security key, [2] name
        String[] split = uppUri.split("/", 3);
        if (split.length == 3) {
            return new PathData(split[0], split[1], split[2]);
        }
        return new PathData(split[0], split[1], "");
    }

    /**
     * Generates URI string for a dynamic resource using its {@code id} and
     * {@code name}. [0] UIid, [1] sec key, [2] name
     *
     * @param name file or attribute name to use in path
     * @param id   unique resource id
     * @return generated URI string
     */
    public static String generateURI(String name, String id) {
        return DYN_RES_PREFIX + UI.getCurrent().getUIId() + PATH_SEPARATOR + id
               + PATH_SEPARATOR + UrlUtil.encodeURIComponent(name);
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
            URI uri = new URI(prefix + UrlUtil.encodeURIComponent(name));
            return Optional.of(uri);
        } catch (URISyntaxException e) {
            getLogger().info(
                    "Path '{}' is not correct URI (it violates RFC 2396)", path,
                    e);
            return Optional.empty();
        }
    }

    /**
     * Returns maximum request size for upload. Override this to increase the
     * default. Defaults to -1 (no limit).
     *
     * @return maximum request size for upload
     */
    protected long getRequestSizeMax() {
        return DEFAULT_REQUEST_SIZE_MAX;
    }

    /**
     * Returns maximum file size for upload. Override this to increase the
     * default. Defaults to -1 (no limit).
     *
     * @return maximum file size for upload
     */
    protected long getFileSizeMax() {
        return DEFAULT_FILE_SIZE_MAX;
    }

    /**
     * Returns maximum file part count for upload. Override this to increase the
     * default. Defaults to 10000.
     *
     * @return maximum file part count for upload
     */
    protected long getFileCountMax() {
        return DEFAULT_FILE_COUNT_MAX;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(StreamResourceHandler.class.getName());
    }
}

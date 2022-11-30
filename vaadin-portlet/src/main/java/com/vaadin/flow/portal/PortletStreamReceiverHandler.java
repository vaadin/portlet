/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.portlet.ClientDataRequest;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.StreamReceiverHandler;

/**
 * Extends {@link StreamReceiverHandler} to handle upload
 * {@link VaadinRequest}s where the underlying request is of type
 * {@link ClientDataRequest} instead of {@link HttpServletRequest}.
 * <p>
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletStreamReceiverHandler extends StreamReceiverHandler {

    @Override
    protected boolean isMultipartUpload(VaadinRequest request) {
        return request instanceof VaadinPortletRequest &&
                PortletFileUpload.isMultipartContent(getRequestContext(request));
    }

    @Override
    protected Collection<Part> getParts(VaadinRequest request)
            throws Exception {
        PortletRequest portletRequest = getPortletRequest(request);
        return ((ClientDataRequest) portletRequest).getParts();
    }

    @Override
    protected FileItemIterator getItemIterator(VaadinRequest request)
            throws FileUploadException, IOException {
        PortletFileUpload upload = new PortletFileUpload();
        return upload.getItemIterator(getRequestContext(request));
    }

    private PortletRequest getPortletRequest(VaadinRequest request) {
        return ((VaadinPortletRequest) request).getPortletRequest();
    }

    private RequestContext getRequestContext(VaadinRequest request) {
        return new StreamRequestContext(request);
    }

    private static class StreamRequestContext implements RequestContext {
        private final VaadinRequest request;

        StreamRequestContext(VaadinRequest request) {
            this.request = request;
        }

        @Override
        public String getCharacterEncoding() {
            return request.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return request.getContentType();
        }

        @Override
        public int getContentLength() {
            return request.getContentLength();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return request.getInputStream();
        }
    }
}

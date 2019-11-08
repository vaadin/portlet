/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.portal.internal;

import javax.portlet.ClientDataRequest;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import com.vaadin.flow.portal.VaadinPortletRequest;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.StreamReceiverHandler;

/**
 * Extends {@link StreamReceiverHandler} to handle upload
 * {@link VaadinRequest}s where the underlying request is of type
 * {@link ClientDataRequest} instead of {@link HttpServletRequest}.
 *
 * For internal use only.
 */
public class PortletStreamReceiverHandler extends StreamReceiverHandler {

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

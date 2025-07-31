/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.UidlRequestHandler;

/**
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
 */
class PortletUidlRequestHandler extends UidlRequestHandler {

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        return "/uidl".equals(request.getPathInfo());
    }

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request,
                                             VaadinResponse response) throws IOException {
        VaadinResponseWrapper vaadinResponseWrapper =
                new VaadinResponseWrapper(request, response);
        return super.synchronizedHandleRequest(session, request, vaadinResponseWrapper);
    }

    @Override
    public boolean isReadAndWriteOutsideSessionLock() {
        // special VaadinResponseWrapper is not compatible with returning true.
        return false;
    }

    /**
     * Wraps the portlet response to stub the writing actions so as to not
     * write the UIDL sync message, when the error occurs during RPC handling.
     * Specific to Liferay.
     * See https://github.com/vaadin/portlet/issues/213
     */
    private static class VaadinResponseWrapper implements VaadinResponse {

        private final VaadinPortletRequest request;
        private final VaadinPortletResponse delegate;

        public VaadinResponseWrapper(VaadinRequest vaadinRequest,
                                     VaadinResponse vaadinResponse) {
            if (!(vaadinResponse instanceof VaadinPortletResponse &&
                  vaadinRequest instanceof VaadinPortletRequest)) {
                throw new IllegalArgumentException(
                        "Portlet request/response expected, make sure you run the application in the portal container");
            }
            request = (VaadinPortletRequest) vaadinRequest;
            delegate = (VaadinPortletResponse) vaadinResponse;
        }

        @Override
        public void setStatus(int statusCode) {
            delegate.setStatus(statusCode);
        }

        @Override
        public void setContentType(String contentType) {
            delegate.setContentType(contentType);
        }

        @Override
        public void setHeader(String name, String value) {
            delegate.setHeader(name, value);
        }

        @Override
        public void setDateHeader(String name, long timestamp) {
            delegate.setDateHeader(name, timestamp);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            if (noError()) {
                return delegate.getOutputStream();
            } else {
                return new OutputStreamWrapper();
            }
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return null;
        }

        @Override
        public void setCacheTime(long milliseconds) {
            delegate.setCacheTime(milliseconds);
        }

        @Override
        public void sendError(int errorCode, String message) throws IOException {
            delegate.sendError(errorCode, message);
        }

        @Override
        public VaadinService getService() {
            return delegate.getService();
        }

        @Override
        public void addCookie(Cookie cookie) {
            delegate.addCookie(cookie);
        }

        @Override
        public void setContentLength(int len) {
            if (noError()) {
                delegate.setContentLength(len);
            }
        }

        @Override
        public void setNoCacheHeaders() {
            delegate.setNoCacheHeaders();
        }

        private boolean noError() {
            return request.getPortletRequest().getAttribute(
                    DefaultPortletErrorHandler.ERROR_ATTRIBUTE_NAME) == null;
        }
    }

    /**
     * Null output stream implementation.
     */
    private static class OutputStreamWrapper extends OutputStream
            implements Serializable {
        private volatile boolean closed;

        private void ensureOpen() throws IOException {
            if (this.closed) {
                throw new IOException("Stream closed");
            }
        }

        public void write(int b) throws IOException {
            this.ensureOpen();
        }

        public void write(byte[] b, int off, int len) throws IOException {
            this.ensureOpen();
        }

        public void close() {
            this.closed = true;
        }
    }
}

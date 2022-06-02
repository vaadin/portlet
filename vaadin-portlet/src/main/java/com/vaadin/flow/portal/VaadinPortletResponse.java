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

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceResponse;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletResponse;

/**
 * Wrapper for {@link PortletResponse} and its subclasses.
 *
 * @author Vaadin Ltd.
 * @see VaadinResponse
 * @see VaadinPortletRequest
 * @since
 */
public class VaadinPortletResponse implements VaadinResponse {
    static final DateFormat HTTP_DATE_FORMAT = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    static {
        HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final PortletResponse response;
    private final VaadinPortletService vaadinService;

    /**
     * Wraps a portlet response and an associated vaadin service.
     *
     * @param response
     *         the portlet response to wrap
     * @param vaadinService
     *         the associated vaadin service
     */
    public VaadinPortletResponse(PortletResponse response,
            VaadinPortletService vaadinService) {
        this.response = response;
        this.vaadinService = vaadinService;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (response instanceof MimeResponse) {
            return ((MimeResponse) response).getPortletOutputStream();
        } else {
            throw new IOException(
                    "Output stream not available for response of type "
                            + response.getClass().getName());
        }
    }

    /**
     * Gets the original, unwrapped portlet response.
     *
     * @return the unwrapped portlet response
     */
    public PortletResponse getPortletResponse() {
        return response;
    }

    @Override
    public void setContentType(String type) {
        if (response instanceof MimeResponse) {
            ((MimeResponse) response).setContentType(type);
        } else {
            throw new VaadinPortletException(
                    "Content type cannot be set for response of type "
                            + response.getClass().getName());
        }
    }

    @Override
    public void setContentLength(int len) {
        if (response instanceof ResourceResponse) {
            ((ResourceResponse) response).setContentLength(len);
        }

    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (response instanceof MimeResponse) {
            return ((MimeResponse) response).getWriter();
        } else {
            throw new IOException(
                    "Writer not available for response of type " + response
                            .getClass().getName());
        }
    }

    @Override
    public void setStatus(int responseStatus) {
        response.setProperty(ResourceResponse.HTTP_STATUS_CODE,
                Integer.toString(responseStatus));
    }

    @Override
    public void setHeader(String name, String value) {
        response.setProperty(name, value);
    }

    @Override
    public void setDateHeader(String name, long timestamp) {
        response.setProperty(name,
                HTTP_DATE_FORMAT.format(new Date(timestamp)));
    }

    @Override
    public void setCacheTime(long milliseconds) {
        VaadinServletResponse.doSetCacheTime(this, milliseconds);
    }

    @Override
    public void sendError(int errorCode, String message) throws IOException {
        setStatus(errorCode);
        if (message != null) {
            message = escapeHtml(message);
        }
        getWriter().write(message);
    }

    /**
     * Perform minimal HTML escaping similar to Guava HtmlEscapers.
     *
     * @param input
     *         string to escape
     * @return minimally escaped HTML safe string
     */
    private static String escapeHtml(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
            case '"':
                sb.append("&quot;");
                break;
            case '\'':
                sb.append("&#39;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public VaadinPortletService getService() {
        return vaadinService;
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addProperty(cookie);
    }

    /**
     * Gets the currently processed portlet response. The current portlet response
     * is automatically defined when the requests is started. The current portlet
     * response can not be used in e.g. background threads because of the way
     * server implementations reuse response instances.
     *
     * @return the current portlet response instance if available, otherwise
     * <code>null</code>
     */
    public static PortletResponse getCurrentPortletResponse() {
        return VaadinPortletService.getCurrentPortletResponse();

    }
    /**
     * Gets the currently processed Vaadin portlet response. The current response
     * is automatically defined when the request is started. The current response
     * can not be used in e.g. background threads because of the way server
     * implementations reuse response instances.
     *
     * @return the current Vaadin portlet response instance if available,
     * otherwise <code>null</code>
     */
    public static VaadinPortletResponse getCurrent() {
        return VaadinPortletService.getCurrentResponse();
    }
}

package com.vaadin.flow.portal;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.StreamResourceHandler;

public class PortletStreamResourceHandler extends StreamResourceHandler {

    @Override
    public void handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response, StreamResource streamResource)
            throws IOException {
        StreamResourceWriter writer;
        session.lock();
        try {
            setResponseContentType(request, response, streamResource);
            response.setCacheTime(streamResource.getCacheTime());
            writer = streamResource.getWriter();
            if (writer == null) {
                throw new IOException(
                        "Stream resource produces null input stream");
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw exception;

        } finally {
            session.unlock();
        }
        // don't use here "try resource" syntax sugar because in case there is
        // an exception the {@code outputStream} will be closed before "catch"
        // block which sets the status code and this code will not have any
        // effect being called after closing the stream (see #8740).
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            writer.accept(outputStream, session);
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw exception;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private void setResponseContentType(VaadinRequest request,
                                        VaadinResponse response,
                                        StreamResource streamResource) {
        PortletContext context = ((VaadinPortletRequest) request)
                .getPortletContext();
        try {
            response.setContentType(streamResource.getContentTypeResolver()
                    .apply(streamResource, null));
        } catch (NullPointerException e) {
            response.setContentType(Optional
                    .ofNullable(
                            context.getMimeType(streamResource.getName()))
                    .orElse("application/octet-stream"));
        }
    }
}

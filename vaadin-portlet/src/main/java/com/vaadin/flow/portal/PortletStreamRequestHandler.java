package com.vaadin.flow.portal;

import com.vaadin.flow.server.communication.StreamRequestHandler;

/**
 * Request handler for portlet uploads.
 */
public class PortletStreamRequestHandler extends StreamRequestHandler {
    public PortletStreamRequestHandler() {
        super(new PortletStreamReceiverHandler());
    }
}

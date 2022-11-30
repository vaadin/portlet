/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletConfig;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;

/**
 * An implementation of {@link VaadinSession} for JSR-362 portlet environments.
 *
 * Only the documented parts of this class should be considered as stable public
 * API.
 *
 * @since
 */
@SuppressWarnings("serial")
public class VaadinPortletSession extends VaadinSession {

    /**
     * Create a portlet service session for the given portlet service.
     *
     * @param service
     *            the portlet service to which the new session belongs
     */
    public VaadinPortletSession(VaadinPortletService service) {
        super(service);
    }

    @Override
    protected StreamResourceRegistry createStreamResourceRegistry() {
        return new PortletStreamResourceRegistry(this);
    }

    /**
     * Returns the underlying portlet session.
     *
     * @return portlet session
     */
    public PortletSession getPortletSession() {
        WrappedSession wrappedSession = getSession();
        PortletSession session = ((WrappedPortletSession) wrappedSession)
                .getPortletSession();
        return session;
    }

    private PortletResponse getCurrentResponse() {
        VaadinPortletResponse currentResponse = (VaadinPortletResponse) CurrentInstance
                .get(VaadinResponse.class);

        if (currentResponse != null) {
            return currentResponse.getPortletResponse();
        } else {
            return null;
        }
    }

    /**
     * Returns the JSR-286 portlet configuration that provides access to the
     * portlet context and init parameters.
     *
     * @return portlet configuration
     */
    public PortletConfig getPortletConfig() {
        VaadinPortletResponse response = (VaadinPortletResponse) CurrentInstance
                .get(VaadinResponse.class);
        return response.getService().getPortlet().getPortletConfig();
    }

    /**
     * Gets the currently used session.
     *
     * @return the current session instance if available, otherwise
     *         <code>null</code>
     */
    public static VaadinPortletSession getCurrent() {
        return (VaadinPortletSession) VaadinSession.getCurrent();
    }
}

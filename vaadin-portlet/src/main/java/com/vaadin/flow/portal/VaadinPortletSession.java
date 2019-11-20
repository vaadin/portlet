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
package com.vaadin.flow.portal;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.shared.JsonConstants;

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
     *         the portlet service to which the new session belongs
     */
    public VaadinPortletSession(VaadinPortletService service) {
        super(service);

        setErrorHandler((ErrorHandler) event -> {
            VaadinPortletResponse response = VaadinPortletResponse
                    .getCurrent();
            if (response != null) {
                try {
                    getService().writeUncachedStringResponse(response,
                            JsonConstants.JSON_CONTENT_TYPE,
                            VaadinService.createCriticalNotificationJSON(
                                    event.getThrowable().getClass()
                                            .getSimpleName(),
                                    event.getThrowable().getMessage(),
                                    "Caused by: " + event.getThrowable()
                                            .getCause().getMessage(),
                                    null));
                } catch (IOException e) {
                    LoggerFactory.getLogger(VaadinPortletSession.class).error("Failed to send critical notification!", e);
                }
            }
        });
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

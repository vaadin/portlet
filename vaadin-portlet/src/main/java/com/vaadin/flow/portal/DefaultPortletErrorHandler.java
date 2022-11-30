/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.JsonConstants;

/**
 * {@link ErrorHandler} implementation which places raises exceptions to the
 * client-side, displaying the errors on the portlet which caused the exception.
 */
public class DefaultPortletErrorHandler implements ErrorHandler {
    static final String ERROR_ATTRIBUTE_NAME =
            DefaultPortletErrorHandler.class.getName() + ".error.thrown";
    private static final Logger logger = LoggerFactory
            .getLogger(DefaultPortletErrorHandler.class);

    private VaadinPortletService service;

    public DefaultPortletErrorHandler(VaadinPortletService service) {
        this.service = service;
    }

    @Override
    public void error(ErrorEvent event) {
        logger.error(event.getThrowable().toString(), event.getThrowable());

        VaadinPortletResponse response = VaadinPortletResponse.getCurrent();
        if (response != null) {
            try {
                service.writeUncachedStringResponse(
                        response, JsonConstants.JSON_CONTENT_TYPE,
                        VaadinService.createCriticalNotificationJSON(
                                event.getThrowable().getClass().getSimpleName(),
                                event.getThrowable().getMessage(),
                                getCauseString(event.getThrowable()), null,
                                getQuerySelector(response)));
                // Liferay related: tells UIDL handler not to write the sync
                // UIDL, because it corrupts RPC response in case of exception
                // see https://github.com/vaadin/portlet/issues/213
                VaadinPortletRequest.getCurrentPortletRequest().setAttribute(
                        ERROR_ATTRIBUTE_NAME, Boolean.TRUE);
            } catch (Exception e) {
                logger.error("Failed to send critical notification!", e);
            }
        }
    }

    /**
     * Define the {@code querySelector} for finding the element under which the
     * error box should be added. If the element found by the
     * {@code querySelector} has a shadow root, the error will be added into the
     * shadow instead.
     *
     * @param response
     *            the portlet response used to write the error to the
     *            client-side
     * @return {@code querySelector}
     */
    protected String getQuerySelector(VaadinPortletResponse response) {
        String nameSpace = response.getPortletResponse().getNamespace();
        return "[data-portlet-id='" + nameSpace + "']";
    }

    private static String getCauseString(Throwable throwable) {
        Throwable cause = throwable.getCause();
        String causeString = cause == null ? "N/A" : cause.getMessage();
        return "Caused by: " + causeString;
    }
}

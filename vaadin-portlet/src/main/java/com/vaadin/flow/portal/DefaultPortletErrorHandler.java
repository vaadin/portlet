package com.vaadin.flow.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.JsonConstants;

public class DefaultPortletErrorHandler implements ErrorHandler {
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
            } catch (Exception e) {
                logger.error("Failed to send critical notification!", e);
            }
        }
    }

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

/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import com.vaadin.flow.component.HasValue;

/**
 * A high-level Vaadin Portlet {@link RuntimeException}.
 */
public class VaadinPortletException extends RuntimeException {

    /**
     * Constructs a new Vaadin Portlet exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public VaadinPortletException(String message) {
        super(message);
    }


    /**
     * Constructs a new Vaadin Portlet exception with the specified detail
     * message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <code>null</code> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public VaadinPortletException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new Vaadin Portlet exception with the specified cause and a
     * detail message of <code>(cause==null ? null : cause.toString())</code>
     * (which typically contains the class and detail message of
     * <code>cause</code>).
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <code>null</code> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public VaadinPortletException(Throwable cause) {
        super(cause);
    }
}

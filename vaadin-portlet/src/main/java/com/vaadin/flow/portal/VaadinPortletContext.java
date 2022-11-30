/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletContext;
import java.util.Enumeration;
import java.util.function.Supplier;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletService;

/**
 * {@link VaadinContext} that goes with {@link VaadinServletService}.
 *
 * @author Vaadin Ltd
 * @since
 */
public class VaadinPortletContext implements VaadinContext {

    private transient PortletContext context;

    /**
     * Creates an instance of this context with given {@link PortletContext}.
     *
     * @param context
     *         Context.
     */
    public VaadinPortletContext(PortletContext context) {
        this.context = context;
    }

    /**
     * Returns the underlying context.
     *
     * @return A non-null {@link PortletContext}.
     */
    public PortletContext getContext() {
        return context;
    }

    /**
     * Ensures there is a valid instance of {@link PortletContext}.
     */
    private void ensurePortletContext() {
        if (context == null && VaadinService
                .getCurrent() instanceof VaadinPortletService) {
            context = ((VaadinPortletService) VaadinService.getCurrent())
                    .getPortlet().getPortletContext();
        } else if (context == null) {
            throw new IllegalStateException(
                    "The underlying PortletContext of VaadinPortletContext is null and there is no VaadinPorletService to obtain it from.");
        }
    }

    @Override
    public <T> T getAttribute(Class<T> type, Supplier<T> defaultValueSupplier) {
        ensurePortletContext();
        synchronized (this) {
            Object result = context.getAttribute(type.getName());
            if (result == null && defaultValueSupplier != null) {
                result = defaultValueSupplier.get();
                context.setAttribute(type.getName(), result);
            }
            return type.cast(result);
        }
    }

    @Override
    public <T> void setAttribute(Class<T> clazz, T value) {
        assert value != null;
        ensurePortletContext();
        context.setAttribute(clazz.getName(), value);
    }

    @Override
    public void removeAttribute(Class<?> clazz) {
        ensurePortletContext();
        context.removeAttribute(clazz.getName());
    }

    @Override
    public Enumeration<String> getContextParameterNames() {
        ensurePortletContext();
        return context.getInitParameterNames();
    }

    @Override
    public String getContextParameter(String name) {
        ensurePortletContext();
        return context.getInitParameter(name);
    }

}

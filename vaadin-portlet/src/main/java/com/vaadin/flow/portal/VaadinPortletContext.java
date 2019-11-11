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

import javax.portlet.PortletContext;
import java.util.Enumeration;
import java.util.function.Supplier;

import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletService;

/**
 * {@link VaadinContext} that goes with {@link VaadinServletService}.
 *
 * @since
 */
public class VaadinPortletContext implements VaadinContext {

    private static final long serialVersionUID = 3260429452395987854L;

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
    public <T> void setAttribute(T value) {
        assert value != null;
        ensurePortletContext();
        context.setAttribute(value.getClass().getName(), value);
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

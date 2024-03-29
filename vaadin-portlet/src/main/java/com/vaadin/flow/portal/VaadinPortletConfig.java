/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.server.VaadinConfig;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;

/**
 * {@link VaadinConfig} for deployment configuration.
 *
 * @author Vaadin Ltd
 * @since
 */
public class VaadinPortletConfig implements VaadinConfig {

    private transient PortletConfig config;

    /**
     * Creates an instance of this context with given {@link PortletConfig}.
     *
     * @param config
     *            PortletConfig
     */
    public VaadinPortletConfig(PortletConfig config) {
        this.config = config;
    }

    /**
     * Ensures there is a valid instance of {@link PortletConfig}.
     */
    private void ensurePortletConfig() {
        if (config == null
                && VaadinService.getCurrent() instanceof VaadinPortletService) {
            config = ((VaadinPortletService) VaadinService.getCurrent())
                    .getPortlet().getPortletConfig();
        } else if (config == null) {
            throw new IllegalStateException(String.format(
                    "The underlying %s of %s is null and there is no %s to obtain it from.",
                    PortletConfig.class.getSimpleName(),
                    VaadinPortletConfig.class.getSimpleName(),
                    VaadinPortletService.class.getSimpleName()));
        }
    }

    @Override
    public VaadinContext getVaadinContext() {
        ensurePortletConfig();
        return new VaadinPortletContext(config.getPortletContext());
    }

    @Override
    public Enumeration<String> getConfigParameterNames() {
        ensurePortletConfig();
        Set<String> initParameterNames = new HashSet<>(
                Collections.list(config.getInitParameterNames()));
        return Collections.enumeration(initParameterNames);
    }

    @Override
    public String getConfigParameter(String name) {
        ensurePortletConfig();
        return config.getInitParameter(name);
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        ensurePortletConfig();
    }
}

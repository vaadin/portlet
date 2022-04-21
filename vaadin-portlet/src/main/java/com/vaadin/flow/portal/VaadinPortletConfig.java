/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.server.Constants;
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
    static Map<String, String> forcedParameters;

    static {
        forcedParameters = Collections.emptyMap();
    }

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
        initParameterNames.addAll(forcedParameters.keySet());
        return Collections.enumeration(initParameterNames);
    }

    @Override
    public String getConfigParameter(String name) {
        ensurePortletConfig();
        String initParameter;
        if (forcedParameters.containsKey(name)) {
            initParameter = forcedParameters.get(name);
        } else {
            initParameter = config.getInitParameter(name);
        }
        return initParameter;
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

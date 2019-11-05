package com.vaadin.flow.portal;

import javax.portlet.PortletConfig;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
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
 * @since
 */
public class VaadinPortletConfig implements VaadinConfig {

    private PortletConfig config;
    final Map<String, String> forcedParameters;

    /**
     * Creates an instance of this context with given {@link PortletConfig}.
     *
     * @param config
     *         PortletConfig
     */
    public VaadinPortletConfig(PortletConfig config) {
        this.config = config;
        Map<String, String> constantParameters = new HashMap<>();
        constantParameters.put(Constants.SERVLET_PARAMETER_COMPATIBILITY_MODE,
                Boolean.FALSE.toString());
        forcedParameters = Collections.unmodifiableMap(constantParameters);
    }

    /**
     * Ensures there is a valid instance of {@link PortletConfig}.
     */
    private void ensurePortletConfig() {
        if (config == null && VaadinService
                .getCurrent() instanceof VaadinPortletService) {
            config = ((VaadinPortletService) VaadinService.getCurrent())
                    .getPortlet().getPortletConfig();
        } else if (config == null) {
            throw new IllegalStateException(
                    "The underlying PortletContext of VaadinPortletContext is null and there is no VaadinPorletService to obtain it from.");
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
        forcedParameters.keySet().forEach(initParameterNames::add);
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
}

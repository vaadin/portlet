/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.server.Constants;

/**
 * Test VaadinServletConfig property handling and function with VaadinContext.
 */
public class VaadinPortletConfigTest {

    private VaadinPortletConfig config;

    private PortletContext portletContext;
    private final Map<String, Object> attributeMap = new HashMap<>();
    private Map<String, String> properties;

    @Before
    public void setup() {
        PortletConfig portletConfig = Mockito.mock(PortletConfig.class);
        portletContext = Mockito.mock(PortletContext.class);

        Mockito.when(portletConfig.getPortletContext())
                .thenReturn(portletContext);

        Mockito.when(portletContext.getAttribute(Mockito.anyString()))
                .then(invocationOnMock -> attributeMap
                        .get(invocationOnMock.getArguments()[0].toString()));
        Mockito.doAnswer(invocationOnMock -> attributeMap
                .put(invocationOnMock.getArguments()[0].toString(),
                        invocationOnMock.getArguments()[1]))
                .when(portletContext)
                .setAttribute(Mockito.anyString(), Mockito.any());

        properties = new HashMap<>();
        properties.put(Constants.SERVLET_PARAMETER_PRODUCTION_MODE, "true");
        properties.put(Constants.SERVLET_PARAMETER_ENABLE_DEV_SERVER, "false");

        Mockito.when(portletConfig.getInitParameterNames())
                .thenReturn(Collections.enumeration(properties.keySet()));
        Mockito.when(portletConfig.getInitParameter(Mockito.anyString()))
                .then(invocation -> properties
                        .get(invocation.getArguments()[0]));
        config = new VaadinPortletConfig(portletConfig);
    }

    @Test
    public void testGetPropertyNames_returnsExpectedProperties() {
        List<String> list = Collections.list(config.getConfigParameterNames());
        Assert.assertEquals(
                "Context should return only keys defined in PortletContext",
                properties.size(), list.size());
        for (String key : properties.keySet()) {
            Assert.assertEquals(String.format(
                    "Value should be same from context for key '%s'", key),
                    properties.get(key), config.getConfigParameter(key));
        }
    }

    @Test
    public void testVaadinContextThroughConfig_setAndGetAttribute() {
        String value = "my-attribute";
        config.getVaadinContext().setAttribute(value);
        String result = config.getVaadinContext().getAttribute(String.class);
        Assert.assertEquals(value, result);
        // overwrite
        String newValue = "this is a new value";
        config.getVaadinContext().setAttribute(newValue);
        result = config.getVaadinContext().getAttribute(String.class);
        Assert.assertEquals(newValue, result);
        // now the provider should not be called, so value should be still there
        result = config.getVaadinContext().getAttribute(String.class, () -> {
            throw new AssertionError("Should not be called");
        });
        Assert.assertEquals(newValue, result);
    }
}

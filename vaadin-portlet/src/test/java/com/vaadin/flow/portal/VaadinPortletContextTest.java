package com.vaadin.flow.portal;

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
 * Tests for VaadinServletContext attribute storage and property delegation.
 */
public class VaadinPortletContextTest {

    private static String testAttributeProvider() {
        return "RELAX_THIS_IS_A_TEST";
    }

    private VaadinPortletContext context;

    private final Map<String, Object> attributeMap = new HashMap<>();
    private Map<String, String> properties;

    @Before
    public void setup() {
        PortletContext portletContext = Mockito.mock(PortletContext.class);
        Mockito.when(portletContext.getAttribute(Mockito.anyString())).then(invocationOnMock -> attributeMap.get(invocationOnMock.getArguments()[0].toString()));
        Mockito.doAnswer(invocationOnMock -> attributeMap.put(
                invocationOnMock.getArguments()[0].toString(),
                invocationOnMock.getArguments()[1]
        )).when(portletContext).setAttribute(Mockito.anyString(), Mockito.any());

        properties = new HashMap<>();
        properties.put(Constants.SERVLET_PARAMETER_COMPATIBILITY_MODE, "false");
        properties.put(Constants.SERVLET_PARAMETER_PRODUCTION_MODE, "true");
        properties.put(Constants.SERVLET_PARAMETER_ENABLE_DEV_SERVER, "false");

        Mockito.when(portletContext.getInitParameterNames())
                .thenReturn(Collections.enumeration(properties.keySet()));
        Mockito.when(portletContext.getInitParameter(Mockito.anyString()))
                .then(invocation -> properties
                        .get(invocation.getArguments()[0]));
        context = new VaadinPortletContext(portletContext);
    }

    @Test
    public void getAttributeWithProvider() {
        Assert.assertNull(context.getAttribute(String.class));

        String value = context.getAttribute(String.class,
                VaadinPortletContextTest::testAttributeProvider);
        Assert.assertEquals(testAttributeProvider(), value);

        Assert.assertEquals("Value from provider should be persisted",
                testAttributeProvider(), context.getAttribute(String.class));
    }

    @Test(expected = AssertionError.class)
    public void setNullAttributeNotAllowed() {
        context.setAttribute(null);
    }

    @Test
    public void getMissingAttributeWithoutProvider() {
        String value = context.getAttribute(String.class);
        Assert.assertNull(value);
    }

    @Test
    public void setAndGetAttribute() {
        String value = testAttributeProvider();
        context.setAttribute(value);
        String result = context.getAttribute(String.class);
        Assert.assertEquals(value, result);
        // overwrite
        String newValue = "this is a new value";
        context.setAttribute(newValue);
        result = context.getAttribute(String.class);
        Assert.assertEquals(newValue, result);
        // now the provider should not be called, so value should be still there
        result = context.getAttribute(String.class,
                () -> {
                    throw new AssertionError("Should not be called");
                });
        Assert.assertEquals(newValue, result);
    }

    @Test
    public void testGetPropertyNames_returnsExpectedProperties() {
        List<String> list = Collections.list(context.getContextParameterNames());
        Assert.assertEquals(
                "Context should return only keys defined in PortletContext",
                properties.size(), list.size());
        for (String key : properties.keySet()) {
            Assert.assertEquals(String.format(
                    "Value should be same from context for key '%s'", key),
                    properties.get(key), context.getContextParameter(key));
        }
    }

}

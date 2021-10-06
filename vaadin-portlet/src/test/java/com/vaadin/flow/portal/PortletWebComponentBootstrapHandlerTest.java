/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.VaadinService;

public class PortletWebComponentBootstrapHandlerTest {

    private PortletWebComponentBootstrapHandler handler = new PortletWebComponentBootstrapHandler();

    private DeploymentConfiguration configuration = Mockito
            .mock(DeploymentConfiguration.class);

    @Before
    public void setUp() {
        VaadinPortletService service = Mockito.mock(VaadinPortletService.class);
        Mockito.when(service.getDeploymentConfiguration())
                .thenReturn(configuration);
        VaadinService.setCurrent(service);
    }

    @After
    public void tearDown() {
        CurrentInstance.clearAll();
    }

    @Test
    public void modifyPath_staticResourcesPathIsEmpty_pathIsPrefixedWithSlash() throws Exception {
        Mockito.when(configuration.isProductionMode()).thenReturn(true);

        Mockito.when(configuration.getStringProperty(Mockito.eq(
                PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING),
                Mockito.anyString())).thenReturn("");
        String path = handler.modifyPath("bar", "./VAADIN/foo");
        Assert.assertEquals("/./VAADIN/foo", path);
    }

    @Test
    public void modifyPath_staticResourcesPathHasNoSlashes_pathIsPrefixedAndPostfixedWithSlash() throws Exception {
        Mockito.when(configuration.isProductionMode()).thenReturn(true);

        Mockito.when(configuration.getStringProperty(Mockito.eq(
                PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING),
                Mockito.anyString())).thenReturn("baz");
        String path = handler.modifyPath("bar", "./VAADIN/foo");
        Assert.assertEquals("/baz/./VAADIN/foo", path);
    }

    @Test
    public void modifyPath_staticResourcesPathHasAllSlashes_pathIsConcatenatedWithMappingURI() throws Exception {
        Mockito.when(configuration.isProductionMode()).thenReturn(true);

        Mockito.when(configuration.getStringProperty(Mockito.eq(
                PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING),
                Mockito.anyString())).thenReturn("/baz/");
        String path = handler.modifyPath("bar", "./VAADIN/foo");
        Assert.assertEquals("/baz/./VAADIN/foo", path);
    }
}

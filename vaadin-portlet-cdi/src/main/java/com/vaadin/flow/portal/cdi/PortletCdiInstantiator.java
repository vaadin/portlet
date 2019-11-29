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
package com.vaadin.flow.portal.cdi;

import javax.enterprise.inject.spi.BeanManager;

import com.vaadin.cdi.AbstractCdiInstantiator;
import com.vaadin.cdi.CdiVaadinServletService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinService;

/**
 * Instantiator for CDI-enabled Vaadin Portlets.
 *
 * @see AbstractCdiInstantiator
 */
public class PortletCdiInstantiator extends AbstractCdiInstantiator {

    private final CdiVaadinServletService.CdiVaadinServiceDelegate delegate;

    public PortletCdiInstantiator(CdiVaadinServletService.CdiVaadinServiceDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public BeanManager getBeanManager() {
        return delegate.getBeanManager();
    }

    @Override
    public <T extends Component> T createComponent(Class<T> componentClass) {
        return getOrCreate(componentClass);
    }

    @Override
    public Class<? extends VaadinService> getServiceClass() {
        return CdiVaadinPortletService.class;
    }
}

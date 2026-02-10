/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.cdi;

import jakarta.enterprise.inject.spi.BeanManager;

import com.vaadin.cdi.AbstractCdiInstantiator;
import com.vaadin.cdi.CdiVaadinServletService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.server.VaadinService;

/**
 * Instantiator for CDI-enabled Vaadin Portlets.
 *
 * @see AbstractCdiInstantiator
 */
public class PortletCdiInstantiator extends AbstractCdiInstantiator {

    private final CdiVaadinServletService.CdiVaadinServiceDelegate delegate;
    private final DefaultInstantiator defaultInstantiator;

    public PortletCdiInstantiator(VaadinService service,
            CdiVaadinServletService.CdiVaadinServiceDelegate delegate) {
        this.delegate = delegate;
        this.defaultInstantiator = new DefaultInstantiator(service);
    }

    @Override
    protected DefaultInstantiator getDelegate() {
        return defaultInstantiator;
    }

    @Override
    public BeanManager getBeanManager() {
        return delegate.getBeanManager();
    }

    @Override
    public <T extends Component> T createComponent(Class<T> componentClass) {
        return getOrCreate(componentClass);
    }
}

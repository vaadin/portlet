/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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

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
import java.util.Optional;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

import com.vaadin.cdi.CdiVaadinServletService.CdiVaadinServiceDelegate;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.portal.VaadinPortletService;
import com.vaadin.flow.server.ServiceException;

/**
 * Vaadin portlet service embellished with CDI functionality from vaadin-cdi
 * implementation.
 *
 * @see com.vaadin.cdi.CdiVaadinServletService
 */
public class CdiVaadinPortletService extends VaadinPortletService {

    private final CdiVaadinServiceDelegate delegate;

    public CdiVaadinPortletService(CdiVaadinPortlet portlet,
            DeploymentConfiguration configuration, BeanManager beanManager) {
        super(portlet, configuration);
        this.delegate = new CdiVaadinServiceDelegate(this, beanManager);
    }

    @Override
    public void init() throws ServiceException {
        delegate.init();
        super.init();
    }

    @Override
    public void fireUIInitListeners(UI ui) {
        delegate.addUIListeners(ui);
        super.fireUIInitListeners(ui);
    }

    @Override
    public CdiVaadinPortlet getPortlet() {
        return (CdiVaadinPortlet) super.getPortlet();
    }

    @Override
    public Optional<Instantiator> loadInstantiators() throws ServiceException {
        final PortletCdiInstantiator instantiator = new PortletCdiInstantiator(
                delegate);
        instantiator.init(this);
        return Optional.of(instantiator);
    }

}

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
import java.util.Optional;

import com.vaadin.cdi.CdiVaadinServiceDelegate;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.portal.VaadinPortletService;
import com.vaadin.flow.server.ServiceException;

public class CdiVaadinPortletService extends VaadinPortletService {

    private final CdiVaadinServiceDelegate delegate;

    public CdiVaadinPortletService(CdiVaadinPortlet portlet,
            DeploymentConfiguration configuration, BeanManager beanManager)
            throws ServiceException {
        super(portlet, configuration);
        this.delegate = new CdiVaadinServiceDelegate(beanManager, this);
    }

    @Override
    public void init() throws ServiceException {
        delegate.init();
        super.init();
    }

    @Override
    public void fireUIInitListeners(UI ui) {
        delegate.fireUIInitListeners(ui);
        super.fireUIInitListeners(ui);
    }

    @Override
    public CdiVaadinPortlet getPortlet() {
        return (CdiVaadinPortlet) super.getPortlet();
    }

    @Override
    protected Optional<Instantiator> loadInstantiators()
            throws ServiceException {
        return delegate.loadInstantiators();
    }
}

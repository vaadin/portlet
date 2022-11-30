/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import java.io.IOException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.portal.VaadinPortlet;
import com.vaadin.flow.portal.VaadinPortletService;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServlet;

public abstract class CdiVaadinPortlet<C extends Component>
        extends VaadinPortlet<C> {
    @Inject
    private BeanManager beanManager;

    private static final ThreadLocal<String> portletName = new ThreadLocal<>();

    @Override
    public void init(PortletConfig config)
            throws javax.portlet.PortletException {
        try {
            portletName.set(config.getPortletName());
            super.init(config);
        } finally {
            portletName.set(null);
        }
    }

    @Override
    protected void doDispatch(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        try {
            portletName.set(getPortletName());
            super.doDispatch(request, response);
        } finally {
            portletName.set(null);
        }
    }

    /**
     * Name of the Vaadin portlet for the current thread.
     * <p>
     * Until VaadinService appears in CurrentInstance, it has to be used to get
     * the portlet name.
     * <p>
     * This method is meant for internal use only.
     *
     * @see VaadinServlet#getCurrent()
     * @return currently processing vaadin portlet name
     */
    public static String getCurrentPortletName() {
        return portletName.get();
    }

    @Override
    protected VaadinPortletService createPortletService(
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        CdiVaadinPortletService service = new CdiVaadinPortletService(this,
                deploymentConfiguration, beanManager);
        service.init();
        return service;
    }
}

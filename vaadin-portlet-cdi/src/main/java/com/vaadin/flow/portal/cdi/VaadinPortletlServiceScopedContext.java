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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import com.vaadin.cdi.annotation.VaadinServiceScoped;
import com.vaadin.cdi.context.VaadinServiceScopedContext;
import com.vaadin.cdi.context.internal.AbstractContextualStorageManager;
import com.vaadin.flow.portal.VaadinPortlet;
import com.vaadin.flow.portal.VaadinPortletService;
import com.vaadin.flow.server.ServiceDestroyEvent;

import static javax.enterprise.event.Reception.IF_EXISTS;

/**
 * Context for {@link VaadinServiceScoped @VaadinServiceScoped} beans.
 */
class VaadinPortletlServiceScopedContext extends VaadinServiceScopedContext {

    public VaadinPortletlServiceScopedContext(BeanManager beanManager) {
        super(beanManager);
    }

    public void init(BeanManager beanManager) {
        setContextManager(BeanProvider.getContextualReference(beanManager,
                VaadinServiceScopedContext.ContextualStorageManager.class,
                false));
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual,
            boolean createIfNotExist) {
        CdiVaadinPortlet portlet = (CdiVaadinPortlet) VaadinPortlet
                .getCurrent();
        String portletName;
        if (portlet != null) {
            portletName = portlet.getPortletName();
        } else {
            portletName = CdiVaadinPortlet.getCurrentPortletName();
        }
        return getContextManager().getContextualStorage(portletName,
                createIfNotExist);
    }

    @Override
    public boolean isActive() {
        VaadinPortlet portlet = VaadinPortlet.getCurrent();
        return portlet instanceof CdiVaadinPortlet || (portlet == null
                && CdiVaadinPortlet.getCurrentPortletName() != null);
    }

    @ApplicationScoped
    public static class ContextualStorageManager
            extends AbstractContextualStorageManager<String> {

        public ContextualStorageManager() {
            super(true);
        }

        /**
         * Service destroy event observer.
         *
         * During application shutdown it is container specific whether this
         * observer being called, or not. Application context destroy may happen
         * earlier, and cleanup done by {@link #destroyAll()}.
         *
         * @param event
         *            service destroy event
         */
        private void onServiceDestroy(
                @Observes(notifyObserver = IF_EXISTS) ServiceDestroyEvent event) {
            if (!(event.getSource() instanceof VaadinPortletService)) {
                return;
            }
            VaadinPortletService service = (VaadinPortletService) event
                    .getSource();
            String portletName = service.getPortlet().getPortletName();
            destroy(portletName);
        }

    }

}

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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.lang.annotation.Annotation;

import org.apache.deltaspike.core.util.context.AbstractContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.cdi.context.ContextWrapper;
import com.vaadin.cdi.context.VaadinServiceScopedContext;

/**
 * Portlet specialization of CDI Extension.
 */
public class VaadinPortletExtension implements Extension {

    private VaadinServiceScopedContext serviceScopedContext;

    private void addContexts(@Observes AfterBeanDiscovery afterBeanDiscovery,
            BeanManager beanManager) {
        serviceScopedContext = new VaadinPortletlServiceScopedContext(
                beanManager);
        addContext(afterBeanDiscovery, serviceScopedContext, null);
    }

    private void initializeContexts(@Observes AfterDeploymentValidation adv,
            BeanManager beanManager) {
        serviceScopedContext.init(beanManager);
    }

    private void addContext(AfterBeanDiscovery afterBeanDiscovery,
            AbstractContext context,
            Class<? extends Annotation> additionalScope) {
        afterBeanDiscovery
                .addContext(new ContextWrapper(context, context.getScope()));
        if (additionalScope != null) {
            afterBeanDiscovery
                    .addContext(new ContextWrapper(context, additionalScope));
        }
        getLogger().info("{} registered for Vaadin Portlet CDI",
                context.getClass().getSimpleName());
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VaadinPortletExtension.class);
    }
}

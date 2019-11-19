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

import com.vaadin.cdi.VaadinExtension;
import com.vaadin.flow.portal.cdi.context.VaadinPortalServiceScopedContext;

/**
 * Portlet specialization of CDI Extension.
 */
public class VaadinPortletExtension extends VaadinExtension {

    @Override
    protected com.vaadin.cdi.context.VaadinServiceScopedContext createVaadinServiceScopedContext(
            BeanManager beanManager) {
        return new VaadinPortalServiceScopedContext(beanManager);
    }

}

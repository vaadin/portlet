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

import javax.enterprise.inject.Specializes;

import com.vaadin.cdi.CdiInstantiator;
import com.vaadin.cdi.annotation.VaadinServiceEnabled;
import com.vaadin.cdi.annotation.VaadinServiceScoped;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinService;

/**
 * Specialization of default Vaadin CDI instantiator.
 *
 * @see CdiInstantiator
 */
@VaadinServiceScoped
@VaadinServiceEnabled
@Specializes
public class PortletCdiInstantiator extends CdiInstantiator {

    @Override
    protected Class<? extends VaadinService> getServiceClass() {
        return CdiVaadinPortletService.class;
    }

    @Override
    public <T extends Component> T createComponent(Class<T> componentClass) {
        return getOrCreate(componentClass);
    }
}


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
package com.vaadin.flow.portal.handler;

import java.io.Serializable;

/**
 * Add this interface to a {@link com.vaadin.flow.portal.VaadinPortlet} view
 * (the {@link com.vaadin.flow.component.Component} subclass passed for the type
 * parameter {@code C}) to handle changes in {@link javax.portlet.PortletMode}.
 *
 * @see PortletModeChangeListener
 */
@FunctionalInterface
public interface PortletModeChangeHandler extends Serializable {

    /**
     * Invoked when the portlet mode changes.
     *
     * @param event
     *            the wevent object
     */
    void portletModeChange(PortletModeEvent event);

}

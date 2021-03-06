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
package com.vaadin.flow.portal.lifecycle;

import java.io.Serializable;

/**
 * Component that implements this interface and is the view given as type
 * parameter for {@link com.vaadin.flow.portal.VaadinPortlet} will receive
 * <i>all</i> events sent using the Portlet Hub, including events sent via
 * {@link com.vaadin.flow.portal.PortletViewContext#fireEvent(String, java.util.Map)}.
 *
 * @author Vaadin Ltd
 * @since
 */
@FunctionalInterface
public interface EventHandler extends Serializable {

    /**
     * This method gets called when an IPC event is received.
     *
     * @param event
     */
    void handleEvent(PortletEvent event);
}

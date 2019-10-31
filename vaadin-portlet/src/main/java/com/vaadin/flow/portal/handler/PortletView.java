/*
 * Copyright 2000-2018 Vaadin Ltd.
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
 * Provides a context to for portlet instance's related actions (like fire an
 * event, registering listeners, updating modes).
 *
 * @see PortletViewContext
 * @author Vaadin Ltd
 * @since
 *
 */
public interface PortletView extends Serializable {

    /**
     * This method gets called once for a portlet component if it implements
     * this interface.
     * <p>
     * Implement the interface and the method to be able to store the
     * {@code context} object and use it to invoke contextual methods.
     *
     * @param context
     *            a portlet context
     */
    void onPortletViewContextInit(PortletViewContext context);
}

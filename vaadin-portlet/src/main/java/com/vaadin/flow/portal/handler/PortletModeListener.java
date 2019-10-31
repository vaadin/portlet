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
 * A listener portlet mode change events.
 *
 * @see VaadinPortletEventContext
 * @see PortletModeHandler
 * @author Vaadin Ltd
 *
 */
public interface PortletModeListener extends Serializable {

    /**
     * Invoked when the portlet mode changes.
     *
     * @param event
     *            the event object
     */
    void portletModeChange(PortletModeEvent event);
}

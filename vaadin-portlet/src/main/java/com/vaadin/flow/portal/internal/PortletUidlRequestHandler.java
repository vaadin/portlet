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
package com.vaadin.flow.portal.internal;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.UidlRequestHandler;

/**
 * For internal use only.
 */
public class PortletUidlRequestHandler extends UidlRequestHandler {

    private static final long serialVersionUID = 2999659152945090056L;

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        return "/uidl".equals(request.getPathInfo());
    }
}

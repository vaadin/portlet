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
package com.vaadin.flow.portal;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.communication.WebComponentBootstrapHandler;

public class PortletWebComponentBootstrapHandler
        extends WebComponentBootstrapHandler {

    @Override
    protected String getServiceUrl(VaadinRequest request) {
        return VaadinPortlet.getCurrent()
                .getWebComponentUIDLRequestHandlerURL();
    }

    @Override
    protected String modifyPath(String basePath, String path) {
        // Require that the static files are available from the server root
        path = path.replaceFirst("^.VAADIN/", "./VAADIN/");
        if (path.startsWith("./VAADIN/")) {
            return "/" + path;
        }
        return super.modifyPath(basePath, path);
    }
}

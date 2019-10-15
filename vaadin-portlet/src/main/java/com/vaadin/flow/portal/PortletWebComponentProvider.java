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

import java.io.IOException;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.WebComponentProvider;

public class PortletWebComponentProvider extends WebComponentProvider {

    public PortletWebComponentProvider() {
        // Disable tag-based cache because the same tag should yield different
        // bootstrap responses in separate portlet namespaces.
        setCacheEnabled(false);
    }

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session,
                                             VaadinRequest request, VaadinResponse response) throws IOException {
        return super.synchronizedHandleRequest(session, request, response);
    }

    @Override
    protected String generateNPMResponse(
            String tagName, VaadinRequest request) {
        VaadinPortletResponse response = VaadinPortletResponse.getCurrent();

        String nameSpace = response.getPortletResponse().getNamespace();
        String webcomponentBootstrapUrl = VaadinPortlet.getCurrent()
                .getWebComponentBootstrapHandlerURL(nameSpace);
        return "var bootstrapAddress='" + webcomponentBootstrapUrl + "';\n"
                + bootstrapNpm();
    }
}

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

import java.net.URI;
import java.net.URISyntaxException;

import javax.portlet.MimeResponse;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceURL;

import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;

/**
 * Portlet-specific registry for {@link StreamResource} instances. Creates the
 * target URL with an additional suffix to direct the upload to the correct
 * portlet.
 *
 * For internal use only.
 */
class PortletStreamResourceRegistry extends StreamResourceRegistry {

    /**
     * Creates stream resource registry for provided {@code session}.
     *
     * @param session Vaadin portlet session
     */
    public PortletStreamResourceRegistry(VaadinPortletSession session) {
        super(session);
    }

    @Override
    public URI getTargetURI(AbstractStreamResource resource) {
        PortletResponse response = VaadinPortletService.getCurrentResponse()
                .getPortletResponse();
        if (response instanceof MimeResponse) {
            MimeResponse mimeResponse = (MimeResponse) response;
            ResourceURL resourceURL = mimeResponse.createResourceURL();
            resourceURL.setResourceID("/" + getURI(resource));
            try {
                return new URI(resourceURL.toString());
            } catch (URISyntaxException e) {
                // should not happen
                throw new RuntimeException(e);
            }
        } else {
            return super.getTargetURI(resource);
        }
    }
}

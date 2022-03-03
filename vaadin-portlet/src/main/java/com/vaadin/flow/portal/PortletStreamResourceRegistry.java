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
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;

/**
 * Portlet-specific registry for {@link StreamResource} instances. Creates the
 * target URL with an additional suffix to direct the upload to the correct
 * portlet.
 * <p>
 * For internal use only.
 *
 * @author Vaadin Ltd
 * @since
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
    public StreamRegistration registerResource(AbstractStreamResource resource) {
        StreamRegistration streamRegistration = super.registerResource(resource);
        return new RegistrationWrapper(streamRegistration);
    }

    /**
     * StreamRegistration implementation which embeds dynamic resource url
     * into portlet url as a 'resourceUrl' query parameter, see
     * {@link ResourceURL}.
     */
    private final class RegistrationWrapper implements StreamRegistration {

        private final StreamRegistration delegate;

        public RegistrationWrapper(StreamRegistration streamRegistration) {
            delegate = streamRegistration;
        }

        @Override
        public URI getResourceUri() {
            return getTargetURI(getResource());
        }

        @Override
        public void unregister() {
            delegate.unregister();
        }

        @Override
        public AbstractStreamResource getResource() {
            return delegate.getResource();
        }
    }
}

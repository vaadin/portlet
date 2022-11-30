/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.MimeResponse;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceURL;

import java.net.URI;
import java.net.URISyntaxException;

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
     * @param session
     *            Vaadin portlet session
     */
    public PortletStreamResourceRegistry(VaadinPortletSession session) {
        super(session);
    }

    @Override
    public URI getTargetURI(AbstractStreamResource resource) {
        return doGetUri(resource);
    }

    @Override
    public StreamRegistration registerResource(
            AbstractStreamResource resource) {
        StreamRegistration streamRegistration = super.registerResource(
                resource);
        return new RegistrationWrapper(streamRegistration);
    }

    /**
     * StreamRegistration implementation which embeds dynamic resource url into
     * portlet url as a 'resourceUrl' query parameter, see {@link ResourceURL}.
     */
    private final class RegistrationWrapper implements StreamRegistration {

        private final StreamRegistration delegate;

        public RegistrationWrapper(StreamRegistration streamRegistration) {
            delegate = streamRegistration;
        }

        @Override
        public URI getResourceUri() {
            return doGetUri(getResource());
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

    private URI doGetUri(AbstractStreamResource resource) {
        PortletResponse response = VaadinPortletService.getCurrentResponse()
                .getPortletResponse();
        if (response instanceof MimeResponse) {
            MimeResponse mimeResponse = (MimeResponse) response;
            ResourceURL resourceURL = mimeResponse.createResourceURL();
            resourceURL.setResourceID(startWithSlash(getURI(resource)));
            try {
                // In Liferay resourceURL is absolute, whereas in Pluto it is
                // relative
                URI uri = new URI(resourceURL.toString());
                if (!uri.isAbsolute()) {
                    uri = new URI("." + uri);
                }
                return uri;
            } catch (URISyntaxException e) {
                // should not happen
                throw new VaadinPortletException(e);
            }
        } else {
            return super.getTargetURI(resource);
        }
    }

    private String startWithSlash(URI uri) {
        String uriString = uri.toString();
        if (uriString.startsWith("/")) {
            return uriString;
        }
        return "/" + uriString;
    }
}

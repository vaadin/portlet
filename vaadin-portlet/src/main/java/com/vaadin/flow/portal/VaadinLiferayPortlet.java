/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import java.io.IOException;
import java.util.Scanner;

import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinRequest;

/**
 * VaadinPortlet workarounds for Liferay versions 7.2.x and 7.3.x
 *
 * Requires the implementing portlet to signal PortletHub dependency due to a Liferay bug.
 * Addresses inconsistent behaviour in injecting required Vaadin specific javascript.
 */
public abstract class VaadinLiferayPortlet<C extends Component>
        extends VaadinPortlet<C> {

    @Override
    public void renderHeaders(HeaderRequest request, HeaderResponse response) {
        // Skip most of renderHeaders for liferay portlets as it is called inconsistently between different versions (7.2, 7.3).
        // - response.addDependency() won't work with all versions
        // (ref:https://issues.liferay.com/browse/LPS-107438)
        // Update: LPS-107438 testcase works with 7.3.10 after removing
        // PortletHub dependency from portlet.xml.
        // - injected scripts may or may not actually appear on the page, and they are processed as XML (?)
        // -> do the injection in doHeaders instead

        // Calling this probably won't help (see above)
        response.addDependency("PortletHub", "javax.portlet", "3.0.0");
    }

    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (!checkStaticResourcesConfiguration()) {
            throw new PortletException(
                    "Unexpected static resources path for Vaadin Liferay Portlet");
        }

        super.doHeaders(request, response);

        /**
         * FIXME: This is pretty quaranteed to end up on the page, however...
         * 1. The script tag will appear once per portlet
         * 2. Liferay partial page update breaks the page and causes Vaadin components to render blank
         *    *if* the browser has stale Vaadin related things from the previous page.
         *
         * In other words we probably need a surgical way to scrub stale Vaadin things from users browser
         * that only runs once, even if the script can be on the page multiple times. Alternatively we can
         * just detect the problem and force a hard reload.
         */
        response.getWriter().println(
                getPortletScriptTag(request, "scripts/PortletMethods.js"));

        // we don't actually know if the portlet is a 3.0 one here, but we need to stop the IPC errors from being thrown
        // with liferay 7.3 the portlet generally fails to render if the exception is thrown
        isPortlet3.set(true);
    }

    @Override
    protected String getStaticResourcesPath() {
        return getService().getDeploymentConfiguration().getStringProperty(
        PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING,
        "/vaadin-portlet-static/");
    }

    private boolean checkStaticResourcesConfiguration() {
        return getStaticResourcesPath().startsWith("/o/");
    }

    // For portlets added to Liferay Content Pages we need to be sure that both
    // PortletMethod.js and web-component scripts are loaded into the main page,
    // and postpone the registration instruction until scripts are effectively
    // loaded, otherwise the web-component contents are not rendered at all.
    // See https://github.com/vaadin/portlet/issues/202 for details.
    @Override
    String portletElementRegistrationScript(VaadinRequest request,
            String scriptUrl, String registrationInstruction) {
        String portletMethodScriptUrl = getPortletScriptTag(
                (RenderRequest) ((VaadinPortletRequest) request)
                        .getPortletRequest(),
                "scripts/PortletMethods.js")
                        .replaceFirst("^.*src=\"([^\"]+)\".*", "$1");

        StringBuilder initScript = new StringBuilder();
        try (Scanner scanner = new Scanner(getPortletContext()
                .getResourceAsStream("scripts/LiferayPortletRegistrationHelper.js"))
                        .useDelimiter("\\A")) {
            initScript.append(scanner.next());
        }
        initScript.append("(['").append(scriptUrl).append("','")
                .append(portletMethodScriptUrl).append("'], function() { ")
                .append(registrationInstruction).append(";})");
        return initScript.toString();
    }

}

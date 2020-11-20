package com.vaadin.flow.portal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.vaadin.flow.component.Component;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * VaadinPortlet workarounds for Liferay versions 7.2.x and 7.3.x
 * 
 * Requires the implementing portlet to signal PortletHub dependency due to a Liferay bug.
 * Addresses inconsistent behaviour in injecting required Vaadin specific javascript.
 * 
 * @implNote
 * The current implemetation makes a lot of assumptions about how Liferay is deployed,
 * tested with the official docker images (notably they use tomcat). The implementation
 * is at best a first draft.
 */
public abstract class VaadinLiferayPortlet<C extends Component>
        extends VaadinPortlet<C> {

    @Override
    public void renderHeaders(HeaderRequest request, HeaderResponse response) {
        // Skip most of renderHeaders for liferay portlets as it is called inconsistently between different versions (7.2, 7.3).
        // - response.addDependency() won't work (ref: https://issues.liferay.com/browse/LPS-107438).
        // - injected scripts may or may not actually appear on the page, and they are processed as XML (?)
        // -> do the injection in doHeaders instead

        // Calling this probably won't help (see above)
        response.addDependency("PortletHub", "javax.portlet", "3.0.0");
    }

    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (!checStaticResourcesConfiguration()) {
            throw new PortletException(
                    "Unexpected static resources path for Vaadin Liferay Portlet");
        }

        super.doHeaders(request, response);

        // This can act as an alternative for missing renderHeaders() calls, but it has downsides
        // What we probably want to do if this approach sticks around is to check if this has already been injected
        if (!response.isCommitted()) {
            try {
                File portletMethodsFile = extractPortletScript(request);
                String scriptSrc = portletMethodsFile.getPath().replace(
                        PropsUtil.get(PropsKeys.LIFERAY_WEB_PORTAL_DIR),
                        getServerUrl(request));

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
                String initScript = "<script src=\"" + scriptSrc
                        + "\" type=\"text/javascript\" />";
                response.getWriter().println(initScript);
            } catch (IOException e) {
                // If we couldn't write the file, then inline the script,
                // this will be subject to Liferay processing, once per portlet
                String initScript = "<script type=\"text/javascript\">\n"
                        + getPortletScript(request) + "\n</script>";

                response.getWriter().println(initScript);
            }
        }

        // we don't actually know if the portlet is a 3.0 one here, but we need to stop the IPC errors from being thrown
        // with liferay 7.3 the portlet generally fails to render if the exception is thrown
        isPortlet3.set(true);
    }

    private boolean checStaticResourcesConfiguration() {
        String vaadinPortletStaticResorces = getService()
                .getDeploymentConfiguration().getStringProperty(
                        PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING,
                        "/vaadin-portlet-static/");

        return "/o/vaadin-portlet-static/".equals(vaadinPortletStaticResorces);
    }

    private static String getServerUrl(RenderRequest req) {
        int port = req.getServerPort();
        String scheme = req.getScheme();
        boolean isDefaultPort = (scheme == "http" && port == 80)
                || (scheme == "https" && port == 443);

        return String.format("%s://%s%s/", scheme, req.getServerName(),
                (isDefaultPort ? "" : ":" + port));
    }

    /**
     * Utility method for extracting PortletMethods to Liferay's web root
     * 
     * This is done to avoid Liferay postprocessing the included script,
     * and it saves a bit of data since the script itself is not dynamic.
     */
    private File extractPortletScript(RenderRequest request)
            throws PortletException, IOException {
        // FIXME: clean up after previous versions, can we assume less about the paths?
        byte[] portletScript = getPortletScript(request)
                .getBytes(StandardCharsets.UTF_8);
        File htmlRoot = new File(
                PropsUtil.get(PropsKeys.LIFERAY_WEB_PORTAL_DIR));

        String scriptVersion = VaadinPortletService.getPortletVersion();
        if (scriptVersion.endsWith("-SNAPSHOT")) {
            scriptVersion = DigestUtils.sha256Hex(portletScript);
        }

        File portletMethodsFile = new File(htmlRoot, String
                .format("html/VAADIN/PortletMethods.%s.js", scriptVersion));

        if (!portletMethodsFile.exists()) {
            if (!htmlRoot.canWrite()) {
                throw new IOException(
                        "Liferay web directory cannot be written to");
            }

            File vaadinAssets = new File(htmlRoot, "html/VAADIN");
            if (!vaadinAssets.isDirectory()) {
                vaadinAssets.mkdirs();
            }

            Files.write(portletMethodsFile.toPath(), portletScript,
                    StandardOpenOption.CREATE);
        }

        return portletMethodsFile;
    }
}

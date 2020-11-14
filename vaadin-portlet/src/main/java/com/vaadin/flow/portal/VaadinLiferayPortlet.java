package com.vaadin.flow.portal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortalContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.vaadin.flow.component.Component;

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Element;

/**
 * VaadinPortlet workarounds for Liferay versions 7.2.x and 7.3.x
 * 
 * Requires the implementing portlet to signal PortletHub dependency due to a Liferay bug.
 * Addresses inconsistent behaviour in injecting required Vaadin specific javascript.
 * 
 * @implNote
 * In Liferay 7.2 changing portlet mode can trigger a full page reload.
 * 
 * @implNote
 * The current implemetation makes a lot of assumptions about how Liferay is deployed,
 * tested with the official docker images (notably they use tomcat). The implementation
 * is at best a first draft.
 */
public abstract class VaadinLiferayPortlet<C extends Component> extends VaadinPortlet<C> {

    private static final String PORTLET_SCRIPTS_HASH = PORTLET_SCRIPTS + "-hash";

	@Override
    public void renderHeaders(HeaderRequest request, HeaderResponse response) {
        // Skip most of renderHeaders for liferay portlets as it is called inconsistently between different versions (7.2, 7.3).
        // - response.addDependency() won't work (ref: https://issues.liferay.com/browse/LPS-107438).
        // - injected scripts may or may not actually appear on the page, and they are processed as XML (?)
        // -> do the injection in doHeaders using MARKUP_HEAD_ELEMENT_SUPPORT instead

        // Calling this probably won't help (see above)
        response.addDependency("PortletHub", "javax.portlet", "3.0.0");
    }

    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        super.doHeaders(request, response);

        if (request.getPortalContext().getProperty(PortalContext.MARKUP_HEAD_ELEMENT_SUPPORT) != null) {
            // This can act as an alternative for missing renderHeaders() calls, but it has downsides
            // FIXME: clean up after previous versions, can we assume less about the paths?
            if (!checStaticResourcesConfiguration())
                throw new PortletException("Unexpected static resources path for Vaadin Liferay Portlet");

            byte[] portletScript = getPortletScript(request).getBytes(StandardCharsets.UTF_8);
            File htmlRoot = new File(getPublicHtmlPath());

            // for the suffix we'd probably like something actually safe and cheap here :)
            File portletMethodsFile = new File(htmlRoot,
                String.format("VAADIN/PortletMethods.%s.js", getPortletScriptsHash(request, portletScript)));

            if (!portletMethodsFile.exists()) {
                if (!htmlRoot.canWrite())
                    throw new IOException("Unable to write PortletMethods.js to Vaadin resosources directory");

                File vaadinAssets = new File(htmlRoot, "VAADIN");
                if (!vaadinAssets.isDirectory())
                    vaadinAssets.mkdirs();

                Files.write(portletMethodsFile.toPath(), portletScript, StandardOpenOption.CREATE);
            }

            // What we probably want to do if this approach sticks around is to check if this has already been injected
            if (!response.isCommitted()) {
                String scriptSrc = portletMethodsFile.getPath().replace(PropsUtil.get(PropsKeys.LIFERAY_WEB_PORTAL_DIR), "");
                Element htmlHeader = response.createElement("script");
                htmlHeader.setAttribute("type", "text/javascript");
                htmlHeader.setAttribute("src", getServerUrl(request) + scriptSrc);

                response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, htmlHeader);
            }

            // we don't actually know if the portlet is a 3.0 one here, but we need to stop the IPC errors from being thrown
            // with liferay 7.3 the portlet generally fails to render if the exception is thrown
            isPortlet3.set(true);
        }
    }

    private String getPortletScriptsHash(RenderRequest req, byte[] portletScripts) {
        String scriptHash = (String) req.getPortletContext()
                .getAttribute(PORTLET_SCRIPTS_HASH);

        if (scriptHash == null) {
            scriptHash = DigestUtils.sha256Hex(portletScripts);
            req.getPortletContext().setAttribute(PORTLET_SCRIPTS_HASH, scriptHash);
        }
        return scriptHash;
    }

    private boolean checStaticResourcesConfiguration() {
        String vaadinPortletStaticResorces = getService().getDeploymentConfiguration().getStringProperty(
            PortletConstants.PORTLET_PARAMETER_STATIC_RESOURCES_MAPPING,
            "/vaadin-portlet-static/");

        return vaadinPortletStaticResorces.endsWith("/html/") || "/o/vaadin-portlet-static/".equals(vaadinPortletStaticResorces);
    }

    private static String getPublicHtmlPath() {
        String webPortalDir = PropsUtil.get(PropsKeys.LIFERAY_WEB_PORTAL_DIR);
        return webPortalDir.endsWith("/") ? webPortalDir + "html" : "/html";
    }

    private static String getServerUrl(RenderRequest req) {
        int port = req.getServerPort();
        String scheme = req.getScheme();

        return String.format("%s://%s%s/", scheme, req.getServerName(),
            ((scheme == "http" && port != 80) || (scheme == "https" && port != 443) ? ":" + port : ""));
    }

}

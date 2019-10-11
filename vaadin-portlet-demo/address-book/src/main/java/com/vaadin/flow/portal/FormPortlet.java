package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ActionURL;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;

public class FormPortlet extends VaadinPortlet<FormPortletView> {

    public static final String TAG = "form-portlet";
    private PortletMode mode = PortletMode.UNDEFINED;
    private WindowState windowState = WindowState.UNDEFINED;
    private SelectHandler handler;
    private String actionUrl;

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

    public static FormPortlet getCurrent() {
        return (FormPortlet) VaadinPortlet.getCurrent();
    }

    public PortletMode getPortletMode() {
        return mode;
    }

    public void setPortletMode(PortletMode portletMode, Element element) {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();

        String getHub = "var hub = window.Vaadin.Flow.Portlets[$0];";
        String createStateObject = "var state = hub.newState();";
        String setPortletMode = String.format("state.portletMode = $1;");

        String setState = "hub.setRenderState(state);";
        StringBuilder reloader = new StringBuilder();
        reloader.append("const poller = () => {");
        reloader.append("  if(hub.isInProgress()) {");
        reloader.append("    setTimeout(poller, 10);");
        reloader.append("  } else {");
        reloader.append("    location.reload();");
        reloader.append("  }");
        reloader.append("};");
        reloader.append("poller();");

        element.executeJs(getHub + createStateObject + setPortletMode + setState
                        + reloader.toString(), portletRegistryName,
                portletMode.toString());
    }

    public WindowState getWindowState() {
        return windowState;
    }

    public void sendRefreshEvent() {

    }

    public void setSelectHandler(SelectHandler handler) {
        this.handler = handler;
    }

    public void setWindowState(WindowState state) {
        String stateChangeScript = String
                .format("location.href = '%s?state=%s'", actionUrl, state);

        UI.getCurrent().getPage().executeJs(stateChangeScript);
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);
        PortletMode oldMode = mode;
        mode = request.getPortletMode();
        if (!oldMode.equals(mode) && handler != null
                && handler instanceof PortletModeHandler) {
            ((PortletModeHandler) handler)
                    .portletModeChange(new PortletModeEvent(mode));
            // This would probably need to fire a push or generate a UIDL request!
        }
        WindowState oldState = windowState;
        windowState = request.getWindowState();
        if (!oldState.equals(windowState) && handler != null
                && handler instanceof WindowStateHandler) {
            ((WindowStateHandler) handler)
                    .windowStateChange(new WindowStateEvent(windowState));
            // This would probably need to fire a push or generate a UIDL request!
        }

        if (isViewInstanceOf(WindowStateHandler.class)) {
            if (actionUrl == null) {
                ActionURL actionURL = response.createActionURL();
                actionUrl = actionURL.toString();
            }
        }
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException {
        if (request.getActionParameters().getNames().contains("state")) {
            response.setWindowState(new WindowState(
                    request.getActionParameters().getValue("state")));
        }
    }

    @Override
    public void processEvent(EventRequest request, EventResponse response)
            throws PortletException {
        super.processEvent(request, response);
        if ("Selection".equals(request.getEvent().getName())) {
            Integer contactId = Integer.parseInt(
                    request.getRenderParameters().getValue("contactId"));
            handler.select(contactId);
        }
    }

    public void registerHub(Element element) {
        try {
            String portletRegistryName = VaadinPortletService
                    .getCurrentResponse().getPortletResponse().getNamespace();
            String registerPortlet = IOUtils.toString(
                    GridPortlet.class.getClassLoader()
                            .getResourceAsStream("PortletHubRegistration.js"),
                    StandardCharsets.UTF_8);
            element.executeJs(registerPortlet, portletRegistryName, element);
        } catch (IOException e) {
        }
    }
}

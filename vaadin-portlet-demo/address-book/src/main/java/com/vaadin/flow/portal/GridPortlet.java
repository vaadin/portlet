package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ActionURL;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.portal.handler.WindowStateHandler;

public class GridPortlet extends VaadinPortlet<GridPortletView> {

    public static final String TAG = "grid-portlet";
    private static final String SELECTION_EVENT = "Selection";
    private String EVENT_NAMESPACE = "";

    private WindowState windowState = WindowState.UNDEFINED;
    private String actionUrl;

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);
        windowState = request.getWindowState();

        if (isViewInstanceOf(WindowStateHandler.class)) {
            if (actionUrl == null) {
                ActionURL actionURL = response.createActionURL();
                actionUrl = actionURL.toString();
            }
        }
    }

    public static GridPortlet getCurrent() {
        return (GridPortlet) VaadinPortlet.getCurrent();
    }

    public WindowState getWindowState() {
        return windowState;
    }

    public void setWindowState(WindowState state) {
        String stateChangeScript = String
                .format("location.href = '%s?state=%s'", actionUrl, state);

        UI.getCurrent().getPage().executeJs(stateChangeScript);
    }

    public void sendContactSelectionEvent(Contact contact, Element element) {
        String portletRegistryName = VaadinPortletService.getCurrentResponse()
                .getPortletResponse().getNamespace();

        String getHub = "var hub = window.Vaadin.Flow.Portlets[$0];";
        String generateParameters = "var params = hub.newParameters();";
        String setValues = "params['action'] = ['send'];"
                + "params['selection'] = ['" + contact.getId() + "'];";
        String fireAction = "hub.action(params);";

        element.executeJs(getHub + generateParameters + setValues + fireAction,
                portletRegistryName, element);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException {

        if (request.getActionParameters().getNames().contains("state")) {
            response.setWindowState(new WindowState(
                    request.getActionParameters().getValue("state")));
        } else {
            String selection = request.getActionParameters().getValue("selection");
            response.getRenderParameters().setValue("contactId", selection);
            QName qn = new QName(EVENT_NAMESPACE, SELECTION_EVENT);
            response.setEvent(qn, "Selection event");
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

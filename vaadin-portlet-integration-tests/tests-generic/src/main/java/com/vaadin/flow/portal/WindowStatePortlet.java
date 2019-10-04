package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ActionURL;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;

public abstract class WindowStatePortlet extends VaadinPortlet {
    private String action;
    private WindowState state;

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);
        state = request.getWindowState();

        if(action == null) {
            ActionURL actionURL = response.createActionURL();
            action = actionURL.toString();
        }
    }

    public WindowState getWindowState() {
        return state;
    }

    public String getActionUrl() {
        return action;
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException {
        if(request.getActionParameters().getNames().contains("state")) {
            response.setWindowState(new WindowState(
                    request.getActionParameters().getValue("state")));
        }
    }
}

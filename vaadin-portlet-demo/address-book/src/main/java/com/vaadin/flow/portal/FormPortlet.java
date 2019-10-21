package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.WindowState;

import com.vaadin.flow.component.Component;

public class FormPortlet extends VaadinPortlet<FormPortletView> {

    public static final String TAG = "form-portlet";
    private Component portletView;

    public static FormPortlet getCurrent() {
        return (FormPortlet) VaadinPortlet.getCurrent();
    }

    public void setPortletView(Component portletView) {
        this.portletView = portletView;
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException {
        if (request.getActionParameters().getNames()
                .contains("selection")) {
            if(portletView != null && portletView instanceof EventHandler) {
                ((EventHandler)portletView).handleEvent(new PortletEvent("selection", request.getParameterMap()));
            }
            if (request.getActionParameters().getValue("windowState") != null) {
                response.setWindowState(new WindowState(
                        request.getRenderParameters().getValue("windowState")));
            }
        }
    }
}

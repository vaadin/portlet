package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.WindowState;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;

public class FormPortlet extends TheseInVaadinPortlet<FormPortletView> {

    public static final String TAG = "form-portlet";
    private Component portletView;

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

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

    @Override
    protected void fireModeChange(PortletModeEvent event) {
        if (portletView != null && portletView instanceof PortletModeHandler) {
            ((PortletModeHandler) portletView).portletModeChange(event);
        }
    }

    @Override
    protected void fireStateChange(WindowStateEvent event) {
        if (portletView != null && portletView instanceof WindowStateHandler) {
            ((WindowStateHandler) portletView).windowStateChange(event);
        }
    }
}

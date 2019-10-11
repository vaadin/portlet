package com.vaadin.flow.portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.WindowState;

import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;
import com.vaadin.flow.portal.handler.WindowStateEvent;
import com.vaadin.flow.portal.handler.WindowStateHandler;

public class FormPortlet extends TheseInVaadinPortlet<FormPortletView> {

    public static final String TAG = "form-portlet";
    private SelectHandler handler;

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

    public static FormPortlet getCurrent() {
        return (FormPortlet) VaadinPortlet.getCurrent();
    }

    public void setSelectHandler(SelectHandler handler) {
        this.handler = handler;
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
            if (request.getRenderParameters().getValue("windowState") != null) {
                setWindowState(new WindowState(
                        request.getRenderParameters().getValue("windowState")));
            }
        }
    }

    @Override
    protected void fireModeChange(PortletModeEvent event) {
        if (handler != null && handler instanceof PortletModeHandler) {
            ((PortletModeHandler) handler).portletModeChange(event);
        }
    }

    @Override
    protected void fireStateChange(WindowStateEvent event) {
        if (handler != null && handler instanceof WindowStateHandler) {
            ((WindowStateHandler) handler).windowStateChange(event);
        }
    }
}

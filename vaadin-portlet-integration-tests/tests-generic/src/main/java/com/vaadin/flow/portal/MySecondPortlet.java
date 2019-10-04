package com.vaadin.flow.portal;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.RenderURL;
import javax.portlet.WindowState;
import java.io.IOException;

public class MySecondPortlet extends VaadinPortlet {

    public static final String TAG = "my-second-portlet";

    String maximizeAction;
    String minimizeAction;
    String normalizeAction;
    WindowState state;

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);
        state = request.getWindowState();

        RenderURL renderURL = response.createRenderURL();
        renderURL.setWindowState(WindowState.MAXIMIZED);
        maximizeAction = renderURL.toString();

        renderURL = response.createRenderURL();
        renderURL.setWindowState(WindowState.MINIMIZED);
        minimizeAction = renderURL.toString();

        renderURL = response.createRenderURL();
        renderURL.setWindowState(WindowState.NORMAL);
        normalizeAction = renderURL.toString();
    }

    public WindowState getWindowState() {
        return state;
    }

    @Override
    public void init() throws PortletException {
    }

    @Override
    protected String getMainComponentTag() throws PortletException {
        return TAG;
    }
}

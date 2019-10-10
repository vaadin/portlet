package com.vaadin.flow.portal;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

public class GridPortlet extends VaadinPortlet<GridPortletView> {

    public static final String TAG = "grid-portlet";

    @Override
    protected String getMainComponentTag() {
        return TAG;
    }

    @Override
    public void render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        super.render(request, response);
//        response.
    }
}

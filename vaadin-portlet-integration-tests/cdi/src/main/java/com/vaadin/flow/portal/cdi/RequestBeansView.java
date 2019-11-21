package com.vaadin.flow.portal.cdi;

import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.portlet.annotations.PortletRequestScoped;

import java.io.Serializable;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/**
 * Exercises handling of pre-defined {@link PortletRequestScoped} beans
 * {@link PortletMode} and {@link WindowState}.
 */
public class RequestBeansView extends Div {

    @PortletRequestScoped
    public static class RequestScopedBean implements Serializable {

        @Inject
        WindowState windowState;

        @Inject
        PortletMode portletMode;

        public String getWindowState() {
            return windowState.toString();
        }

        public String getPortletMode() {
            return portletMode.toString();
        }
    }

    @Inject
    RequestScopedBean requestScopedBean;

    static final String WINDOW_STATE_LABEL_ID = "windowStateLabel";
    static final String PORTLET_MODE_LABEL_ID = "portletModeLabels";

    public RequestBeansView() {
        final Span windowStateLabel = new Span();
        windowStateLabel.setId(WINDOW_STATE_LABEL_ID);
        add(windowStateLabel);

        final Span portletModeLabel = new Span();
        portletModeLabel.setId(PORTLET_MODE_LABEL_ID);
        add(portletModeLabel);

        addAttachListener(e -> {
            windowStateLabel.setText(requestScopedBean.getWindowState());
            portletModeLabel.setText(requestScopedBean.getPortletMode());

        });
    }
}

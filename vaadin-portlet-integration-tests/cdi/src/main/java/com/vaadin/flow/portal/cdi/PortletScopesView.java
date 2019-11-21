package com.vaadin.flow.portal.cdi;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.portlet.annotations.PortletRequestScoped;
import javax.portlet.annotations.PortletSessionScoped;

import java.io.Serializable;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/**
 * Exercises handling of pre-defined {@link PortletRequestScoped} beans
 * {@link PortletMode} and {@link WindowState}.
 */
public class PortletScopesView extends Div {

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

    @PortletSessionScoped
    public static class SessionScopedBean implements Serializable {
        int counter = 0;

        public int getCounter() {
            return counter;
        }

        public void incrementCounter() {
            counter++;
        }
    }

    @Inject
    RequestScopedBean requestScopedBean;

    @Inject
    SessionScopedBean sessionScopedBean;

    static final String WINDOW_STATE_LABEL_ID = "windowStateLabel";
    static final String PORTLET_MODE_LABEL_ID = "portletModeLabel";
    static final String ATTACH_COUNTER_LABEL_ID = "attachCounterLabel";

    public PortletScopesView() {
        final Span windowStateLabel = new Span();
        windowStateLabel.setId(WINDOW_STATE_LABEL_ID);
        add(windowStateLabel);

        final Span portletModeLabel = new Span();
        portletModeLabel.setId(PORTLET_MODE_LABEL_ID);
        add(portletModeLabel);

        final Span attachCounterLabel = new Span();
        attachCounterLabel.setId(ATTACH_COUNTER_LABEL_ID);
        add(attachCounterLabel);

        addAttachListener(e -> {
            windowStateLabel.setText(requestScopedBean.getWindowState());
            portletModeLabel.setText(requestScopedBean.getPortletMode());
            sessionScopedBean.incrementCounter();
            attachCounterLabel
                    .setText(Integer.toString(sessionScopedBean.getCounter()));
        });
    }
}

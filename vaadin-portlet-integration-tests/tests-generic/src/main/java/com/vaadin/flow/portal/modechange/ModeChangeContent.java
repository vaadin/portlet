package com.vaadin.flow.portal.modechange;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.portal.handler.PortletModeEvent;
import com.vaadin.flow.portal.handler.PortletModeHandler;

public class ModeChangeContent extends Div implements PortletModeHandler {

    static final String MODE_LABEL_ID = "mode_label_id";

    private final Span label;

    public ModeChangeContent() {
        label = new Span();
        label.setId(MODE_LABEL_ID);
        add(label);
    }

    @Override
    public void portletModeChange(PortletModeEvent event) {
        label.setText(event.getPortletMode().toString());
    }
}

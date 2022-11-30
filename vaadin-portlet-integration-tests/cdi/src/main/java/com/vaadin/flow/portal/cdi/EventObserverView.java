/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.cdi;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.portlet.annotations.PortletSessionScoped;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;

public class EventObserverView extends Div {

    public static class Portlet extends CdiVaadinPortlet<EventObserverView> {
    }

    @PortletSessionScoped
    public static class EventObserver implements Serializable {
        private List<EventObject> eventsObserved = new ArrayList<>();

        private void onSessionInit(
                @Observes SessionInitEvent sessionInitEvent) {
            eventsObserved.add(sessionInitEvent);
        }

        private void onUIInit(@Observes UIInitEvent uiInitEvent) {
            eventsObserved.add(uiInitEvent);
        }

        public List<EventObject> getObservedEvents() {
            return eventsObserved;
        }
    }

    public static final String POPULATE_EVENTS_BUTTON_ID = "populateEventsButton";
    public static final String EVENT_LABEL_CLASS = "event-label";

    @Inject
    private EventObserver eventObserver;

    public EventObserverView() {
        VerticalLayout eventContainer = new VerticalLayout();
        add(eventContainer);

        Button populateEventsButton = new Button(
                "Populate with observed events", e -> {
                    eventContainer.removeAll();
                    eventObserver.getObservedEvents().forEach(event -> {
                        final Span span = new Span(event.getClass().toString());
                        span.addClassName(EVENT_LABEL_CLASS);
                        eventContainer.add(span);
                    });
                });
        populateEventsButton.setId(POPULATE_EVENTS_BUTTON_ID);
        add(populateEventsButton);
    }

}

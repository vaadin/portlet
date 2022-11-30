/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.cdi;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;

public class EventObserverIT extends AbstractPlutoPortalTest {

    public EventObserverIT() {
        super("cdi", "event-observer");
    }

    @Test
    public void receivesSessionAndUIInitEvents() {
        // ask to populate event list
        getVaadinPortletRootElement().$("*")
                .id(EventObserverView.POPULATE_EVENTS_BUTTON_ID).click();

        // wait for event labels to be added
        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class", EventObserverView.EVENT_LABEL_CLASS)
                .exists());

        // we should have received SessionInitEvent and UIInitEvent
        List<String> events = getVaadinPortletRootElement().$("*")
                .attributeContains("class", EventObserverView.EVENT_LABEL_CLASS)
                .all().stream().map(se -> se.getText())
                .collect(Collectors.toList());
        Assert.assertTrue("expected SessionInitEvent",
                events.contains(SessionInitEvent.class.toString()));
        Assert.assertTrue("expected UIInitEvent",
                events.contains(UIInitEvent.class.toString()));
    }
}

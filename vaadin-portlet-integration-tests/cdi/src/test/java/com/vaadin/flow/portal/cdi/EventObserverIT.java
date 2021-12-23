/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

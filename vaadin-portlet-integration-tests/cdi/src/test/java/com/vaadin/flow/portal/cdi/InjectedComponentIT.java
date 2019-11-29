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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.LabelElement;

public class InjectedComponentIT extends AbstractPlutoPortalTest {

    public InjectedComponentIT() {
        super("injected-component");
    }

    @Test
    public void injectedComponentIsAddedAndPreserved() {
        // wait for label to be added
        waitUntil(
                driver -> $(LabelElement.class)
                        .attributeContains("class",
                                InjectedComponentView.INJECTED_LABEL_CLASS)
                        .exists());

        // check that label contains a random integer
        final List<LabelElement> labels = $(LabelElement.class)
                .attributeContains("class",
                        InjectedComponentView.INJECTED_LABEL_CLASS)
                .all();
        Assert.assertEquals(1, labels.size());
        final int labelContents = Integer.parseInt(labels.get(0).getText());

        // reload the portal page
        driver.navigate().refresh();

        waitUntil(
                driver -> $(LabelElement.class)
                        .attributeContains("class",
                                InjectedComponentView.INJECTED_LABEL_CLASS)
                        .exists());

        // check that label contains the same random integer
        final List<LabelElement> labelsAfterRefresh = $(LabelElement.class)
                .attributeContains("class",
                        InjectedComponentView.INJECTED_LABEL_CLASS)
                .all();
        Assert.assertEquals(1, labels.size());
        final int labelContentsAfterRefresh = Integer
                .parseInt(labelsAfterRefresh.get(0).getText());
        Assert.assertEquals(labelContents, labelContentsAfterRefresh);
    }
}

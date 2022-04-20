/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.cdi;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class InjectedComponentIT extends AbstractPlutoPortalTest {

    public InjectedComponentIT() {
        super("cdi", "injected-component");
    }

    @Test
    public void injectedComponentIsAddedAndPreserved() {
        // wait for label to be added
        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class",
                        InjectedComponentView.INJECTED_LABEL_CLASS).exists());

        // check that label contains a random integer
        final List<TestBenchElement> labels = getVaadinPortletRootElement().$(
                "*").attributeContains("class",
                InjectedComponentView.INJECTED_LABEL_CLASS).all();
        Assert.assertEquals(1, labels.size());
        final int labelContents = Integer.parseInt(labels.get(0).getText());

        // reload the portal page
        driver.navigate().refresh();

        waitUntil(driver -> getVaadinPortletRootElement().$("*")
                .attributeContains("class",
                        InjectedComponentView.INJECTED_LABEL_CLASS).exists());

        // check that label contains the same random integer
        final List<TestBenchElement> labelsAfterRefresh = getVaadinPortletRootElement().$(
                "*").attributeContains("class",
                InjectedComponentView.INJECTED_LABEL_CLASS).all();
        Assert.assertEquals(1, labels.size());
        final int labelContentsAfterRefresh = Integer.parseInt(
                labelsAfterRefresh.get(0).getText());
        Assert.assertEquals(labelContents, labelContentsAfterRefresh);
    }
}

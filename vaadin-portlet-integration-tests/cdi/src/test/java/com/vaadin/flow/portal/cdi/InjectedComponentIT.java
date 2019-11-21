package com.vaadin.flow.portal.cdi;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.LabelElement;

public class InjectedComponentIT extends AbstractPlutoPortalTest {

    public InjectedComponentIT() {
        super("injectedcomponent");
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

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.cdi;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;

public class I18NProviderIT extends AbstractPlutoPortalTest {

    public I18NProviderIT() {
        super("cdi", "i18n-provider");
    }

    @Test
    public void i18NProviderTranslatesKeyCorrectly() {
        final String label1 = getVaadinPortletRootElement().$("*")
                .attributeContains("id", I18NProviderView.TRANSLATED_LABEL1_ID)
                .waitForFirst().getText();
        Assert.assertEquals("translation", label1);

        final String label2 = getVaadinPortletRootElement().$("*")
                .id(I18NProviderView.TRANSLATED_LABEL1_ID).getText();
        Assert.assertEquals("translation", label2);
    }

    @Test
    public void i18NProviderCreatedOnlyOnce() {
        openInAnotherWindow();

        final int counter = Integer.parseInt(
                getVaadinPortletRootElement().$("*").attributeContains("id",
                                I18NProviderView.COUNTER_LABEL_ID).waitForFirst()
                        .getText());
        Assert.assertEquals("I18NProvider expected initialized only once",
                counter, 1);
    }
}

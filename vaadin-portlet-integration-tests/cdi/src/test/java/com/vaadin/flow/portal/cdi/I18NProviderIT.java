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
        final String label1 = getVaadinPortletRootElement().$("*").first()
                .$(SpanElement.class)
                .attributeContains("id", I18NProviderView.TRANSLATED_LABEL1_ID)
                .waitForFirst().getText();
        Assert.assertEquals("translation", label1);

        final String label2 = getVaadinPortletRootElement().$("*").first()
                .$(SpanElement.class).id(I18NProviderView.TRANSLATED_LABEL1_ID)
                .getText();
        Assert.assertEquals("translation", label2);
    }

    @Test
    public void i18NProviderCreatedOnlyOnce() {
        openInAnotherWindow();

        final int counter = Integer.parseInt(
                getVaadinPortletRootElement().$("*").first()
                        .$(SpanElement.class).attributeContains("id",
                                I18NProviderView.COUNTER_LABEL_ID).waitForFirst()
                        .getText());
        Assert.assertEquals("I18NProvider expected initialized only once",
                counter, 1);
    }
}

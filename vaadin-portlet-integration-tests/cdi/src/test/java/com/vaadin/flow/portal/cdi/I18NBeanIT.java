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

public class I18NBeanIT extends AbstractPlutoPortalTest {

    public I18NBeanIT() {
        super("i18n-bean");
    }

    @Test
    public void i18nTranslationWorks() {
        waitUntil(driver -> $(SpanElement.class)
                .attributeContains("id", I18NBeanView.TRANSLATED_LABEL_ID)
                .exists());
        final String label1 = $(SpanElement.class)
                .id(I18NBeanView.TRANSLATED_LABEL_ID).getText();
        Assert.assertEquals("translation", label1);
    }

    // TODO: add a test validating that the I18NProvider implementation is
    // instantiated in service scope (i.e., only once).
}
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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.cdi.annotation.VaadinServiceEnabled;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.i18n.I18NProvider;

import static java.util.Locale.ENGLISH;

public class I18NProviderView extends Div {

    public static class Portlet extends CdiVaadinPortlet<I18NProviderView> {
        public static AtomicInteger counter = new AtomicInteger(0);

        public String getTag() {
            return "i18n-portlet";
        }
    }

    @VaadinServiceEnabled
    @ApplicationScoped
    public static class I18N implements I18NProvider {

        public I18N() {
            System.out.println(
                    "Constructing the I18NProvider, class is " + getClass());
        }

        @PostConstruct
        public void init() {
            Portlet.counter.incrementAndGet();
        }

        @Override
        public List<Locale> getProvidedLocales() {
            return Arrays.asList(ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale,
                Object... params) {
            return "test_key".equals(key) ? "translation" : null;
        }
    }

    public static final String TRANSLATED_LABEL1_ID = "translatedLabel1";
    public static final String TRANSLATED_LABEL2_ID = "translatedLabel2";
    public static final String COUNTER_LABEL_ID = "counterLabel";

    @VaadinServiceEnabled
    @Inject
    I18NProvider i18nProvider;

    @PostConstruct
    private void init() {
        final Span label1 = new Span(getTranslation("test_key", ENGLISH));
        label1.setId(TRANSLATED_LABEL1_ID);
        add(label1);

        final Span label2 = new Span(
                i18nProvider.getTranslation("test_key", ENGLISH));
        label2.setId(TRANSLATED_LABEL2_ID);
        add(label2);

        final Span instanceCounter = new Span(
                Integer.toString(Portlet.counter.get()));
        instanceCounter.setId(COUNTER_LABEL_ID);
        add(instanceCounter);
    }

}

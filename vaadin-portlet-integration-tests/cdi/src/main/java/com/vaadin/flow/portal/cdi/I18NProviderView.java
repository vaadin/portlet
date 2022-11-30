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
    }

    public static AtomicInteger counter = new AtomicInteger(0);

    @VaadinServiceEnabled
    @ApplicationScoped
    public static class I18N implements I18NProvider {

        @PostConstruct
        public void init() {
            counter.incrementAndGet();
        }

        @Override
        public List<Locale> getProvidedLocales() {
            return Arrays.asList(ENGLISH);
        }

        @Override
        public String getTranslation(String key, Locale locale,
                Object... params) {
            return I18N_TEST_KEY.equals(key) ? "translation" : null;
        }
    }

    public static final String TRANSLATED_LABEL1_ID = "translatedLabel1";
    public static final String TRANSLATED_LABEL2_ID = "translatedLabel2";
    public static final String COUNTER_LABEL_ID = "counterLabel";
    private static final String I18N_TEST_KEY = "test_key";

    @VaadinServiceEnabled
    @Inject
    I18NProvider i18nProvider;

    @PostConstruct
    private void init() {
        final Span label1 = new Span(getTranslation(I18N_TEST_KEY, ENGLISH));
        label1.setId(TRANSLATED_LABEL1_ID);
        add(label1);

        final Span label2 = new Span(
                i18nProvider.getTranslation(I18N_TEST_KEY, ENGLISH));
        label2.setId(TRANSLATED_LABEL2_ID);
        add(label2);

        final Span instanceCounter = new Span(Integer.toString(counter.get()));
        instanceCounter.setId(COUNTER_LABEL_ID);
        add(instanceCounter);
    }

}

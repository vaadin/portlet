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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.vaadin.cdi.annotation.VaadinServiceEnabled;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.i18n.I18NProvider;

import static java.util.Locale.ENGLISH;

public class I18NBeanView extends Div {

    public static class Portlet extends CdiVaadinPortlet<I18NBeanView> {
        public String getTag() {
            return "i18n-portlet";
        }
    }

    // TODO: Cannot use @VaadinServiceScoped, as this context is currently not
    // active in the BeanManager provided by Pluto. This needs to be fixed (and
    // tested), as the I18NProvider is now unnecessarily often instantiated.
    // @VaadinServiceScoped
    @VaadinServiceEnabled
    public static class I18N implements I18NProvider {
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

    public static final String TRANSLATED_LABEL_ID = "translatedLabel1";

    @PostConstruct
    private void init() {
        final Span label1 = new Span(getTranslation("test_key", ENGLISH));
        label1.setId(TRANSLATED_LABEL_ID);
        add(label1);
    }

}

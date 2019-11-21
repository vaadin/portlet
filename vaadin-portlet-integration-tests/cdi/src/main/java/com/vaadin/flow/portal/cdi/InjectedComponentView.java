package com.vaadin.flow.portal.cdi;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.util.Random;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

/**
 * Inject a Vaadin component and add it in @PostConstruct-annotated method.
 */
public class InjectedComponentView extends Div {

    public static class InjectedLabel extends Label {

        public InjectedLabel() {
            super(Integer.toString(new Random().nextInt(1000)));
        }
    }

    public static final String INJECTED_LABEL_CLASS = "injected-label";

    @Inject
    private InjectedLabel injectedLabel;

    @PostConstruct
    private void init() {
        injectedLabel.addClassName(INJECTED_LABEL_CLASS);

        add(injectedLabel);
    }
}

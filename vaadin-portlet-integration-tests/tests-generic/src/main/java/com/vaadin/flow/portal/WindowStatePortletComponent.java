package com.vaadin.flow.portal;

import javax.portlet.WindowState;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;

public interface WindowStatePortletComponent extends HasElement {


    default void maximize() {
        getElement().executeJs(
                "location.href = '" + ((MySecondPortlet) VaadinPortlet
                        .getCurrent()).maximizeAction + "'");
    }

    default void normalize() {
        getElement().executeJs(
                "location.href = '" + ((MySecondPortlet) VaadinPortlet
                        .getCurrent()).normalizeAction + "'");
    }

    default void minimize() {
        getElement().executeJs(
                "location.href = '" + ((MySecondPortlet) VaadinPortlet
                        .getCurrent()).minimizeAction + "'");
    }

     void renderNormal();

     void renderMaximized();

     void renderMinimized();
}

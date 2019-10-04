package com.vaadin.flow.portal;

import javax.portlet.WindowState;

import com.vaadin.flow.component.UI;

public interface WindowStateHandler<P extends WindowStatePortlet> {

    void windowStateChange(WindowState newState);

    default void maximize() {
        changeState(WindowState.MAXIMIZED);
    }

    default void normalize() {
        changeState(WindowState.NORMAL);
    }

    default void minimize() {
        changeState(WindowState.MINIMIZED);
    }

    default WindowState getWindowState() {
        return ((P) VaadinPortlet.getCurrent()).getWindowState();
    }

    default void changeState(WindowState state) {
        String stateChangeScript = String
                .format("location.href = '%s?state=%s'", ((P) VaadinPortlet.getCurrent()).getActionUrl(), state);

        UI.getCurrent().getPage().executeJs(stateChangeScript);
    }
}

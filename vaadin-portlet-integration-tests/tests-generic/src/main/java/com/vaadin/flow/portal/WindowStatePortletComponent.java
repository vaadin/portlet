package com.vaadin.flow.portal;

import javax.portlet.WindowState;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;

public abstract class WindowStatePortletComponent<T extends Component, P extends WindowStatePortlet>
        extends Composite<T> implements HasComponents {

    abstract void renderNormal();

    abstract void renderMaximized();

    abstract void renderMinimized();

    public void maximize() {
        changeState(WindowState.MAXIMIZED);
    }

    public void normalize() {
        changeState(WindowState.NORMAL);
    }

    public void minimize() {
        changeState(WindowState.MINIMIZED);
    }

    public WindowState getWindowState() {
        return ((P) VaadinPortlet.getCurrent()).getWindowState();
    }

    private void changeState(WindowState state) {
        String stateChangeScript = String
                .format("location.href = '%s?state=%s'", getActionUrl(), state);

        getElement().executeJs(stateChangeScript);
    }

    private String getActionUrl() {
        return ((P) VaadinPortlet.getCurrent()).getActionUrl();
    }

}

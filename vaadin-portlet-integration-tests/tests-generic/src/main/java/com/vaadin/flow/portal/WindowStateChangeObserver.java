package com.vaadin.flow.portal;

import javax.portlet.WindowState;

import com.vaadin.flow.component.UI;

public interface WindowStateChangeObserver {
    void beforeWindowStateChange(WindowState newState);
}

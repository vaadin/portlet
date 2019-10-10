package com.vaadin.flow.portal.handler;

import javax.portlet.WindowState;

public class WindowStateEvent {

    private final WindowState windowState;

    public WindowStateEvent(WindowState windowState) {
        this.windowState = windowState;
    }

    public WindowState getWindowState() {
        return windowState;
    }

    public boolean isMaximized() {
        return WindowState.MAXIMIZED.equals(windowState);
    }
}

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
package com.vaadin.flow.portal.handler;

import javax.portlet.WindowState;
import java.io.Serializable;

/**
 * An event of this class is fired when the {@link WindowState} of the portlet
 * is updated to a state different from its current state.
 */
public class WindowStateEvent implements Serializable {

    private final String windowState;
    private final String prevWindowState;
    private final boolean fromClient;

    /**
     * Creates a new event.
     *
     * @param newState
     *            the updated window state
     * @param oldState
     *            the old window state value
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public WindowStateEvent(WindowState newState, WindowState oldState,
            boolean fromClient) {
        this.windowState = newState.toString();
        this.prevWindowState = oldState.toString();
        this.fromClient = fromClient;
    }

    /**
     * The new {@link WindowState} of the portlet.
     *
     * @return the update portlet window state
     */
    public WindowState getWindowState() {
        return new WindowState(windowState);
    }

    /**
     * Whether the portlet window state is {@link WindowState#NORMAL}.
     *
     * @return true iff the window state is {@link WindowState#NORMAL}
     */
    public boolean isNormal() {
        return WindowState.NORMAL.toString().equals(windowState);
    }

    /**
     * Whether the portlet window state is {@link WindowState#MINIMIZED}.
     *
     * @return true iff the window state is {@link WindowState#MINIMIZED}
     */
    public boolean isMinimized() {
        return WindowState.MINIMIZED.toString().equals(windowState);
    }

    /**
     * Whether the portlet window state is {@link WindowState#MAXIMIZED}.
     *
     * @return true iff the window state is {@link WindowState#MAXIMIZED}
     */
    public boolean isMaximized() {
        return WindowState.MAXIMIZED.toString().equals(windowState);
    }

    /**
     * The {@link WindowState} of the portlet just before the update that
     * triggered this event.
     *
     * @return the previous window state.
     */
    public WindowState getPreviousWindowState() {
        return new WindowState(prevWindowState);
    }

    /**
     * Checks if this event originated from the client side.
     *
     * @return <code>true</code> if the event originated from the client side,
     *         <code>false</code> otherwise
     */
    public boolean isFromClient() {
        return fromClient;
    }
}

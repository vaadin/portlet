package com.vaadin.flow.portal.cdi;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.junit.Test;

import com.vaadin.testbench.TestBenchElement;

public class RequestBeansIT extends AbstractPlutoPortalTest {

    public RequestBeansIT() {
        super("requestbeans");
    }

    @Test
    public void sendRequest_preDefinedBeansReflectRequest() {
        // initially expect normal window state and view mode
        waitUntil(driver -> WindowState.NORMAL.toString()
                .equals($(TestBenchElement.class)
                        .id(RequestBeansView.WINDOW_STATE_LABEL_ID).getText())
                && PortletMode.VIEW.toString().equals($(TestBenchElement.class)
                        .id(RequestBeansView.PORTLET_MODE_LABEL_ID).getText()));

        // maximize and switch to edit mode
        setWindowStateInPortal(WindowState.MAXIMIZED);
        setPortletModeInPortal(PortletMode.EDIT);

        // then expect maximized window state and edit mode
        waitUntil(driver -> WindowState.MAXIMIZED.toString()
                .equals($(TestBenchElement.class)
                        .id(RequestBeansView.WINDOW_STATE_LABEL_ID).getText())
                && PortletMode.EDIT.toString().equals($(TestBenchElement.class)
                .id(RequestBeansView.PORTLET_MODE_LABEL_ID).getText()));
    }
}

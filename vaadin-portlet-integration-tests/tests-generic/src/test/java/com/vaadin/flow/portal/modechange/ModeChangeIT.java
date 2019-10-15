package com.vaadin.flow.portal.modechange;

import javax.portlet.PortletMode;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.portal.AbstractPlutoPortalTest;
import com.vaadin.testbench.TestBenchElement;

public class ModeChangeIT extends AbstractPlutoPortalTest {

    public ModeChangeIT() {
        super("modechange");
    }

    @Test
    public void testModeChange() throws Exception {
        Assert.assertEquals("", getModeLabelText());

        setPortletMode(PortletMode.EDIT);
        Assert.assertEquals(PortletMode.EDIT.toString(), getModeLabelText());

        setPortletMode(PortletMode.HELP);
        Assert.assertEquals(PortletMode.HELP.toString(), getModeLabelText());

        setPortletMode(PortletMode.VIEW);
        Assert.assertEquals(PortletMode.VIEW.toString(), getModeLabelText());
    }

    protected String getModeLabelText() {
        return $(TestBenchElement.class)
                .id(ModeChangeContent.MODE_LABEL_ID)
                .getText();
    }

}

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

@Ignore("https://github.com/vaadin/portlet/issues/214 https://issues.liferay.com/browse/LPS-150560")
@NotThreadSafe
public class LiferayHubRenderIT extends AbstractLiferayPortalTest {

    @Test
    public void changeModeAndState_modeAndStateAreKept() {
        TestBenchElement renderPortlet = getPortlet();
        TestBenchElement stateChange = renderPortlet.$("*")
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        TestBenchElement modeChange = renderPortlet.$("*")
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_MAXIMIZE,
                stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_EDIT,
                modeChange.getText());

        stateChange.click();

        waitForPageRefresh();

        renderPortlet = getPortlet();

        stateChange = renderPortlet.$("*")
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        modeChange = renderPortlet.$("*")
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        WebElement stateInfo = renderPortlet.$("*").id("state-info");
        Assert.assertEquals(WindowState.MAXIMIZED.toString(),
                stateInfo.getText());

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_NORMALIZE,
                stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_EDIT,
                modeChange.getText());
        Assert.assertEquals("VIEW", getPortletModeInPortal());
        Assert.assertFalse(isNormalWindowState());

        modeChange.click();

        waitForPageRefresh();

        renderPortlet = getPortlet();

        stateChange = renderPortlet.$(ButtonElement.class)
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        modeChange = renderPortlet.$(ButtonElement.class)
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        WebElement modeInfo = renderPortlet.$("*").id("mode-info");
        Assert.assertEquals(PortletMode.EDIT.toString(), modeInfo.getText());

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_NORMALIZE,
                stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_VIEW,
                modeChange.getText());
        Assert.assertEquals("EDIT", getPortletModeInPortal());
        Assert.assertFalse(isNormalWindowState());

        stateChange.click();

        waitForPageRefresh();

        renderPortlet = getPortlet();

        stateChange = renderPortlet.$("*")
                .id(LiferayPortlet30RenderView.WINDOW_STATE_CHANGE);
        modeChange = renderPortlet.$("*")
                .id(LiferayPortlet30RenderView.PORTLET_MODE_CHANGE);

        stateInfo = renderPortlet.$("*").id("state-info");
        Assert.assertEquals(WindowState.NORMAL.toString(), stateInfo.getText());

        Assert.assertEquals(LiferayPortlet30RenderView.STATE_MAXIMIZE,
                stateChange.getText());
        Assert.assertEquals(LiferayPortlet30RenderView.MODE_VIEW,
                modeChange.getText());
        Assert.assertEquals("EDIT", getWindowStateInPortal());
        Assert.assertTrue(isNormalWindowState());
    }

    private TestBenchElement getPortlet() {
        return getVaadinPortletRootElementByStaticPart(
                "renderportlet30_WAR_liferayportlet30");
    }

    private void waitForPageRefresh() {
        // Wait for a moment so the page refresh is done before continuing
        try {
            Thread.sleep(250); // NOSONAR
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isNormalWindowState() {
        return "NORMAL".equals(getWindowStateInPortal());
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/hubrender";
    }
}

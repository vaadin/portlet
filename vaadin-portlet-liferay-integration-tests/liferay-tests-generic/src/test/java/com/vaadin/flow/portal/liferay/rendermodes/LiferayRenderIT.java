/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.rendermodes;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;
import com.vaadin.testbench.TestBenchElement;

@Ignore("https://github.com/vaadin/portlet/issues/214 https://issues.liferay.com/browse/LPS-150560")
@NotThreadSafe
public class LiferayRenderIT extends AbstractLiferayPortalTest {

    @Test
    public void changeModeAndState_modeAndStateAreKept() {
        TestBenchElement stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.WINDOW_STATE_CHANGE);
        TestBenchElement modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(LiferayRenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(LiferayRenderView.MODE_EDIT, modeChange.getText());

        stateChange.click();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(LiferayRenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(LiferayRenderView.MODE_EDIT, modeChange.getText());
        Assert.assertEquals("VIEW", getWindowStateInPortal());
        Assert.assertFalse(isNormalWindowState());

        modeChange.click();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(LiferayRenderView.STATE_NORMALIZE, stateChange.getText());
        Assert.assertEquals(LiferayRenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowStateInPortal());
        Assert.assertFalse(isNormalWindowState());

        stateChange.click();

        stateChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.WINDOW_STATE_CHANGE);
        modeChange = getVaadinPortletRootElement().$("*")
                .id(LiferayRenderView.PORTLET_MODE_CHANGE);

        Assert.assertEquals(LiferayRenderView.STATE_MAXIMIZE, stateChange.getText());
        Assert.assertEquals(LiferayRenderView.MODE_VIEW, modeChange.getText());
        Assert.assertEquals("EDIT", getWindowStateInPortal());
        Assert.assertTrue(isNormalWindowState());
    }

    private boolean isNormalWindowState() {
        return findElements(By.id("portlets-left-column")).size() > 0;
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/renderer";
    }
}

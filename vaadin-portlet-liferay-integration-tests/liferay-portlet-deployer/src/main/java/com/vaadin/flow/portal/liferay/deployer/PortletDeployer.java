/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay.deployer;

import java.util.Collection;
import java.util.LinkedList;

import com.vaadin.flow.portal.liferay.AbstractPortletDeployer;
import com.vaadin.flow.portal.liferay.LayoutInfo;
import com.vaadin.flow.portal.liferay.PortletInfo;

/**
 * Adds Liferay layouts and test portlets for generic-tests and portlet 3.0.
 */
public class PortletDeployer extends AbstractPortletDeployer {

    @Override
    protected Collection<LayoutInfo> getLayouts() {
        // remember to update the xml config files in
        // liferay-tests-generic/src/main/webapp/WEB-INF/
        // when adding a new test portlet

        // Generic tests

        Collection<LayoutInfo> layouts = new LinkedList<>();

        LayoutInfo basicLayout = new LayoutInfo("BasicPortlet", "/test/basic");
        PortletInfo basic = new PortletInfo();
        basic.setId("basic_WAR_liferaytestsgeneric");
        basic.setPortletName("BasicPortlet");
        basic.setPortletFriendlyUrl("/test/basic");
        basicLayout.addPortlet(basic);
        layouts.add(basicLayout);

        LayoutInfo errorLayout = new LayoutInfo("ErrorHandling", "/test/errorhandling");
        PortletInfo errorHandling = new PortletInfo();
        errorHandling.setId("errorhandling_WAR_liferaytestsgeneric");
        errorHandling.setPortletName("ErrorHandling");
        errorHandling.setPortletFriendlyUrl("/test/errorhandling");
        errorLayout.addPortlet(errorHandling);
        layouts.add(errorLayout);

        LayoutInfo eventLayout = new LayoutInfo("EventHandler",
                "/test/eventhandler");
        PortletInfo eventHandler = new PortletInfo();
        eventHandler.setId("eventhandler_WAR_liferaytestsgeneric");
        eventHandler.setPortletName("EventHandler");
        eventHandler.setPortletFriendlyUrl("/test/eventhandler");
        eventLayout.addPortlet(eventHandler);
        layouts.add(eventLayout);

        LayoutInfo stateLayout = new LayoutInfo("MinimizedStateRenderer",
                "/test/minimized-state-render");
        PortletInfo minimizedStateRenderer = new PortletInfo();
        minimizedStateRenderer.setId("minimizedstaterender_WAR_liferaytestsgeneric");
        minimizedStateRenderer.setPortletName("MinimizedStateRenderer");
        minimizedStateRenderer.setPortletFriendlyUrl("/test/minimized-state-render");
        stateLayout.addPortlet(minimizedStateRenderer);
        layouts.add(stateLayout);

        LayoutInfo renderLayout = new LayoutInfo("Renderer",
                "/test/renderer");
        PortletInfo renderer = new PortletInfo();
        renderer.setId("render_WAR_liferaytestsgeneric");
        renderer.setPortletName("Renderer");
        renderer.setPortletFriendlyUrl("/test/renderer");
        renderLayout.addPortlet(renderer);
        layouts.add(renderLayout);

        LayoutInfo streamLayout = new LayoutInfo("StreamResource",
                "/test/stream-resource");
        PortletInfo streamResource = new PortletInfo();
        streamResource.setId("streamresource_WAR_liferaytestsgeneric");
        streamResource.setPortletName("StreamResource");
        streamResource.setPortletFriendlyUrl("/test/stream-resource");
        streamLayout.addPortlet(streamResource);
        layouts.add(streamLayout);

        LayoutInfo uploadLayout = new LayoutInfo("Upload", "/test/upload");
        PortletInfo upload = new PortletInfo();
        upload.setId("upload_WAR_liferaytestsgeneric");
        upload.setPortletName("Upload");
        upload.setPortletFriendlyUrl("/test/upload");
        uploadLayout.addPortlet(upload);
        layouts.add(uploadLayout);


        // Portlet 3.0:

        String ipcEventFriendlyUrl = "/test/ipcevent";
        LayoutInfo eventSourceTargetLayout = new LayoutInfo("Events",
                ipcEventFriendlyUrl);
        PortletInfo eventSource = new PortletInfo();
        eventSource.setId("eventsource_WAR_liferayportlet30");
        eventSource.setPortletName("Event Source");
        eventSource.setPortletFriendlyUrl(ipcEventFriendlyUrl);
        eventSourceTargetLayout.addPortlet(eventSource);

        PortletInfo eventTarget = new PortletInfo();
        eventTarget.setId("eventtarget_WAR_liferayportlet30");
        eventTarget.setPortletName("Event Target");
        eventTarget.setPortletFriendlyUrl(ipcEventFriendlyUrl);
        eventSourceTargetLayout.addPortlet(eventTarget);

        PortletInfo otherEventTarget = new PortletInfo();
        otherEventTarget.setId("othereventtarget_WAR_liferayportlet30");
        otherEventTarget.setPortletName("Other Event Target");
        otherEventTarget.setPortletFriendlyUrl(ipcEventFriendlyUrl);
        eventSourceTargetLayout.addPortlet(otherEventTarget);

        layouts.add(eventSourceTargetLayout);

        String ipcEventNotVaadinFriedlyUrl = "/test/ipceventnotvaadin";
        LayoutInfo notVaadinEventLayout = new LayoutInfo("Not Vaadin Events",
                ipcEventNotVaadinFriedlyUrl);
        PortletInfo vaadinIpcPortlet = new PortletInfo();
        vaadinIpcPortlet.setId("vaadinipcportlet_WAR_liferayportlet30");
        vaadinIpcPortlet.setPortletName("Vaadin IPC Portlet");
        vaadinIpcPortlet.setPortletFriendlyUrl(ipcEventNotVaadinFriedlyUrl);
        notVaadinEventLayout.addPortlet(vaadinIpcPortlet);

        PortletInfo plainIpcPortlet = new PortletInfo();
        plainIpcPortlet.setId("plainipcportlet_WAR_liferayportlet30");
        plainIpcPortlet.setPortletName("Event Target");
        plainIpcPortlet.setPortletFriendlyUrl(ipcEventNotVaadinFriedlyUrl);
        notVaadinEventLayout.addPortlet(plainIpcPortlet);

        layouts.add(notVaadinEventLayout);

        LayoutInfo render30Layout = new LayoutInfo("Render 30",
                "/test/hubrender");
        PortletInfo render30Portlet = new PortletInfo();
        render30Portlet.setId("renderportlet30_WAR_liferayportlet30");
        render30Portlet.setPortletName("Render 30 Portlet");
        render30Portlet.setPortletFriendlyUrl("/test/hubrender");
        render30Layout.addPortlet(render30Portlet);

        layouts.add(render30Layout);

        return layouts;
    }

}





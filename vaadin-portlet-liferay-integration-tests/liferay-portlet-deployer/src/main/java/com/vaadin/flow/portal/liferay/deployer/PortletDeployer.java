package com.vaadin.flow.portal.liferay.deployer;

import java.util.Collection;
import java.util.LinkedList;

import com.vaadin.flow.portal.liferay.AbstractPortletDeployer;
import com.vaadin.flow.portal.liferay.LayoutInfo;
import com.vaadin.flow.portal.liferay.PortletInfo;

public class PortletDeployer extends AbstractPortletDeployer {

    @Override
    protected Collection<LayoutInfo> getLayouts() {
        // remember to update the xml config files in
        // liferay-tests-generic/src/main/webapp/WEB-INF/
        // when adding a new test portlet
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
        layouts.add(eventLayout);

        LayoutInfo stateLayout = new LayoutInfo("MinimizedStateRenderer",
                "/test/minimized-state-render");
        PortletInfo minimizedStateRenderer = new PortletInfo();
        minimizedStateRenderer.setId("minimizedstaterender_WAR_liferaytestsgeneric");
        minimizedStateRenderer.setPortletName("MinimizedStateRenderer");
        minimizedStateRenderer.setPortletFriendlyUrl("/test/minimized-state-render");
        layouts.add(stateLayout);

        LayoutInfo renderLayout = new LayoutInfo("Renderer",
                "/test/renderer");
        PortletInfo renderer = new PortletInfo();
        renderer.setId("render_WAR_liferaytestsgeneric");
        renderer.setPortletName("Renderer");
        renderer.setPortletFriendlyUrl("/test/renderer");
        layouts.add(renderLayout);

        LayoutInfo streamLayout = new LayoutInfo("StreamResource",
                "/test/stream-resource");
        PortletInfo streamResource = new PortletInfo();
        streamResource.setId("streamresource_WAR_liferaytestsgeneric");
        streamResource.setPortletName("StreamResource");
        streamResource.setPortletFriendlyUrl("/test/stream-resource");
        layouts.add(streamLayout);

        LayoutInfo uploadLayout = new LayoutInfo("Upload", "/test/upload");
        PortletInfo upload = new PortletInfo();
        upload.setId("upload_WAR_liferaytestsgeneric");
        upload.setPortletName("Upload");
        upload.setPortletFriendlyUrl("/test/upload");
        layouts.add(uploadLayout);

        return layouts;
    }

}





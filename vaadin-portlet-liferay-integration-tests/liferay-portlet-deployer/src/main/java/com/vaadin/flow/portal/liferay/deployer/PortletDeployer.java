package com.vaadin.flow.portal.liferay.deployer;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.sites.kernel.util.SitesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortletDeployer extends GenericPortlet {

    private static final Map<String, PortletInfo> portletInfo =
            new HashMap<>();

    static {
        // remember to update the xml config files in
        // liferay-tests-generic/src/main/webapp/WEB-INF/
        // when adding a new test portlet

        PortletInfo basic = new PortletInfo();
        basic.setPortletName("BasicPortlet");
        basic.setPortletFriendlyUrl("/test/basic");

        PortletInfo errorHandling = new PortletInfo();
        errorHandling.setPortletName("ErrorHandling");
        errorHandling.setPortletFriendlyUrl("/test/errorhandling");

        PortletInfo eventHandler = new PortletInfo();
        eventHandler.setPortletName("EventHandler");
        eventHandler.setPortletFriendlyUrl("/test/eventhandler");

        PortletInfo minimizedStateRenderer = new PortletInfo();
        minimizedStateRenderer.setPortletName("MinimizedStateRenderer");
        minimizedStateRenderer.setPortletFriendlyUrl("/test/minimized-state-render");

        PortletInfo renderer = new PortletInfo();
        renderer.setPortletName("Renderer");
        renderer.setPortletFriendlyUrl("/test/renderer");

        PortletInfo streamResource = new PortletInfo();
        streamResource.setPortletName("StreamResource");
        streamResource.setPortletFriendlyUrl("/test/stream-resource");

        PortletInfo upload = new PortletInfo();
        upload.setPortletName("Upload");
        upload.setPortletFriendlyUrl("/test/upload");

        portletInfo.put("basic_WAR_liferaytestsgeneric", basic);
        portletInfo.put("upload_WAR_liferaytestsgeneric", upload);
        portletInfo.put("eventhandler_WAR_liferaytestsgeneric", eventHandler);
        portletInfo.put("render_WAR_liferaytestsgeneric", renderer);
        portletInfo.put("minimized-state-render_WAR_liferaytestsgeneric", minimizedStateRenderer);
        portletInfo.put("errorhandling_WAR_liferaytestsgeneric", errorHandling);
        portletInfo.put("streamresource_WAR_liferaytestsgeneric", streamResource);
    }

    private long userId;

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        AtomicInteger order = new AtomicInteger(1);
        portletInfo.forEach((portletId, value) -> {
            String portletName = value.getPortletName();
            String portletFriendlyUrl = value.getPortletFriendlyUrl();
            Layout layout = addMyLayout(portletName, portletFriendlyUrl);
            getLogger().info("Deployed layout for {}", portletName);
            addMyPortlet(layout, portletId, order.get());
            getLogger().info("Deployed portlet = {}", portletName);
            order.incrementAndGet();
        });
    }

    private void addMyPortlet(Layout layout, String portletId, int order) {
        LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();
        String newPortletId = layoutTypePortlet.addPortletId(userId,
                portletId, "column-1", order, false);
        setPortletPreferences(layout, portletId, newPortletId);
        try {
            LayoutLocalServiceUtil.updateLayout(layout.getGroupId(), layout.getPrivateLayout(), layout.getLayoutId(),
                    layout.getTypeSettings());
        } catch (PortalException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPortletPreferences(Layout layout, String portletType, String portletIdNew) {
        try {
            PortletPreferences prefs = PortletPreferencesLocalServiceUtil
                    .getPreferences(layout.getCompanyId(),
                            PortletKeys.PREFS_OWNER_ID_DEFAULT,
                            PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
                            layout.getPlid(), portletIdNew);
            portletInfo.get(portletType).getPortletPreferences().forEach((key, value) -> {
                try {
                    prefs.setValue(key, value);
                } catch (ReadOnlyException e) {
                    throw new RuntimeException(e);
                }
            });
            prefs.store();
        } catch (IOException | ValidatorException e) {
            throw new RuntimeException(e);
        }
    }

    private Layout addMyLayout(String portletName, String friendlyUrl) {
        Layout layout = null;
        try {
            User user = getMyDefaultUserId();
            userId = Objects.requireNonNull(user).getUserId();
            long groupId = getGroupFromDefaultCompanyId();

//            if (getLayoutByFriendlyUrl(groupId, friendlyURL) != null) {
//                friendlyURL = friendlyURL + new Random().nextInt();
//            }
            removeOldLayoutIfPresent(groupId, friendlyUrl);

            ServiceContext serviceContext = new ServiceContext();

            layout = LayoutLocalServiceUtil.addLayout(userId, groupId, false,
                    0, portletName, portletName, portletName, LayoutConstants.TYPE_PORTLET, false,
                    friendlyUrl, serviceContext);
        } catch (PortalException e) {
            throw new RuntimeException(e);
        }

        return layout;
    }

    public boolean removeOldLayoutIfPresent(long pGroupId, String friendlyURL) {
        try {
            Layout layoutByFriendlyURL = getLayoutByFriendlyUrl(pGroupId, friendlyURL);
            if (layoutByFriendlyURL != null){
                if (SitesUtil.isLayoutDeleteable(layoutByFriendlyURL)){
                    LayoutLocalServiceUtil.deleteLayout(layoutByFriendlyURL);
                }

            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private User getMyDefaultUserId() {
        try {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(User.class).add(PropertyFactoryUtil.forName("defaultUser").eq(Boolean.TRUE));
            List<User> users = UserLocalServiceUtil.dynamicQuery(query);
            return users.get(0);
        } catch (SystemException e) {
            return null;
        }
    }

    private long getCompanyId() {
        try {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Company.class).add(PropertyFactoryUtil.forName("active").eq(Boolean.TRUE));
            List<Company> users = CompanyLocalServiceUtil.dynamicQuery(query);
            Company company = users.get(0);
            return company.getCompanyId();
        } catch (SystemException e) {
            return 0;
        }
    }

    private long getGroupFromDefaultCompanyId() {
        try {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Group.class);
            query.add(PropertyFactoryUtil.forName("site").eq(Boolean.TRUE));
            query.add(PropertyFactoryUtil.forName("type").eq(1));

            List<Group> groups = GroupLocalServiceUtil.dynamicQuery(query);
            Group g = groups.get(0);
            return g.getGroupId();
        } catch (SystemException e) {
            return 0;
        }
    }

    public Layout getLayoutByFriendlyUrl(long groupId, String friendlyURL) {
        return LayoutLocalServiceUtil.fetchLayoutByFriendlyURL(groupId, false, friendlyURL);
    }

    private static final class PortletInfo implements Serializable {
        private static final String PORTLET_NAME = "portletName";
        private static final String PORTLET_FRIENDLY_URL = "portletFriendlyUrl";
        private static final String PORTLET_PREFERENCES = "portletPreferences";

        private final Map<String, Object> portletInfo;

        public PortletInfo() {
            portletInfo = new HashMap<>(3);
            portletInfo.put(PORTLET_PREFERENCES, new HashMap<>());
        }

        public void setPortletName(String name) {
            portletInfo.put(PORTLET_NAME, name);
        }

        public String getPortletName() {
            return (String) portletInfo.get(PORTLET_NAME);
        }

        public void setPortletFriendlyUrl(String friendlyUrl) {
            portletInfo.put(PORTLET_FRIENDLY_URL, friendlyUrl);
        }

        public String getPortletFriendlyUrl() {
            return (String) portletInfo.get(PORTLET_FRIENDLY_URL);
        }

        public void addPortletPreference(String key, String value) {
            getPortletPreferences().put(key, value);
        }

        public String getPortletPreference(String key) {
            return getPortletPreferences().get(key);
        }

        public Map<String, String> getPortletPreferences() {
            return (Map<String, String>) portletInfo.get(PORTLET_PREFERENCES);
        }

    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
}





package com.vaadin.flow.portal.liferay.deployer;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import com.liferay.portlet.Preference;
import com.liferay.sites.kernel.util.SitesUtil;

public class PortletDeployer extends GenericPortlet {

    private static final Map<String, Collection<Preference>> portletInfo = new HashMap<>(2);

    static {
        portletInfo.put("basic_WAR_liferaytestsgeneric",
                Collections.emptyList());
    }

    private long userId;

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        Layout layout = addMyLayout();
        AtomicInteger order = new AtomicInteger(1);
        portletInfo.keySet().forEach(name -> {
            addMyPortlet(layout, name, order.get());
            order.incrementAndGet();
        });
    }

    private void addMyPortlet(Layout layout, String portletType, int order) {
        LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();
        String newPortletId = layoutTypePortlet.addPortletId(userId,
                portletType, "column-1", order, false);
        setPortletPreferences(layout, portletType, newPortletId);
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
            portletInfo.get(portletType).forEach(preference -> {
                try {
                    prefs.setValue(preference.getName(), preference.getValues()[0]);
                } catch (ReadOnlyException e) {
                    throw new RuntimeException(e);
                }
            });
            prefs.store();
        } catch (IOException | ValidatorException e) {
            throw new RuntimeException(e);
        }
    }

    private Layout addMyLayout() {
        Layout layout = null;
        try {
            User user = getMyDefaultUserId();
            userId = Objects.requireNonNull(user).getUserId();

            long myCompanyId = getMyCompanyId();
            long groupId = getGroupFromDefaultCompanyId(myCompanyId);


            String name = "BasicPortlet";
            String friendlyURL = "/test/basic";
//            if (getLayoutByFriendlyUrl(groupId, friendlyURL) != null) {
//                friendlyURL = friendlyURL + new Random().nextInt();
//            }
            removeOldLayoutIfPresent(groupId, friendlyURL);

            ServiceContext serviceContext = new ServiceContext();

            layout = LayoutLocalServiceUtil.addLayout(userId, groupId, false, 0, name,
                    name, name, LayoutConstants.TYPE_PORTLET, false, friendlyURL, serviceContext);
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

//    private long getMyDefaultGroupId() {
//        String webId = PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID);
//        try {
//            Company company = CompanyLocalServiceUtil.getCompanyByWebId(webId);
//            return company.getGroup().getGroupId();
//        } catch (PortalException e) {
//            return 0;
//        }
//    }

    private long getMyCompanyId() {
        try {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Company.class).add(PropertyFactoryUtil.forName("active").eq(Boolean.TRUE));
            List<Company> users = CompanyLocalServiceUtil.dynamicQuery(query);
            Company company = users.get(0);
            return company.getCompanyId();
        } catch (SystemException e) {
            return 0;
        }
    }

    private long getGroupFromDefaultCompanyId(long companyId) {
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
}





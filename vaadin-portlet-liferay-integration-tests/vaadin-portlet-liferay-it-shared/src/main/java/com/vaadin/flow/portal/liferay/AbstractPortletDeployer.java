/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.sites.kernel.util.SitesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Generic Portlet which creates Liferay layouts and adds test portlets to the
 * corresponding layouts once deployed to Liferay container.
 * Uses Liferay API and simplifies preparation of portlets before running
 * integration tests.
 * Layouts to be added should be defined in child class.
 */
public abstract class AbstractPortletDeployer extends GenericPortlet {

    private long userId;

    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        AtomicInteger order = new AtomicInteger(1);
        Collection<LayoutInfo> layouts = Objects.requireNonNull(getLayouts());
        layouts.forEach(layoutInfo -> {
            Layout addedLayout = addLayout(layoutInfo);
            getLogger().info("Deployed layout for {}", layoutInfo.getName());

            Collection<PortletInfo> portlets = Objects
                    .requireNonNull(layoutInfo.getPortletInfos());
            portlets.forEach(portlet -> {
                addPortlet(addedLayout, portlet, order.get());
                getLogger().info("Deployed portlet = {}",
                        portlet.getPortletName());
                order.incrementAndGet();
            });
        });
    }

    private void addPortlet(Layout layout, PortletInfo portlet, int order) {
        LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout
                .getLayoutType();
        String newPortletId = layoutTypePortlet.addPortletId(userId,
                portlet.getId(), "column-1", order, false);
        setPortletPreferences(layout, portlet, newPortletId);
        try {
            LayoutLocalServiceUtil.updateLayout(layout.getGroupId(),
                    layout.getPrivateLayout(), layout.getLayoutId(),
                    layout.getTypeSettings());
        } catch (PortalException e) {
            throw new PortletDeployerException(e);
        }
    }

    private void setPortletPreferences(Layout layout, PortletInfo portlet,
            String portletIdNew) {
        try {
            PortletPreferences prefs = PortletPreferencesLocalServiceUtil
                    .getPreferences(layout.getCompanyId(),
                            PortletKeys.PREFS_OWNER_ID_DEFAULT,
                            PortletKeys.PREFS_OWNER_TYPE_LAYOUT,
                            layout.getPlid(), portletIdNew);
            portlet.getPortletPreferences().forEach((key, value) -> {
                try {
                    prefs.setValue(key, value);
                } catch (ReadOnlyException e) {
                    throw new PortletDeployerException(e);
                }
            });
            prefs.store();
        } catch (IOException | ValidatorException e) {
            throw new PortletDeployerException(e);
        }
    }

    private Layout addLayout(LayoutInfo layoutInfo) {
        Layout layout;
        try {
            User user = getDefaultUserId();
            userId = Objects.requireNonNull(user).getUserId();
            long groupId = getGroupFromDefaultCompanyId();

            removeOldLayoutIfPresent(groupId, layoutInfo.getFriendlyUrl());

            ServiceContext serviceContext = new ServiceContext();

            layout = LayoutLocalServiceUtil.addLayout(userId, groupId, false, 0,
                    layoutInfo.getName(), layoutInfo.getName(),
                    layoutInfo.getName(), LayoutConstants.TYPE_PORTLET, false,
                    layoutInfo.getFriendlyUrl(), serviceContext);
        } catch (PortalException e) {
            throw new PortletDeployerException(e);
        }

        return layout;
    }

    public boolean removeOldLayoutIfPresent(long pGroupId, String friendlyURL) {
        try {
            Layout layoutByFriendlyURL = getLayoutByFriendlyUrl(pGroupId,
                    friendlyURL);
            if (layoutByFriendlyURL != null
                    && SitesUtil.isLayoutDeleteable(layoutByFriendlyURL)) {
                LayoutLocalServiceUtil.deleteLayout(layoutByFriendlyURL);
            }
        } catch (Exception e) {
            getLogger().warn("Failed to remove olf layout for {}", friendlyURL,
                    e);
            return false;
        }
        return true;
    }

    private User getDefaultUserId() {
        try {
            DynamicQuery query = DynamicQueryFactoryUtil
                    .forClass(User.class, getClass().getClassLoader())
                    .add(PropertyFactoryUtil.forName("defaultUser")
                            .eq(Boolean.TRUE));
            List<User> users = UserLocalServiceUtil.dynamicQuery(query);
            return users.get(0);
        } catch (SystemException e) {
            getLogger().warn("Failed to retrieve default user", e);
            return null;
        }
    }

    private long getGroupFromDefaultCompanyId() {
        try {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Group.class,
                    getClass().getClassLoader());
            query.add(PropertyFactoryUtil.forName("site").eq(Boolean.TRUE));
            query.add(PropertyFactoryUtil.forName("type").eq(1));

            List<Group> groups = GroupLocalServiceUtil.dynamicQuery(query);
            Group g = groups.get(0);
            return g.getGroupId();
        } catch (SystemException e) {
            getLogger().warn(
                    "Failed to retrieve default company id, using 0 as fallback",
                    e);
            return 0;
        }
    }

    public Layout getLayoutByFriendlyUrl(long groupId, String friendlyURL) {
        return LayoutLocalServiceUtil.fetchLayoutByFriendlyURL(groupId, false,
                friendlyURL);
    }

    /**
     * Gets the collection of Liferay layouts (including portlets) to be added
     * onto the page.
     * @return collection of layouts to be added.
     */
    protected abstract Collection<LayoutInfo> getLayouts();

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    private static class PortletDeployerException extends RuntimeException {

        public PortletDeployerException() {
        }

        public PortletDeployerException(Throwable cause) {
            super(cause);
        }
    }
}

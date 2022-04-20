/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.PropsUtil;

/**
 * Portlet request for Liferay.
 */
public class VaadinLiferayRequest extends VaadinHttpAndPortletRequest {
    /**
     * The PortalUtil class to use. Set to either
     * {@link #LIFERAY_6_PORTAL_UTIL} or {@link #LIFERAY_7_PORTAL_UTIL} the
     * first time it is needed.
     */
    private static String portalUtilClass = null;
    private static final String LIFERAY_6_PORTAL_UTIL = "com.liferay.portal.util.PortalUtil";
    private static final String LIFERAY_7_PORTAL_UTIL = "com.liferay.portal.kernel.util.PortalUtil";
    private static final String LIFERAY_7_PORTLET_UTIL = "com.liferay.portlet.LiferayPortletUtil";
    private static final String PORTLET_REQUEST_CLASS = "javax.portlet.PortletRequest";

    public VaadinLiferayRequest(PortletRequest request,
            VaadinPortletService vaadinService) {
        super(request, vaadinService);
    }

    @Override
    public String getPortalProperty(String name) {
        return PropsUtil.get(name);
    }

    /**
     * Simplified version of what Liferay PortalClassInvoker did. This is
     * used because the API of PortalClassInvoker has changed in Liferay
     * 6.2.
     *
     * This simply uses reflection with Liferay class loader. Parameters are
     * Strings to avoid static dependencies and to load all classes with
     * Liferay's own class loader. Only static utility methods are
     * supported.
     *
     * This method is for internal use only and may change in future
     * versions.
     *
     * @param className
     *         name of the Liferay class to call
     * @param methodName
     *         name of the method to call
     * @param parameterClassName
     *         name of the parameter class of the method
     * @return return value of the invoked method
     * @throws Exception
     */
    private Object invokeStaticLiferayMethod(String className,
            String methodName, Object argument, String parameterClassName)
            throws Exception {
        Thread currentThread = Thread.currentThread();

        ClassLoader contextClassLoader = currentThread.getContextClassLoader();

        try {
            // this should be available across all Liferay versions with no
            // problematic static dependencies
            ClassLoader portalClassLoader = PortalClassLoaderUtil
                    .getClassLoader();
            // this is in case the class loading triggers code that
            // explicitly
            // uses current thread class loader
            currentThread.setContextClassLoader(portalClassLoader);

            Class<?> targetClass = portalClassLoader.loadClass(className);
            Class<?> parameterClass = portalClassLoader
                    .loadClass(parameterClassName);
            Method method = targetClass.getMethod(methodName, parameterClass);

            return method.invoke(null, argument);
        } catch (InvocationTargetException ite) {
            throw (Exception) ite.getCause();
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    protected HttpServletRequest getServletRequest(PortletRequest request) {
        LiferayPortletRequest liferayPortletRequest = null;
        if (portalUtilClass == null) {
            try {
                try {
                    liferayPortletRequest = (LiferayPortletRequest) invokeStaticLiferayMethod(
                            LIFERAY_7_PORTLET_UTIL, "getLiferayPortletRequest",
                            request, PORTLET_REQUEST_CLASS);
                    portalUtilClass = LIFERAY_7_PORTLET_UTIL;
                    // Liferay 7.1+
                } catch (Exception e) {
                    // Liferay 7.0
                    invokeStaticLiferayMethod(LIFERAY_7_PORTAL_UTIL,
                            "getHttpServletRequest", request,
                            PORTLET_REQUEST_CLASS);
                    portalUtilClass = LIFERAY_7_PORTAL_UTIL;
                }
            } catch (Exception e) {
                // Liferay 6 or older
                portalUtilClass = LIFERAY_6_PORTAL_UTIL;
            }
        }
        try {
            HttpServletRequest httpRequest;
            if (liferayPortletRequest != null) {
                return liferayPortletRequest.getOriginalHttpServletRequest();
            } else if (portalUtilClass.equals(LIFERAY_7_PORTLET_UTIL)) {
                liferayPortletRequest = (LiferayPortletRequest) invokeStaticLiferayMethod(
                        portalUtilClass, "getLiferayPortletRequest", request,
                        PORTLET_REQUEST_CLASS);
                return liferayPortletRequest.getOriginalHttpServletRequest();
            } else {
                // httpRequest = PortalUtil.getHttpServletRequest(request);
                httpRequest = (HttpServletRequest) invokeStaticLiferayMethod(
                        portalUtilClass, "getHttpServletRequest", request,
                        PORTLET_REQUEST_CLASS);

                // httpRequest =
                // PortalUtil.getOriginalServletRequest(httpRequest);
                httpRequest = (HttpServletRequest) invokeStaticLiferayMethod(
                        portalUtilClass, "getOriginalServletRequest",
                        httpRequest, "javax.servlet.http.HttpServletRequest");
                return httpRequest;
            }

        } catch (Exception e) {
            throw new IllegalStateException("Liferay request not detected", e);
        }
    }
}

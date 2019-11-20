/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.vaadin.flow.server.WrappedSession;

/**
 * Wrapper for {@link PortletSession}.
 *
 * @author Vaadin Ltd
 * @see WrappedSession
 * @since 7.0.0
 */
class WrappedPortletSession implements WrappedSession {

    private final transient PortletSession session;

    /**
     * Creates a new wrapped portlet session.
     *
     * @param session
     *         the portlet session to wrap.
     */
    WrappedPortletSession(PortletSession session) {
        this.session = session;
    }

    @Override
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    @Override
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name in the given
     * scope.
     *
     * @param name
     *         a string specifying the name of the object
     * @param scope
     *         session scope of this attribute
     * @return the object with the specified name
     * @throws java.lang.IllegalStateException
     *         if this method is called on an invalidated session, or the
     *         scope is unknown to the container.
     * @throws java.lang.IllegalArgumentException
     *         if name is <code>null</code>.
     * @see PortletSession#getAttribute(String, int)
     * @see PortletSession#PORTLET_SCOPE
     * @see PortletSession#APPLICATION_SCOPE
     * @since 7.6
     */
    public Object getAttribute(String name, int scope) {
        return session.getAttribute(name, scope);
    }

    @Override
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    /**
     * Binds an object to this session in the given scope, using the name
     * specified. If an object of the same name in this scope is already bound
     * to the session, that object is replaced.
     *
     * <p>
     * If the value is <code>null</code>, this has the same effect as calling
     * <code>removeAttribute()</code>.
     *
     * @param name
     *         the name to which the object is bound; this cannot be
     *         <code>null</code>.
     * @param value
     *         the object to be bound
     * @param scope
     *         session scope of this attribute
     * @throws java.lang.IllegalStateException
     *         if this method is called on a session which has been
     *         invalidated
     * @throws java.lang.IllegalArgumentException
     *         if name is <code>null</code> or scope is unknown to the
     *         container.
     * @see PortletSession#setAttribute(String, Object, int)
     * @see PortletSession#PORTLET_SCOPE
     * @see PortletSession#APPLICATION_SCOPE
     * @since 7.6
     */
    public void setAttribute(String name, Object value, int scope) {
        session.setAttribute(name, value, scope);
    }

    /**
     * Gets the wrapped {@link PortletSession}.
     *
     * @return the wrapped portlet session
     */
    public PortletSession getPortletSession() {
        return session;
    }

    @Override
    public Set<String> getAttributeNames() {
        return enumerationToSet(session.getAttributeNames());
    }

    /**
     * Gets the current set of attribute names bound to this session in the
     * given scope.
     *
     * @param scope
     *         session scope of the attribute names
     * @return an unmodifiable set of the current attribute names in the given
     * scope
     * @see PortletSession#getAttributeNames()
     * @since 7.6
     */
    public Set<String> getAttributeNames(int scope) {
        return enumerationToSet(session.getAttributeNames(scope));
    }

    /**
     * Convert {@link Enumeration} to a unmodifiable set.
     *
     * @param values
     *         enumeration to convert
     * @param <T>
     *         type of enumeration values
     * @return unmodifiable set of enumeration values
     */
    static <T> Set<T> enumerationToSet(Enumeration<T> values) {
        return Collections
                .unmodifiableSet(new HashSet<T>(Collections.list(values)));
    }

    @Override
    public void invalidate() {
        session.invalidate();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public long getCreationTime() {
        return session.getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    @Override
    public boolean isNew() {
        return session.isNew();
    }

    @Override
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    /**
     * Removes the object bound with the specified name and the given scope from
     * this session. If the session does not have an object bound with the
     * specified name, this method does nothing.
     *
     * @param name
     *         the name of the object to be removed from this session
     * @param scope
     *         session scope of this attribute
     * @throws java.lang.IllegalStateException
     *         if this method is called on a session which has been
     *         invalidated
     * @throws java.lang.IllegalArgumentException
     *         if name is <code>null</code>.
     * @see PortletSession#removeAttribute(String, int)
     * @see PortletSession#PORTLET_SCOPE
     * @see PortletSession#APPLICATION_SCOPE
     * @since 7.6
     */
    public void removeAttribute(String name, int scope) {
        session.removeAttribute(name, scope);
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        session.setMaxInactiveInterval(interval);
    }
}

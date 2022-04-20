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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;

public class VaadinPortletServiceTest {

    @Test
    @SuppressWarnings({ "rawtypes", "serial" })
    public void instantiate_defaultErrorHandlerIsAddedToNewSession()
            throws SessionExpiredException {
        VaadinPortlet portlet = Mockito.mock(VaadinPortlet.class);
        DeploymentConfiguration configuration = Mockito
                .mock(DeploymentConfiguration.class);
        ReentrantLock lock = new ReentrantLock();
        VaadinPortletService service = new VaadinPortletService(portlet,
                configuration) {

            @Override
            protected Lock getSessionLock(WrappedSession wrappedSession) {
                return lock;
            }

            @Override
            protected boolean requestCanCreateSession(VaadinRequest request) {
                return true;
            }

            @Override
            public Instantiator getInstantiator() {
                return new DefaultInstantiator(this);
            }
        };

        VaadinPortletRequest request = Mockito.mock(VaadinPortletRequest.class);
        WrappedPortletSession wrappedSession = Mockito
                .mock(WrappedPortletSession.class);
        Mockito.when(request.getWrappedSession(Mockito.anyBoolean()))
                .thenReturn(wrappedSession);
        Mockito.when(request.getWrappedSession()).thenReturn(wrappedSession);

        VaadinSession session = service.findVaadinSession(request);
        session.lock();
        try {
            Assert.assertNotNull(session.getErrorHandler());
            Assert.assertTrue(session
                    .getErrorHandler() instanceof DefaultPortletErrorHandler);
        } finally {
            session.unlock();
        }
    }
}

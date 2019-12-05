/*
 * Copyright 2000-2018 Vaadin Ltd.
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

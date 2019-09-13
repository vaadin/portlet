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
package com.vaadin.flow.portal.addressbook.form;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import java.io.IOException;

import com.vaadin.flow.portal.VaadinPortlet;

/**
 * @author Vaadin Ltd
 *
 */
public class FormPortlet extends VaadinPortlet {

    public static final String TAG = "form-portlet";

    @Override
    public String getMainComponentTag() {
        return TAG;
    }

    @Override
    protected void handleRequest(PortletRequest request,
            PortletResponse response) throws PortletException, IOException {
        String namespace = response.getNamespace();
        System.out.println("Xxxxxxxxxxx " + namespace);
        super.handleRequest(request, response);
    }
}

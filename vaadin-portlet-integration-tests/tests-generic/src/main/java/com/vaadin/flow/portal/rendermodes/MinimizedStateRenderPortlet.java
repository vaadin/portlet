/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.rendermodes;

import javax.portlet.Portlet;

import com.vaadin.flow.portal.VaadinPortlet;

public class MinimizedStateRenderPortlet
        extends VaadinPortlet<MinimizedStateRenderView>
        implements Portlet {
    @Override
    protected boolean shouldRenderMinimized() {
        return true;
    }
}

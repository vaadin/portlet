/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
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

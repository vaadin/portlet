/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.portal.liferay.streamresource;

import java.io.ByteArrayInputStream;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

public class LiferayStreamResourceContent extends VerticalLayout {

    static final String FILENAME = "export.xlsx";

    public LiferayStreamResourceContent() {
        StreamResource downloadResource = new StreamResource(FILENAME,
                () -> new ByteArrayInputStream(new byte[0]));
        downloadResource.setContentType("application/xls");
        downloadResource.setHeader("Content-Disposition",
                "attachment;filename=export.xlsx");
        Anchor link = new Anchor(downloadResource, "Download File");
        link.setId("downloadLink");
        add(link);
    }
}

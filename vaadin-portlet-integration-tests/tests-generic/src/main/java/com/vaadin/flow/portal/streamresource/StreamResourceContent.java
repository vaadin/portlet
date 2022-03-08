package com.vaadin.flow.portal.streamresource;

import java.io.ByteArrayInputStream;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

public class StreamResourceContent extends VerticalLayout {

    static final String FILENAME = "export.xlsx";

    public StreamResourceContent() {
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

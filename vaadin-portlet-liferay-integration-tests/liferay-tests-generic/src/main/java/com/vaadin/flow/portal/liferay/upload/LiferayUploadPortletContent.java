/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 */
package com.vaadin.flow.portal.liferay.upload;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

public class LiferayUploadPortletContent extends VerticalLayout {

    public static final String UPLOAD_LABEL_ID = "uploadLabel";

    public LiferayUploadPortletContent() {
        Label uploadInfo = new Label();
        uploadInfo.setId(UPLOAD_LABEL_ID);
        add(uploadInfo);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            int bytesRead = 0;
            try (InputStream is = buffer.getInputStream()) {
                while (is.read() != -1) {
                    bytesRead++;
                }
                uploadInfo.setText(Integer.toString(bytesRead));
            } catch (IOException ioe) {
                getLogger().error("Upload failed", ioe);
            }
        });
        upload.setAutoUpload(true);
        add(upload);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
}

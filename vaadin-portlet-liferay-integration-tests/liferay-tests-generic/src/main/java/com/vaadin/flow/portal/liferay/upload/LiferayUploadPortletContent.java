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

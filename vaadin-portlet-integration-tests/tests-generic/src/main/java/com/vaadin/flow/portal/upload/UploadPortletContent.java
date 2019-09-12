package com.vaadin.flow.portal.upload;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

public class UploadPortletContent extends VerticalLayout {

    public static final String UPLOAD_LABEL_ID = "uploadLabel";

    public UploadPortletContent() {
        Label uploadInfo = new Label();
        uploadInfo.setId(UPLOAD_LABEL_ID);
        add(uploadInfo);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            try {
                int bytesRead = 0;
                for (InputStream is = buffer.getInputStream();
                     is.read() != -1;
                     bytesRead++)
                    ;
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

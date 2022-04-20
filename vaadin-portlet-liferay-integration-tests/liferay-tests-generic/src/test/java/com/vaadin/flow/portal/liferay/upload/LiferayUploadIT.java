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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;

import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;

@NotThreadSafe
public class LiferayUploadIT extends AbstractLiferayPortalTest {

    @Test
    public void fileUploaded_fileSizeIsRendered() throws Exception {
        // upload a file
        File file = createTempFile();

        UploadElement upload = getVaadinPortletRootElement()
                .$(UploadElement.class).waitForFirst();
        WebElement input = upload.$("*").id("fileInput");
        setLocalFileDetector(input);
        input.sendKeys(file.getAbsolutePath());

        // check that label indicates size of file
        waitUntil(driver -> {
            LabelElement label = getVaadinPortletRootElement()
                    .$(LabelElement.class).id(LiferayUploadPortletContent.UPLOAD_LABEL_ID);
            return label != null && !"".equals(label.getText());
        });

        LabelElement label = getVaadinPortletRootElement()
                .$(LabelElement.class).id(LiferayUploadPortletContent.UPLOAD_LABEL_ID);
        Assert.assertEquals(Long.toString(file.length()), label.getText());
    }

    private File createTempFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("These are the contents of the uploaded file.");
        writer.close();
        tempFile.deleteOnExit();
        return tempFile;
    }

    private void setLocalFileDetector(WebElement element) {
        if (getRunLocallyBrowser() != null) {
            return;
        }

        if (element instanceof WrapsElement) {
            element = ((WrapsElement) element).getWrappedElement();
        }
        if (element instanceof RemoteWebElement) {
            ((RemoteWebElement) element)
                    .setFileDetector(new LocalFileDetector());
        } else {
            throw new IllegalArgumentException(
                    "Expected argument of type RemoteWebElement, received "
                            + element.getClass().getName());
        }
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/upload";
    }
}

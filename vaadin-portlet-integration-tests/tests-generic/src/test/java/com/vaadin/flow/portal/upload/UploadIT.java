package com.vaadin.flow.portal.upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;

import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.portal.AbstractPlutoPortalTest;

public class UploadIT extends AbstractPlutoPortalTest {

    public UploadIT() {
        super("upload");
    }

    @Test
    public void testUpload() throws Exception {
        // upload a file
        File file = createTempFile();
        UploadElement upload = $(UploadElement.class).first();
        WebElement input = getInShadowRoot(upload, By.id("fileInput"));
        setLocalFileDetector(input);
        input.sendKeys(file.getAbsolutePath());

        // check that label indicates size of file
        LabelElement label = $(LabelElement.class).id(UploadPortletContent.UPLOAD_LABEL_ID);
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

    private void setLocalFileDetector(WebElement element) throws Exception {
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
}

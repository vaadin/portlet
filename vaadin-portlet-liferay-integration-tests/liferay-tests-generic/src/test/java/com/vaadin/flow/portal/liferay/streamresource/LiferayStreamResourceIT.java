/**
 * Copyright (C) 2019-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal.liferay.streamresource;

import java.time.Duration;
import java.util.Map;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;

import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.portal.liferay.AbstractLiferayPortalTest;

@NotThreadSafe
public class LiferayStreamResourceIT extends AbstractLiferayPortalTest {

    @Test
    public void downloadStreamResource_responseHeadersAreSent() {
        AnchorElement link = getVaadinPortletRootElement()
                .$(AnchorElement.class).id("downloadLink");
        String url = link.getAttribute("href");
        getDriver().manage().timeouts().scriptTimeout(Duration.ofSeconds(15));

        Map<String, String> headers = downloadAndGetResponseHeaders(url);

        Assert.assertEquals(
                "attachment;filename=" + LiferayStreamResourceContent.FILENAME,
                headers.getOrDefault("content-disposition", null));
    }

    /*
     * Stolen from stackexchange.
     *
     * It's not possible to use a straight way to download the link externally
     * since it will use another session and the link will be invalid in this
     * session. So either this pure client side way or external download with
     * cookies copy (which allows preserve the session) needs to be used.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> downloadAndGetResponseHeaders(String url) {
        String script = "var url = arguments[0];"
                + "var callback = arguments[arguments.length - 1];"
                + "var xhr = new XMLHttpRequest();"
                + "xhr.open('GET', url, true);"
                + "xhr.responseType = \"arraybuffer\";" +
                // force the HTTP response, response-type header to be array
                // buffer
                "xhr.onload = function() {"
                // Get the raw header string "
                + "  var headers = xhr.getAllResponseHeaders();"
                // Convert the header string into an array
                // of individual headers
                + "  var arr = headers.trim().split(/[\\r\\n]+/);"
                // Create a map of header names to values
                + "  var headerMap = {};" + "  arr.forEach(function (line) { "
                + "    var parts = line.split(': '); "
                + "    var header = parts.shift().toLowerCase(); "
                + "    var value = parts.join(': '); "
                + "    headerMap[header] = value;" + "  }); "
                + "  callback(headerMap);" + "};" + "xhr.send();";
        Object response = ((JavascriptExecutor) getDriver())
                .executeAsyncScript(script, url);
        return (Map<String, String>) response;
    }

    @Override
    protected String getFriendlyUrl() {
        return "test/stream-resource";
    }
}

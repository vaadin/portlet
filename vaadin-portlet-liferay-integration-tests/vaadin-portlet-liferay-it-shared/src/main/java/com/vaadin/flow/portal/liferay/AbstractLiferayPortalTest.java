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
package com.vaadin.flow.portal.liferay;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.ParallelTest;

public abstract class AbstractLiferayPortalTest extends ParallelTest {

    /**
     * Property set to true when running on a test hub.
     */
    private static final String USE_HUB_PROPERTY = "test.use.hub";

    private static final int SERVER_PORT = 8080;

    private static final String PORTLET_ID_ATTRIBUTE = "data-portlet-id";

    private static final String PORTAL_ROUTE = "en/web/guest/";
    private static final String PORTAL_LOGIN = "en/c/portal/login";

    private static final String PORTLET_ID_PARAMETER_KEY = "p_p_id";
    private static final String PORTLET_STATE_PARAMETER_KEY = "p_p_state";
    private static final String PORTLET_MODE_PARAMETER_KEY = "p_p_mode";

    public static final String PORTLET_ID_PATTERN_STRING = "^_(.*_WAR_.*)(_INSTANCE_.*)?_$";
    public static final Pattern PORTLET_ID_PATTERN = Pattern
            .compile(PORTLET_ID_PATTERN_STRING);

    private static final String LOGIN_FORM_USER_LOGIN = "_com_liferay_login_web_portlet_LoginPortlet_login";
    public static final String LOGIN_FORM_USER_PASSWORD = "_com_liferay_login_web_portlet_LoginPortlet_password";

    private String portletId = null;

    private final Map<String, String> portletIdByStaticPart = new HashMap<>();

    @Rule
    public ScreenshotOnFailureRule rule = new ScreenshotOnFailureRule(this,
            false);

    @Override
    @Before
    public void setup() throws Exception {
        if (isUsingHub()) {
            super.setup();
        } else {
            setDriver(TestBench.createDriver(new ChromeDriver()));
        }
        loginToPortal();
        openPortletLayout();
    }

    protected void openPortletLayout() {
        getDriver().get(getURL(getFriendlyUrl()));
        waitUntil(driver -> {
            List<TestBenchElement> portlets = getVaadinPortletRootElements();
            return portlets != null && !portlets.isEmpty();
        }, 20);

        List<TestBenchElement> portlets = getVaadinPortletRootElements();

        if (portlets.size() == 1 && portletId == null) {
            portletId = portlets.get(0).getAttribute(PORTLET_ID_ATTRIBUTE);
        }

        if (portletIdByStaticPart.isEmpty()) {
            portlets.forEach(portlet -> {
                String portletIdAttribute = portlet
                        .getAttribute(PORTLET_ID_ATTRIBUTE);
                Matcher matcher = PORTLET_ID_PATTERN
                        .matcher(portletIdAttribute);
                if (matcher.matches()) {
                    String staticPart = matcher.group(1);
                    portletIdByStaticPart.put(staticPart, portletIdAttribute);
                } else {
                    throw new IllegalStateException(
                            "Found portlet with unrecognizable ID");
                }
            });
        }
    }

    protected void loginToPortal() {
        getDriver().get(getURL(PORTAL_LOGIN));
        waitUntil(driver -> {
            WebElement element = driver
                    .findElement(By.id(LOGIN_FORM_USER_LOGIN));
            return element != null;
        });

        final WebElement username = findElement(By.id(LOGIN_FORM_USER_LOGIN));
        final WebElement password = findElement(
                By.id(LOGIN_FORM_USER_PASSWORD));

        WebElement submitButton = findElement(
                By.xpath("//button[@type='submit' and span='Sign In']"));
        username.clear();
        username.sendKeys("test@liferay.com");
        password.clear();
        password.sendKeys("test");
        submitButton.click();

        // Wait for home page to be loaded, checking for user menu
        waitUntil(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.personal-menu-dropdown")));
    }

    protected String openInAnotherWindow() {
        final String firstWin = getDriver().getWindowHandle();
        ((JavascriptExecutor) getDriver()).executeScript("window.open('"
                + getURL(getPortalRoute() + "/" + getFriendlyUrl())
                + "','_blank');");
        final String secondWin = driver.getWindowHandles().stream()
                .filter(windowId -> !windowId.equals(firstWin)).findFirst()
                .orElseThrow(
                        () -> new AssertionError("Secoond window not found"));
        driver.switchTo().window(secondWin);
        return secondWin;
    }

    protected List<TestBenchElement> getVaadinPortletRootElements() {
        return $(TestBenchElement.class).hasAttribute(PORTLET_ID_ATTRIBUTE)
                .all();
    }

    protected TestBenchElement getVaadinPortletRootElementByStaticPart(
            String staticIdPart) {
        return getVaadinPortletRootElement(
                portletIdByStaticPart.get(staticIdPart));
    }

    protected TestBenchElement getVaadinPortletRootElement(String id) {
        return $(TestBenchElement.class).hasAttribute(PORTLET_ID_ATTRIBUTE)
                .attribute(PORTLET_ID_ATTRIBUTE, id).waitForFirst();
    }

    protected TestBenchElement getVaadinPortletRootElement() {
        if (portletId == null) {
            throw new AssertionError(
                    "no Vaadin Portlet added or page contains multiple portlets");
        }
        return getVaadinPortletRootElement(portletId);
    }

    /**
     * Set the mode of the first portlet on page via Liferay's header dropdown.
     */
    protected void setPortletModeInPortal(PortletMode portletMode) {
        setStateInPortal(PORTLET_MODE_PARAMETER_KEY,
                portletMode.toString().toLowerCase());
    }

    private void setStateInPortal(String key, String value) {
        String currentUrl = getDriver().getCurrentUrl();
        // Exclude '_' characters around portlet id
        String trimmedPortletId = portletId.substring(1,
                portletId.length() - 1);
        String url = appendPortletQueryParameter(currentUrl,
                PORTLET_ID_PARAMETER_KEY, trimmedPortletId);
        String newUrl = appendPortletQueryParameter(url, key, value);
        getDriver().get(newUrl);
    }

    /**
     * Set the mode of the first portlet on page via Pluto's header dropdown.
     */
    protected void setWindowStateInPortal(WindowState windowState) {
        setStateInPortal(PORTLET_STATE_PARAMETER_KEY,
                windowState.toString().toLowerCase());
    }

    protected String getWindowStateInPortal() {
        return getPortletQueryParameter(PORTLET_STATE_PARAMETER_KEY);
    }

    protected String getPortletModeInPortal() {
        return getPortletQueryParameter(PORTLET_MODE_PARAMETER_KEY);
    }

    /**
     * Gets the portal route.
     *
     * @return the portal route
     */
    protected String getPortalRoute() {
        return PORTAL_ROUTE;
    }

    /**
     * Gets the portal page.
     *
     * @return the portal page
     */
    protected abstract String getFriendlyUrl();

    /**
     * Returns deployment host name concatenated with route.
     *
     * @return URL to route
     */
    protected static String getURL(String route) {
        return String.format("http://%s:%d/%s", getDeploymentHostname(),
                SERVER_PORT, route);
    }

    /**
     * Returns whether we are using a test hub. This means that the starter is
     * running tests in Vaadin's CI environment, and uses TestBench to connect
     * to the testing hub.
     *
     * @return whether we are using a test hub
     */
    private static boolean isUsingHub() {
        return Boolean.TRUE.toString()
                .equals(System.getProperty(USE_HUB_PROPERTY));
    }

    /**
     * If running on CI, get the host name from environment variable HOSTNAME
     *
     * @return the host name
     */
    private static String getDeploymentHostname() {
        return isUsingHub() ? System.getenv("HOSTNAME") : "localhost";
    }

    private String appendPortletQueryParameter(String url, String key,
            String value) {
        String portletModeQueryParamRegexp = "\\b" + key + "=.*?(&|$)";
        String newPortletModeQueryParam = key + "=" + value;
        String newUrl = url.replaceFirst(portletModeQueryParamRegexp,
                newPortletModeQueryParam + "$1");
        if (newUrl.equals(url)) {
            if (newUrl.contains("?")) {
                newUrl = newUrl + "&" + newPortletModeQueryParam;
            } else {
                newUrl = newUrl + "?" + newPortletModeQueryParam;
            }
        }
        return newUrl;
    }

    private String getPortletQueryParameter(String key) {
        String currentUrl = getDriver().getCurrentUrl();
        String portletModeQueryParamRegexp = key + "=(.*?)(&|$)";
        Pattern compile = Pattern.compile(portletModeQueryParamRegexp);
        Matcher matcher = compile.matcher(currentUrl);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        } else {
            return key.equals(PORTLET_MODE_PARAMETER_KEY) ? "VIEW" : "NORMAL";
        }
    }

}

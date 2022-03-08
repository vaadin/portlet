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
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.ParallelTest;

/**
 * Base class for ITs
 * <p>
 * The tests use Chrome driver (see pom.xml for integration-tests profile) to
 * run integration tests on a headless Chrome. If a property {@code test.use
 * .hub} is set to true, {@code AbstractViewTest} will assume that the TestBench
 * test is running in a CI environment. In order to keep the this class light,
 * it makes certain assumptions about the CI environment (such as available
 * environment variables). It is not advisable to use this class as a base class
 * for you own TestBench tests.
 * <p>
 * To learn more about TestBench, visit <a href=
 * "https://vaadin.com/docs/v10/testbench/testbench-overview.html">Vaadin
 * TestBench</a>.
 */
public abstract class AbstractLiferayPortalTest extends ParallelTest {

    /**
     * Property set to true when running on a test hub.
     */
    private static final String USE_HUB_PROPERTY = "test.use.hub";

    private static final int SERVER_PORT = 8080;

    private static final String PORTLET_ID_ATTRIBUTE = "data-portlet-id";

    private static final String PORTAL_ROUTE = "web/guest/";
    private static final String PORTAL_LOGIN = "en/c/portal/login";
    private static final String ADMIN_PAGE_FRAGMENT = "Pluto Admin";

    private static final String LOGIN_FORM_USER_LOGIN =
            "_com_liferay_login_web_portlet_LoginPortlet_login";
    public static final String LOGIN_FORM_USER_PASSWORD =
            "_com_liferay_login_web_portlet_LoginPortlet_password";

    private final String layoutName;
    private String testPage;
    private final String portletName;
    private String portletId = null;

    public AbstractLiferayPortalTest(String layoutName, String portletName) {
        this.layoutName = layoutName;
        this.portletName = portletName;
    }

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
        getDriver().get(getURL(PORTAL_LOGIN));
        loginToPortal();
        openPortletLayout();
    }

    protected void openPortletLayout() {
        getDriver().get(getURL(getPortalRoute() + "/" + getFriendlyUrl()));

        List<TestBenchElement> portlets = getVaadinPortletRootElements();
        if (portlets == null || portlets.isEmpty()) {
            throw new AssertionError("No portlet found on the page");
        }

        if (portlets.size() > 1) {
            throw new AssertionError("Expected only one portlet per page");
        }

        if (portletId == null) {
            portletId = portlets.get(0).getAttribute(PORTLET_ID_ATTRIBUTE);
        }
    }

    protected void loginToPortal() {
        waitUntil(driver -> {
            WebElement element = driver.findElement(By.id(LOGIN_FORM_USER_LOGIN));
            return element != null;
        });

        final WebElement username = findElement(By.id(LOGIN_FORM_USER_LOGIN));
        final WebElement password = findElement(By.id(LOGIN_FORM_USER_PASSWORD));

        WebElement submitButton = findElement(By.xpath(
                "//button[@type='submit' and span='Sign In']"));
        username.sendKeys("test@liferay.com");
        password.sendKeys("test");
        submitButton.click();
    }

    protected List<TestBenchElement> getVaadinPortletRootElements() {
        return $(TestBenchElement.class).hasAttribute(PORTLET_ID_ATTRIBUTE)
                .all();
    }

    protected TestBenchElement getVaadinPortletRootElement(
            String id) {
        return $(TestBenchElement.class).hasAttribute(PORTLET_ID_ATTRIBUTE)
                .attribute(PORTLET_ID_ATTRIBUTE,id)
                .waitForFirst();
    }

    protected TestBenchElement getVaadinPortletRootElement() {
        if (portletId == null) {
            throw new AssertionError("no Vaadin Portlet added");
        }
        return getVaadinPortletRootElement(portletId);
    }

    /**
     * Set the mode of the first portlet on page via Pluto's header dropdown.
     */
    protected void setPortletModeInPortal(PortletMode portletMode) {
        SelectElement modeSelector = $(TestBenchElement.class)
                .attribute("name", "modeSelectionForm").first()
                .$(SelectElement.class).first();
        modeSelector.selectByText(portletMode.toString().toUpperCase());
    }

    /**
     * Set the mode of the first portlet on page via Pluto's header dropdown.
     */
    protected void setWindowStateInPortal(WindowState windowState) {
        String buttonLabel = WindowState.MAXIMIZED.equals(windowState)
                ? "Maximize"
                : WindowState.NORMAL.equals(windowState) ? "Restore"
                : WindowState.MINIMIZED.equals(windowState) ? "Minimize"
                : null;
        AnchorElement anchor = $(AnchorElement.class)
                .attribute("title", buttonLabel).first();
        anchor.click();
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

}

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
package com.vaadin.flow.portal;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.parallel.ParallelTest;

/**
 * Base class for ITs
 * <p>
 * The tests use Chrome driver (see pom.xml for integration-tests profile) to
 * run integration tests on a headless Chrome. If a property {@code test.use
 * .hub} is set to true, {@code AbstractViewTest} will assume that the
 * TestBench test is running in a CI environment. In order to keep the this
 * class light, it makes certain assumptions about the CI environment (such
 * as available environment variables). It is not advisable to use this class
 * as a base class for you own TestBench tests.
 * <p>
 * To learn more about TestBench, visit
 * <a href="https://vaadin.com/docs/v10/testbench/testbench-overview.html">Vaadin TestBench</a>.
 */
public abstract class AbstractPlutoPortalTest extends ParallelTest {
    private static final int SERVER_PORT = 8080;

    private final String route = "pluto/portal";
    private final String warName = "tests-generic";
    private final String testPage = "IT";
    private final String adminPage = "Pluto Admin";
    private final String portletName;

    public AbstractPlutoPortalTest(String portletName) {
        this.portletName = portletName;
    }

    @Rule
    public ScreenshotOnFailureRule rule = new ScreenshotOnFailureRule(this,
            false);

    @Before
    public void setup() throws Exception {
        if (isUsingHub()) {
            super.setup();
        } else {
            setDriver(TestBench.createDriver(new ChromeDriver()));
        }
        getDriver().get(getURL(route));
        loginToPortal();
        addPortlet();
    }

    @After
    public void tearDown() {
        removePortletPage();
    }

    protected void loginToPortal() {
        final WebElement username = findElement(By.id("j_username"));
        final WebElement password = findElement(By.id("j_password"));
        final WebElement login = findElement(By.id("j_login"));
        username.sendKeys("pluto");
        password.sendKeys("pluto");
        login.click();
    }

    protected void addPortlet() {
        getDriver().get(getURL(route + "/" + adminPage));

        // Create a new page
        findElement(By.name("newPage")).sendKeys(testPage);
        findElement(By.id("addPageButton")).click();

        // Add the portlet
        Map<String, SelectElement> nameMap = $(SelectElement.class).all().stream().collect(Collectors
                .toMap(selectElement -> selectElement.getAttribute("name"),
                        Function.identity(), (oldValue, newValue) -> oldValue));
        nameMap.get("page").selectByText(testPage);
        nameMap.get("applications").selectByText("/" + warName);
        nameMap.get("availablePortlets").selectByText(portletName);
        findElement(By.id("addButton")).click();

        // go to portlet page
        getDriver().get(getURL(route + "/" + testPage));
    }

    protected void removePortletPage() {
        getDriver().get(getURL(route + "/" + adminPage));
        Map<String, SelectElement> nameMap = $(SelectElement.class).all().stream().collect(Collectors
                .toMap(selectElement -> selectElement.getAttribute("name"),
                        Function.identity(), (oldValue, newValue) -> oldValue));
        nameMap.get("page").selectByText(testPage);
        findElement(By.id("removePageButton")).click();
    }

    /**
     * Find the first {@link WebElement} using the given {@link By} selector.
     *
     * @param shadowRootOwner
     *            the web component owning shadow DOM to start search from
     * @param by
     *            the selector used to find element
     * @return an element from shadow root, if located
     * @throws AssertionError
     *             if shadow root is not present or element is not found in the
     *             shadow root
     */
    protected WebElement getInShadowRoot(WebElement shadowRootOwner, By by) {
        return getShadowRoot(shadowRootOwner).findElements(by).stream()
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Could not find required element in the shadowRoot"));
    }

    private WebElement getShadowRoot(WebElement webComponent) {
        waitUntil(driver -> getCommandExecutor().executeScript(
                "return arguments[0].shadowRoot", webComponent) != null);
        WebElement shadowRoot = (WebElement) getCommandExecutor()
                .executeScript("return arguments[0].shadowRoot", webComponent);
        Assert.assertNotNull("Could not locate shadowRoot in the element",
                shadowRoot);
        return shadowRoot;
    }

    /**
     * Property set to true when running on a test hub.
     */
    private static final String USE_HUB_PROPERTY = "test.use.hub";

    /**
     * Returns deployment host name concatenated with route.
     *
     * @return URL to route
     */
    private static String getURL(String route) {
        return String.format("http://%s:%d/%s", getDeploymentHostname(),
                SERVER_PORT, route);
    }

    /**
     * Returns whether we are using a test hub. This means that the starter
     * is running tests in Vaadin's CI environment, and uses TestBench to
     * connect to the testing hub.
     *
     * @return whether we are using a test hub
     */
    private static boolean isUsingHub() {
        return Boolean.TRUE.toString().equals(
                System.getProperty(USE_HUB_PROPERTY));
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
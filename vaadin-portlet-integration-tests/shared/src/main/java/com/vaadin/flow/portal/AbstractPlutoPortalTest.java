/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.portal;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.html.testbench.SelectElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.ParallelTest;

/**
 * Base class for ITs running in the Apache Pluto container.
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
public abstract class AbstractPlutoPortalTest extends ParallelTest {

    /**
     * Property set to true when running on a test hub.
     */
    private static final String USE_HUB_PROPERTY = "test.use.hub";

    private static final int SERVER_PORT = 8080;

    private static final String PORTLET_ID_ATTRIBUTE = "data-portlet-id";

    private static final String PORTAL_ROUTE = "pluto/portal";
    private static final String ADMIN_PAGE_FRAGMENT = "Pluto Admin";

    private final String warName;
    private String testPage;
    private final String portletName;
    private String firstPortletId = null;

    public AbstractPlutoPortalTest(String warName, String portletName) {
        this.warName = warName;
        this.portletName = portletName;
    }

    @Rule
    public ScreenshotOnFailureRule rule = new ScreenshotOnFailureRule(this,
            false);

    /**
     * Sets up the chrome driver path in a system variable.
     */
    @BeforeClass
    public static void setChromeDriverPath() {
        if (isUsingHub()) {
            return;
        }
        String chromedriverProperty = System
                .getProperty("webdriver.chrome.driver");
        if (chromedriverProperty == null
                || !new File(chromedriverProperty).exists()) {
            // This sets the same property
            WebDriverManager.chromedriver().setup();
        } else {
            System.out
                    .println("Using chromedriver from " + chromedriverProperty);
        }
    }

    @Override
    @Before
    public void setup() throws Exception {
        if (isUsingHub()) {
            super.setup();
        } else {
            ChromeOptions options = new ChromeOptions();
            if (!isJavaInDebugMode()) {
                options.addArguments("--headless", "--disable-gpu");
            }
            setDriver(TestBench.createDriver(new ChromeDriver(options)));
        }
        getDriver().get(getURL(PORTAL_ROUTE));
        loginToPortal();
        addVaadinPortlet(portletName);
    }

    static boolean isJavaInDebugMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments()
                .toString().contains("jdwp");
    }

    protected void loginToPortal() {
        if (!findElements(By.id("j_login")).isEmpty()) {
            final WebElement username = findElement(By.id("j_username"));
            final WebElement password = findElement(By.id("j_password"));
            final WebElement login = findElement(By.id("j_login"));
            username.sendKeys("pluto");
            password.sendKeys("pluto");
            login.click();
        }
    }

    /**
     * Adds a new {@code portlet} to the {@code page}.
     * <p>
     * A new page is created if {@code page} is {@code null}.
     *
     * @param portlet
     *            the portlet name
     * @return the portlet instance ID for uniquely identifying it
     */
    protected String addVaadinPortlet(String portlet) {
        createTestPageIfNotExists();

        // Go to the page and collect the portlet names
        getDriver().get(getURL(getPortalRoute() + "/" + getPage()));
        final Set<String> portletIds = getVaadinPortletRootElements().stream()
                .map(we -> we.getAttribute(PORTLET_ID_ATTRIBUTE))
                .collect(Collectors.toSet());

        addPortlet(portlet);

        // Added portlet is expected to be the only new portlet amongst all the
        // portlets on the page
        Set<String> newPortletId = getVaadinPortletRootElements().stream()
                .map(we -> we.getAttribute(PORTLET_ID_ATTRIBUTE))
                .filter(id -> !portletIds.contains(id))
                .collect(Collectors.toSet());
        if (newPortletId.isEmpty()) {
            throw new AssertionError("no new portlet added");
        } else if (newPortletId.size() > 1) {
            throw new AssertionError("expected only one portal to be added");
        }
        String portletId = newPortletId.iterator().next();
        if (firstPortletId == null) {
            firstPortletId = portletId;
        }
        return portletId;
    }

    /**
     * Adds a new {@code portlet} to the {@code page}.
     * <p>
     * A new page is created if {@code page} is {@code null}.
     *
     * @param portlet
     *            the portlet name
     */
    protected void addPortlet(String portlet) {
        createTestPageIfNotExists();

        // Go to admin
        getDriver().get(getURL(PORTAL_ROUTE + "/" + ADMIN_PAGE_FRAGMENT));

        // Add the portlet
        Map<String, SelectElement> nameMap = $(SelectElement.class).all()
                .stream()
                .collect(Collectors.toMap(
                        selectElement -> selectElement.getAttribute("name"),
                        Function.identity(), (oldValue, newValue) -> oldValue));
        nameMap.get("page").selectByText(testPage);
        nameMap.get("applications").selectByText("/" + warName);
        nameMap.get("availablePortlets").selectByText(portlet);
        findElement(By.id("addButton")).click();

        getDriver().get(getURL(getPortalRoute() + "/" + getPage()));

        // Wait for the portlet to appear on the page
        waitUntil(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h2[contains(text(),'" + portlet + "')]")));
    }

    protected void createTestPageIfNotExists() {
        if (testPage == null) {
            // Go to admin
            getDriver().get(getURL(PORTAL_ROUTE + "/" + ADMIN_PAGE_FRAGMENT));

            testPage = String.format("IT-%d",
                    new Random().nextInt(Integer.MAX_VALUE));
            findElement(By.name("newPage")).sendKeys(testPage);
            findElement(By.id("addPageButton")).click();
        }
    }

    protected void removePortletPage() {
        getDriver().get(getURL(PORTAL_ROUTE + "/" + ADMIN_PAGE_FRAGMENT));
        Map<String, SelectElement> nameMap = $(SelectElement.class).all()
                .stream()
                .collect(Collectors.toMap(
                        selectElement -> selectElement.getAttribute("name"),
                        Function.identity(), (oldValue, newValue) -> oldValue));
        nameMap.get("page").selectByText(testPage);
        findElement(By.id("removePageButton")).click();
    }

    protected String openInAnotherWindow() {
        final String firstWin = getDriver().getWindowHandle();
        ((JavascriptExecutor) getDriver()).executeScript("window.open('"
                + getURL(PORTAL_ROUTE + "/" + testPage) + "','_blank');");
        final String secondWin = driver.getWindowHandles().stream()
                .filter(windowId -> !windowId.equals(firstWin)).findFirst()
                .get();
        driver.switchTo().window(secondWin);
        return secondWin;
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
        if (firstPortletId == null) {
            throw new AssertionError("no Vaadin Portlet added");
        }
        return getVaadinPortletRootElement(firstPortletId);
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
    protected String getPage() {
        return testPage;
    }

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

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class OptimizedLoginTest {
    static private Playwright playwright;
    static private Browser browser;
    private BrowserContext context;
    private Page page;
    static private List<com.microsoft.playwright.options.Cookie> authCookies;
    private static TestReport report = TestReport.getInstance();

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        
        BrowserContext loginContext = browser.newContext();
        Page loginPage = loginContext.newPage();
        authCookies = performLogin(loginPage);
        loginContext.close();
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        report.startTest(testInfo.getDisplayName());
        context = browser.newContext();
        context.addCookies(authCookies);
        page = context.newPage();
    }

    @Test
    void testSecureArea() {
        try {
            page.navigate("https://the-internet.herokuapp.com/secure");
            Assertions.assertTrue(page.locator("h2").textContent().contains("Secure Area"));
            report.endTest(true);
        } catch (AssertionError | Exception e) {
            takeScreenshot("testSecureArea_failed");
            report.endTest(false);
            throw e;
        }
    }

    private void takeScreenshot(String fileName) {
        try {
            String screenshotPath = "test-reports/screenshots/" + fileName + "_" + 
                System.currentTimeMillis() + ".png";
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(screenshotPath))
                .setFullPage(true));
            report.addScreenshot(screenshotPath);
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    private static List<com.microsoft.playwright.options.Cookie> performLogin(Page page) {
        page.navigate("https://the-internet.herokuapp.com/login");
        page.fill("#username", "tomsmith");
        page.fill("#password", "SuperSecretPassword!");
        page.click("button[type='submit']");
        
        return page.context().cookies();
    }

    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        report.generateReport();
    }
}
package demo;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.rmi.server.ExportException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import io.github.bonigarcia.wdm.WebDriverManager;

public class TestCases {
    ChromeDriver driver;
    WebDriverWait wait;

    @SuppressWarnings("deprecation")
    public TestCases() {
        System.out.println("Constructor: TestCases");
        WebDriverManager.chromedriver().timeout(120).setup();
        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "chromedriver.log");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, Duration.ofSeconds(120));
    }

    public void endTest() {
        System.out.println("End Test: TestCases");
        driver.close();
        driver.quit();

    }

    public void logStatus(boolean status, String description) {
        if(status) {
            System.out.println(description + ": PASS");
        }
        else {
            System.out.println(description + ": FAIL");
        }
    }

    public void testCase01() {
        System.out.println("Start testCase01 --> Verify the Xflix Homepage URL");
        driver.get("https://xflix-qa.vercel.app/");
        boolean status = driver.getCurrentUrl().contains("xflix");
        logStatus(status, "testCase01");
    }

    public void testCase02() {
        System.out.println("Start testCase02 --> Verify Search Functionality");
        driver.get("https://xflix-qa.vercel.app/");
        driver.findElement(By.xpath("//input[@class='search-input']")).sendKeys("frameworks");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='dashboard-grid-item']")));
        List<WebElement> results = driver.findElements(By.xpath("//div[@class='dashboard-grid-item']"));
        for(WebElement we : results) {
            boolean status = we.findElement(By.xpath(".//p[@class='video-title']")).getText().contains("frameworks");
            logStatus(status, "step-testCase02");
        }

        driver.findElement(By.xpath("//input[@class='search-input']")).clear();
        driver.findElement(By.xpath("//input[@class='search-input']")).sendKeys("selenium");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='no-search-txt']")));
        boolean status = driver.findElement(By.xpath("//div[@class='no-search-txt']")).isDisplayed();
        logStatus(status, "testCase02");

    }

    public void testCase03() {
        System.out.println("Start testCase03 --> Verify the Functionality of Filters");
        driver.get("https://xflix-qa.vercel.app/");
        List<WebElement> beforeResults = driver.findElements(By.xpath("//div[@class='video-card']"));
        WebElement dropDownElement = driver.findElement(By.id("sortBySelect"));
        Select select = new Select(dropDownElement);
        select.selectByVisibleText("Sort By: View Count");
        List<WebElement> afterResults = driver.findElements(By.xpath("//div[@class='video-card']"));
        boolean status = !beforeResults.equals(afterResults);
        logStatus(status, "testCase03");
    }

    public void testCase04() {
        System.out.println("Start testCase04 --> Verify the Functionality of Upload Video");
        driver.get("https://xflix-qa.vercel.app/");
        driver.findElement(By.xpath("//button[@class='header-btn btn-upload']")).click();
        WebElement parentModal = driver.findElement(By.xpath("//div[@class='modal-content']"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='modal-content']")));
        driver.findElement(By.xpath("//button[text()='upload video']")).click();
        Alert alerts = driver.switchTo().alert();
        boolean status = alerts.getText().contains("not be empty");
        logStatus(status, "step-testCase04");
        alerts.accept();
        String embeedLink = "https://www.youtube.com/embed/GWfYHEuWh-k?si=UDwVjalFUBUEXxUT";
        driver.findElement(By.xpath("//input[@placeholder='Video Link']")).sendKeys(embeedLink);
        driver.findElement(By.xpath("//input[@placeholder='Thumbnail Image Link']")).sendKeys(embeedLink);
        driver.findElement(By.xpath("//input[@placeholder='Title']")).sendKeys("Test video title");
        Select genreDropdown = new Select(driver.findElement(By.id("genre-modal-dropdown")));
        genreDropdown.selectByValue("Comedy");

        Select ageDropdown = new Select(driver.findElement(By.id("age-modal-dropdown")));
        ageDropdown.selectByVisibleText("18+");

        driver.findElement(By.xpath("//input[@type='date']")).sendKeys("16-02-1996");

        driver.findElement(By.xpath("//button[text()='upload video']")).click();

        Alert alertSuccess = driver.switchTo().alert();
        status = alertSuccess.getText().equals("Video Posted Successfully!");
        alertSuccess.accept();

        logStatus(status, "testCase04");

    }

    public void testCase05() {
        System.out.println("Start testCase05 --> Verify the Like Counter Functionality");
        driver.get("https://xflix-qa.vercel.app/");
        List<WebElement> results = driver.findElements(By.xpath("//div[@class='video-card']"));
        results.get(0).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@class='btn btn-like']")));

        int beforeLikeCount = Integer.parseInt(driver.findElement(By.xpath("//button[@class='btn btn-like']")).getText().trim());

        driver.findElement(By.xpath("//button[@class='btn btn-like']")).click();
        String parentUrl = driver.getCurrentUrl();

        driver.switchTo().newWindow(WindowType.TAB);
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        List<String> windowHandles = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(windowHandles.get(1));
        driver.get(parentUrl);

        int afterLikeCount = Integer.parseInt(driver.findElement(By.xpath("//button[@class='btn btn-like']")).getText().trim());

        boolean status = beforeLikeCount==afterLikeCount;
        logStatus(!status, "testCase05");
        driver.close();
        driver.switchTo().window(windowHandles.get(0));
    }
}
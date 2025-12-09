package com.example.exercices;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EXERCICE 1 : Recherche Google avec Firefox - Tests JUnit 5
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Exercice1_RechercheGoogleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String SCREENSHOTS_DIR = "screenshots/";

    @BeforeAll
    static void setUp() {
        System.setProperty("webdriver.gecko.driver",
            "C:\\Users\\ACHRAF\\Downloads\\geckodriver-v0.36.0-win32\\geckodriver.exe");

        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("C:\\Program Files\\Firefox Developer Edition\\firefox.exe");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        
        // Creer le dossier screenshots s'il n'existe pas
        new File(SCREENSHOTS_DIR).mkdirs();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Capture une screenshot et la sauvegarde dans le dossier screenshots
     */
    private void captureScreenshot(String testName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destination = new File(SCREENSHOTS_DIR + testName + ".png");
            FileUtils.copyFile(screenshot, destination);
            System.out.println("📸 Screenshot sauvegardee: " + destination.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erreur lors de la capture de screenshot: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test 1.1 - Acces a Google et recherche Selenium WebDriver")
    void testRechercheGoogle() throws InterruptedException {
        System.out.println("\n📍 Etape 1 : Ouverture de Google...");
        driver.get("https://www.google.com");
        Thread.sleep(2000);

        // Accepter les cookies si necessaire
        try {
            WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Accepter') or contains(., 'Accept') or contains(., 'Tout accepter')]")
            ));
            acceptCookies.click();
            System.out.println("🍪 Cookies acceptes");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("ℹ️ Pas de popup de cookies");
        }

        // Capture screenshot apres chargement de Google
        captureScreenshot("01_google_homepage");

        // Localiser la barre de recherche
        System.out.println("🔍 Etape 2 : Localisation de la barre de recherche...");
        WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("q")));
        assertNotNull(searchBox, "La barre de recherche doit etre presente");

        // Saisir la requete
        String requete = "Selenium WebDriver";
        System.out.println("⌨️ Etape 3 : Saisie de la requete : '" + requete + "'");
        searchBox.sendKeys(requete);
        Thread.sleep(500);

        // Capture screenshot avec la requete saisie
        captureScreenshot("02_google_search_input");

        // Soumettre la recherche
        System.out.println("↩️ Etape 4 : Soumission de la recherche...");
        searchBox.sendKeys(Keys.RETURN);

        // Attendre et verifier les resultats
        System.out.println("⏳ Etape 5 : Attente des resultats...");
        Thread.sleep(2000);
        
        // Capture screenshot des resultats
        captureScreenshot("03_google_search_results");

        // Verification avec assertion
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.toLowerCase().contains("selenium"), 
            "Le titre de la page doit contenir 'selenium'");
        
        System.out.println("✅ SUCCES : Les resultats de recherche s'affichent correctement !");
        System.out.println("📄 Titre de la page: " + pageTitle);
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2 - Verification des resultats de recherche non vides")
    void testResultatsNonVides() throws InterruptedException {
        // S'assurer qu'on est sur la page de resultats
        if (!driver.getCurrentUrl().contains("google.com/search")) {
            driver.get("https://www.google.com/search?q=Selenium+WebDriver");
            Thread.sleep(2000);
        }

        // Verifier que l'URL contient la recherche
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("search") || currentUrl.contains("q="), 
            "L'URL doit contenir une recherche");
        
        // Verifier que la page contient du contenu
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Selenium") || pageSource.contains("selenium"), 
            "La page doit contenir des resultats lies a Selenium");
        
        System.out.println("✅ La page de resultats contient des informations sur Selenium");
        
        // Capture screenshot finale
        captureScreenshot("04_google_results_verification");
    }
}

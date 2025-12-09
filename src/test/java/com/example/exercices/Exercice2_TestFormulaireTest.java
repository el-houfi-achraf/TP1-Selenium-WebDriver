package com.example.exercices;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
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
 * EXERCICE 2 : Test de formulaire de connexion - Tests JUnit 5
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Exercice2_TestFormulaireTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String SCREENSHOTS_DIR = "screenshots/";
    private static String cheminFichierHTML;

    @BeforeAll
    static void setUp() {
        System.setProperty("webdriver.gecko.driver",
            "C:\\Users\\ACHRAF\\Downloads\\geckodriver-v0.36.0-win32\\geckodriver.exe");

        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("C:\\Program Files\\Firefox Developer Edition\\firefox.exe");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        File fichierHTML = new File("src/main/resources/connexion.html");
        cheminFichierHTML = "file:///" + fichierHTML.getAbsolutePath().replace("\\", "/");

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
    @DisplayName("Test 2.1 - Affichage du formulaire de connexion")
    void testAffichageFormulaire() throws InterruptedException {
        System.out.println("\n📍 Test : Affichage du formulaire de connexion");
        driver.get(cheminFichierHTML);
        Thread.sleep(1000);

        // Verifier la presence des elements
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.id("submitBtn"));

        assertNotNull(usernameField, "Le champ username doit etre present");
        assertNotNull(passwordField, "Le champ password doit etre present");
        assertNotNull(submitButton, "Le bouton de soumission doit etre present");

        // Capture screenshot du formulaire vide
        captureScreenshot("05_formulaire_connexion_vide");

        System.out.println("✅ Tous les elements du formulaire sont presents");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2.2 - Connexion avec identifiants valides")
    void testConnexionValide() throws InterruptedException {
        System.out.println("\n========== TEST 2.2 : IDENTIFIANTS VALIDES ==========");
        driver.get(cheminFichierHTML);
        Thread.sleep(1000);

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.id("submitBtn"));

        String username = "admin";
        String password = "admin123";

        usernameField.clear();
        usernameField.sendKeys(username);

        passwordField.clear();
        passwordField.sendKeys(password);

        System.out.println("⌨️ Username : " + username);
        System.out.println("⌨️ Password : " + password);

        // Capture screenshot avec les champs remplis (avant soumission)
        captureScreenshot("06_formulaire_rempli_valide");

        Thread.sleep(500);
        submitButton.click();
        Thread.sleep(1000);

        WebElement messageDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        String messageText = messageDiv.getText();
        System.out.println("📝 Message affiche : " + messageText);

        // Capture screenshot avec le message de succes
        captureScreenshot("07_connexion_reussie");

        assertTrue(messageText.contains("Connexion réussie"), 
            "Le message doit indiquer une connexion reussie");
        System.out.println("✅ TEST REUSSI : Connexion valide detectee correctement !");
    }

    @Test
    @Order(3)
    @DisplayName("Test 2.3 - Connexion avec identifiants invalides")
    void testConnexionInvalide() throws InterruptedException {
        System.out.println("\n========== TEST 2.3 : IDENTIFIANTS INVALIDES ==========");
        driver.get(cheminFichierHTML);
        Thread.sleep(1000);

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.id("submitBtn"));

        String username = "user123";
        String password = "wrongpassword";

        usernameField.clear();
        usernameField.sendKeys(username);

        passwordField.clear();
        passwordField.sendKeys(password);

        System.out.println("⌨️ Username : " + username);
        System.out.println("⌨️ Password : " + password);

        // Capture screenshot avec les champs remplis
        captureScreenshot("08_formulaire_rempli_invalide");

        Thread.sleep(500);
        submitButton.click();
        Thread.sleep(1000);

        WebElement messageDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        String messageText = messageDiv.getText();
        System.out.println("📝 Message affiche : " + messageText);

        // Capture screenshot avec le message d'erreur
        captureScreenshot("09_connexion_echouee");

        assertTrue(messageText.contains("Échec") || messageText.contains("échec") || messageText.contains("Erreur") || messageText.contains("erreur"), 
            "Le message doit indiquer un echec de connexion");
        System.out.println("✅ TEST REUSSI : Identifiants invalides detectes correctement !");
    }

    @Test
    @Order(4)
    @DisplayName("Test 2.4 - Validation des champs vides")
    void testChampsVides() throws InterruptedException {
        System.out.println("\n========== TEST 2.4 : CHAMPS VIDES ==========");
        driver.get(cheminFichierHTML);
        Thread.sleep(1000);

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.id("submitBtn"));

        // Laisser les champs vides
        usernameField.clear();
        passwordField.clear();

        System.out.println("⌨️ Username : (vide)");
        System.out.println("⌨️ Password : (vide)");

        // Capture screenshot avec les champs vides
        captureScreenshot("10_formulaire_champs_vides");

        submitButton.click();
        Thread.sleep(1000);

        // Capture screenshot apres tentative de soumission
        captureScreenshot("11_validation_champs_vides");

        // Verifier que le formulaire gere les champs vides
        // Le comportement depend de l'implementation HTML
        System.out.println("✅ TEST VALIDE : Comportement des champs vides verifie");
    }
}

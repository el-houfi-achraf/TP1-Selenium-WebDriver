package com.example.exercices;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * EXERCICE 3 : Comparateur de prix entre Amazon et eBay
 */
public class Exercice3_ComparateurPrix {

    private WebDriver driver;
    private WebDriverWait wait;

    public void configurerDriver() {
        System.setProperty("webdriver.gecko.driver",
                "C:\\Users\\ACHRAF\\Downloads\\geckodriver-v0.36.0-win32\\geckodriver.exe");

        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("C:\\Program Files\\Firefox Developer Edition\\firefox.exe");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    public double rechercherSurAmazon(String nomProduit) {
        System.out.println("\n========== RECHERCHE SUR AMAZON ==========");

        try {
            driver.get("https://www.amazon.fr");
            Thread.sleep(3000);

            // Accepter les cookies
            try {
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(By.id("sp-cc-accept")));
                acceptCookies.click();
                System.out.println("Cookies acceptes");
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Pas de popup de cookies");
            }

            // Rechercher le produit
            System.out.println("Recherche du produit : " + nomProduit);
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("twotabsearchtextbox")));

            searchBox.clear();
            searchBox.sendKeys(nomProduit);
            searchBox.sendKeys(Keys.RETURN);

            Thread.sleep(3000);

            // Recuperer le premier prix
            WebElement prixElement = null;
            String prixTexte = "";

            try {
                // Essayer de trouver le prix avec a-price-whole
                prixElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".a-price-whole")));
                prixTexte = prixElement.getText();

                // Si le prix contient une virgule, on recupere aussi les decimales
                try {
                    WebElement prixDecimal = driver.findElement(By.cssSelector(".a-price-fraction"));
                    prixTexte = prixTexte + "," + prixDecimal.getText();
                } catch (Exception e) {
                    // Pas de decimales
                }

            } catch (Exception e1) {
                try {
                    // Alternative : essayer avec a-offscreen
                    prixElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".a-price .a-offscreen")));
                    prixTexte = prixElement.getAttribute("textContent");
                } catch (Exception e2) {
                    System.err.println("Impossible de trouver le prix sur Amazon");
                    return -1;
                }
            }

            System.out.println("Prix trouve sur Amazon : " + prixTexte);

            double prix = extrairePrix(prixTexte);
            System.out.println("Prix extrait : " + prix + " EUR");

            Thread.sleep(2000);
            return prix;

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche sur Amazon : " + e.getMessage());
            return -1;
        }
    }

    public double rechercherSurEbay(String nomProduit) {
        System.out.println("\n========== RECHERCHE SUR EBAY ==========");

        try {
            driver.get("https://www.ebay.fr");
            Thread.sleep(3000);

            // Accepter les cookies
            try {
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Tout accepter') or contains(text(), 'Accepter')]")
                ));
                acceptCookies.click();
                System.out.println("Cookies acceptes");
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Pas de popup de cookies");
            }

            // Rechercher le produit
            System.out.println("Recherche du produit : " + nomProduit);
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@type='text' and contains(@placeholder, 'Rechercher')]")
            ));

            searchBox.clear();
            searchBox.sendKeys(nomProduit);
            searchBox.sendKeys(Keys.RETURN);

            Thread.sleep(3000);

            // Recuperer le premier prix
            String prixTexte = "";

            try {
                // Attendre que les resultats se chargent avec un delai plus long
                Thread.sleep(2000);

                // Essayer plusieurs selecteurs possibles
                List<WebElement> prixElements = null;

                try {
                    // Premiere tentative : s-item__price standard
                    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".s-item__price")));
                    prixElements = driver.findElements(By.cssSelector(".s-item__price"));
                } catch (Exception e1) {
                    try {
                        // Deuxieme tentative : avec span
                        prixElements = driver.findElements(By.cssSelector("span.s-item__price"));
                    } catch (Exception e2) {
                        System.err.println("Impossible de trouver les elements de prix sur eBay");
                        return -1;
                    }
                }

                if (prixElements == null || prixElements.isEmpty()) {
                    System.err.println("Aucun prix trouve sur eBay");
                    return -1;
                }

                System.out.println("Nombre de prix trouves : " + prixElements.size());

                // Parcourir les elements pour trouver le premier prix valide
                for (int i = 0; i < prixElements.size(); i++) {
                    try {
                        WebElement element = prixElements.get(i);
                        String texte = element.getText();

                        System.out.println("Prix " + (i+1) + " : " + texte);

                        if (texte != null && !texte.trim().isEmpty() &&
                            !texte.toLowerCase().contains("à") &&
                            !texte.toLowerCase().contains("ou") &&
                            texte.contains("EUR")) {
                            prixTexte = texte;
                            break;
                        }
                    } catch (Exception e) {
                        // Element pas accessible, continuer
                    }
                }

                if (prixTexte.isEmpty()) {
                    System.err.println("Impossible de trouver un prix valide sur eBay");
                    return -1;
                }

            } catch (Exception e) {
                System.err.println("Erreur lors de l'extraction du prix sur eBay : " + e.getMessage());
                return -1;
            }

            System.out.println("Prix trouve sur eBay : " + prixTexte);

            double prix = extrairePrix(prixTexte);
            System.out.println("Prix extrait : " + prix + " EUR");

            Thread.sleep(2000);
            return prix;

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche sur eBay : " + e.getMessage());
            return -1;
        }
    }

    private double extrairePrix(String prixTexte) {
        try {
            String prixNettoye = prixTexte.replaceAll("[^0-9,.]", "").trim();

            if (prixNettoye.contains(",") && prixNettoye.contains(".")) {
                if (prixNettoye.lastIndexOf(",") > prixNettoye.lastIndexOf(".")) {
                    prixNettoye = prixNettoye.replace(".", "").replace(",", ".");
                } else {
                    prixNettoye = prixNettoye.replace(",", "");
                }
            } else if (prixNettoye.contains(",")) {
                prixNettoye = prixNettoye.replace(",", ".");
            }

            prixNettoye = prixNettoye.replace(" ", "");
            return Double.parseDouble(prixNettoye);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction du prix : " + prixTexte);
            return -1;
        }
    }

    public void comparerPrix(double prixAmazon, double prixEbay, String produit) {
        System.out.println("\n========== COMPARAISON DES PRIX ==========");
        System.out.println("Produit recherche : " + produit);
        System.out.println("------------------------------------------");

        if (prixAmazon > 0) {
            System.out.printf("Amazon.fr  : %.2f EUR%n", prixAmazon);
        } else {
            System.out.println("Amazon.fr  : Prix non trouve");
        }

        if (prixEbay > 0) {
            System.out.printf("eBay.fr    : %.2f EUR%n", prixEbay);
        } else {
            System.out.println("eBay.fr    : Prix non trouve");
        }

        System.out.println("------------------------------------------");

        if (prixAmazon > 0 && prixEbay > 0) {
            if (prixAmazon < prixEbay) {
                double economie = prixEbay - prixAmazon;
                System.out.printf("MEILLEUR PRIX : Amazon.fr (Economie de %.2f EUR)%n", economie);
            } else if (prixEbay < prixAmazon) {
                double economie = prixAmazon - prixEbay;
                System.out.printf("MEILLEUR PRIX : eBay.fr (Economie de %.2f EUR)%n", economie);
            } else {
                System.out.println("Les prix sont identiques sur les deux sites");
            }
        } else {
            System.out.println("Impossible de comparer : prix manquant(s)");
        }

        System.out.println("------------------------------------------\n");
    }

    public void fermerNavigateur() {
        if (driver != null) {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        Exercice3_ComparateurPrix comparateur = new Exercice3_ComparateurPrix();

        System.out.println("====================================================");
        System.out.println("       COMPARATEUR DE PRIX - SELENIUM WEBDRIVER    ");
        System.out.println("====================================================");

        try {
            comparateur.configurerDriver();

            String produit = "iPhone 13";

            double prixAmazon = comparateur.rechercherSurAmazon(produit);
            double prixEbay = comparateur.rechercherSurEbay(produit);

            comparateur.comparerPrix(prixAmazon, prixEbay, produit);

            System.out.println("Comparaison terminee avec succes !");

        } catch (Exception e) {
            System.err.println("Erreur generale : " + e.getMessage());
            e.printStackTrace();
        } finally {
            comparateur.fermerNavigateur();
        }
    }
}


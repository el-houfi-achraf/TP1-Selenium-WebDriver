package com.example.exercices;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EXERCICE 4 : Recherche d'entreprises pour PFE
 */
public class Exercice4_RechercheEntreprises {

    private WebDriver driver;
    private WebDriverWait wait;
    private List<Entreprise> entreprisesTrouvees;

    static class Entreprise {
        String nom;
        String adresse;
        String telephone;
        String activite;

        public Entreprise(String nom, String adresse, String telephone, String activite) {
            this.nom = nom;
            this.adresse = adresse;
            this.telephone = telephone;
            this.activite = activite;
        }

        @Override
        public String toString() {
            return String.format(
                "--------------------------------------\n" +
                "Entreprise : %s\n" +
                "Activite   : %s\n" +
                "Adresse    : %s\n" +
                "Telephone  : %s\n",
                nom, activite, adresse, telephone
            );
        }
    }

    public void configurerDriver() {
        System.setProperty("webdriver.gecko.driver",
            "C:\\Users\\ACHRAF\\Downloads\\geckodriver-v0.36.0-win32\\geckodriver.exe");

        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("C:\\Program Files\\Firefox Developer Edition\\firefox.exe");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        entreprisesTrouvees = new ArrayList<>();
    }

    public void rechercherSurPagesJaunes(String secteur, String ville) {
        System.out.println("\n========== RECHERCHE SUR PAGES JAUNES ==========");

        try {
            driver.get("https://www.pagesjaunes.fr");
            Thread.sleep(3000);

            // Accepter les cookies
            try {
                WebElement acceptCookies = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Accepter') or contains(text(), 'Tout accepter')]")
                ));
                acceptCookies.click();
                System.out.println("Cookies acceptes");
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Pas de popup de cookies");
            }

            // Remplir le formulaire de recherche
            System.out.println("Recherche : " + secteur + " a " + ville);

            WebElement champQuoi = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("quoi")));
            champQuoi.clear();
            champQuoi.sendKeys(secteur);
            Thread.sleep(500);

            WebElement champOu = driver.findElement(By.id("ou"));
            champOu.clear();
            champOu.sendKeys(ville);
            Thread.sleep(500);

            WebElement boutonRecherche = driver.findElement(
                By.xpath("//button[@type='submit' and contains(@class, 'pj-lb-button')]")
            );
            boutonRecherche.click();

            System.out.println("Chargement des resultats...");
            Thread.sleep(5000);

            // Extraire les resultats
            System.out.println("Extraction des entreprises...");
            extraireResultatsPagesJaunes(secteur);

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche sur PagesJaunes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void extraireResultatsPagesJaunes(String secteur) {
        try {
            List<WebElement> resultats = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("article.bi-bloc")
            ));

            System.out.println(resultats.size() + " resultats trouves");

            int compteur = 0;
            for (WebElement resultat : resultats) {
                if (compteur >= 10) break;

                try {
                    String nom = "Non renseigne";
                    try {
                        WebElement elementNom = resultat.findElement(By.cssSelector("h3.bi-denomination"));
                        nom = elementNom.getText().trim();
                    } catch (Exception e) {
                        try {
                            WebElement elementNom = resultat.findElement(By.cssSelector("a.denomination-links"));
                            nom = elementNom.getText().trim();
                        } catch (Exception ex) {
                        }
                    }

                    String adresse = "Non renseignee";
                    try {
                        WebElement elementAdresse = resultat.findElement(By.cssSelector("address"));
                        adresse = elementAdresse.getText().trim().replace("\n", ", ");
                    } catch (Exception e) {
                    }

                    String telephone = "Non renseigne";
                    try {
                        WebElement elementTel = resultat.findElement(By.cssSelector("a[data-pjlb='tel']"));
                        telephone = elementTel.getAttribute("href").replace("tel:", "");
                    } catch (Exception e) {
                    }

                    if (!nom.equals("Non renseigne")) {
                        Entreprise entreprise = new Entreprise(nom, adresse, telephone, secteur);
                        entreprisesTrouvees.add(entreprise);
                        compteur++;

                        System.out.println("Entreprise #" + compteur + " extraite : " + nom);
                    }

                } catch (Exception e) {
                    System.out.println("Erreur lors de l'extraction d'une entreprise : " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'extraction des resultats : " + e.getMessage());
        }
    }

    public void afficherResultats() {
        System.out.println("\n================================================");
        System.out.println("           RESULTATS DE LA RECHERCHE           ");
        System.out.println("================================================");

        if (entreprisesTrouvees.isEmpty()) {
            System.out.println("Aucune entreprise trouvee.");
            return;
        }

        System.out.println("\nNombre total d'entreprises : " + entreprisesTrouvees.size());
        System.out.println("--------------------------------------\n");

        for (int i = 0; i < entreprisesTrouvees.size(); i++) {
            System.out.println("Entreprise #" + (i + 1));
            System.out.println(entreprisesTrouvees.get(i));
        }
    }

    public void sauvegarderResultats(String secteur, String ville) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String nomFichier = "entreprises_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(nomFichier)) {
            writer.write("================================================\n");
            writer.write("    RECHERCHE D'ENTREPRISES POUR STAGE/PFE     \n");
            writer.write("================================================\n\n");

            writer.write("Date de recherche : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy a HH:mm")) + "\n");
            writer.write("Secteur          : " + secteur + "\n");
            writer.write("Localisation     : " + ville + "\n");
            writer.write("Resultats trouves : " + entreprisesTrouvees.size() + "\n\n");
            writer.write("----------------------------------------------\n\n");

            for (int i = 0; i < entreprisesTrouvees.size(); i++) {
                writer.write("ENTREPRISE #" + (i + 1) + "\n");
                writer.write(entreprisesTrouvees.get(i).toString());
                writer.write("\n");
            }

            writer.write("\n----------------------------------------------\n");
            writer.write("Conseil : Verifiez les sites web de ces entreprises\n");
            writer.write("pour connaitre leurs besoins en stagiaires.\n");

            System.out.println("\nResultats sauvegardes dans : " + nomFichier);

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    public void fermerNavigateur() {
        if (driver != null) {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        Exercice4_RechercheEntreprises recherche = new Exercice4_RechercheEntreprises();

        System.out.println("================================================");
        System.out.println("   RECHERCHE D'ENTREPRISES POUR STAGE/PFE      ");
        System.out.println("        Automatisation avec Selenium          ");
        System.out.println("================================================");

        try {
            recherche.configurerDriver();

            String secteur = "Developpement informatique";
            String ville = "Paris";

            System.out.println("\nSecteur recherche : " + secteur);
            System.out.println("Localisation     : " + ville);

            recherche.rechercherSurPagesJaunes(secteur, ville);
            recherche.afficherResultats();
            recherche.sauvegarderResultats(secteur, ville);

            System.out.println("\nRecherche terminee avec succes !");
            System.out.println("Consultez le fichier genere pour la liste complete.");

        } catch (Exception e) {
            System.err.println("Erreur generale : " + e.getMessage());
            e.printStackTrace();
        } finally {
            recherche.fermerNavigateur();
        }
    }
}


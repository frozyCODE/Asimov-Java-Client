package com.asimov.client.controllers;

import com.asimov.client.utils.SceneManager;
import com.asimov.client.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Contrôleur principal du tableau de bord (Dashboard).
 * <p>
 * Ce contrôleur est le cœur de l'application cliente. Il assure deux responsabilités majeures :
 * <ul>
 *   <li><b>Le Routage Interne :</b> Il bascule l'affichage entre les différentes vues (onglets) chargées via {@code <fx:include>}.</li>
 *   <li><b>La Sécurité Visuelle (RBAC) :</b> Il applique un filtrage strict de l'interface graphique selon le rôle
 *       (Élève, Professeur, Proviseur, Secrétariat) afin de prévenir toute action non autorisée avant même
 *       que le backend n'ait à intervenir (Prévention des erreurs 403).</li>
 * </ul>
 * </p>
 */
public class DashboardController {

    // =========================================================================
    // COMPOSANTS D'INTERFACE : EN-TÊTE ET MENUS
    // =========================================================================

    @FXML private VBox rootVBox;
    @FXML private Label welcomeLabel;

    // --- Boutons de navigation ---
    @FXML private Button monProfilNavButton;    // Utilisé si tu as un bouton pour le profil élève
    @FXML private Button elevesNavButton;
    @FXML private Button optionsNavButton;
    @FXML private Button moyennesNavButton;
    @FXML private Button classesNavButton;
    @FXML private Button professeursNavButton;
    @FXML private Button parentsNavButton;

    // =========================================================================
    // CONTENEURS DES ONGLETS (INJECTÉS VIA FX:INCLUDE)
    // =========================================================================

    /*
     * L'utilisation du type "Node" est impérative ici.
     * Certains de tes onglets (comme ClassesTab) utilisent un "SplitPane" à la racine,
     * tandis que d'autres utilisent une "VBox". "Node" est le parent commun qui
     * évite les exceptions de casting (IllegalArgumentException).
     */
    @FXML private Node eleveProfileTab;
    @FXML private Node elevesTab;
    @FXML private Node classesTab;
    @FXML private Node optionsTab;
    @FXML private Node moyennesTab;
    @FXML private Node professeursTab;
    @FXML private Node parentsTab;

    // =========================================================================
    // INITIALISATION ET ROUTAGE SÉCURISÉ
    // =========================================================================

    /**
     * S'exécute automatiquement après la création de l'interface graphique.
     * Récupère la session active et adapte l'environnement de travail de l'utilisateur.
     */
    @FXML
    public void initialize() {
        // 1. Personnalisation du message de bienvenue
        String prenom = UserSession.getInstance().getPrenom();
        String role = UserSession.getInstance().getRole();
        welcomeLabel.setText("Bienvenue, " + prenom + " (" + role + ")");

        // 2. Cloisonnement de l'interface en fonction du rôle
        appliquerDroitsAcces(role);
    }

    /**
     * Applique les règles de gestion des accès (RBAC) sur l'interface utilisateur.
     * Masque les boutons interdits et force l'affichage du premier onglet autorisé.
     *
     * @param role Le rôle extrait du token JWT (ex: "Eleve", "Professeur", "Proviseur").
     */
    private void appliquerDroitsAcces(String role) {

        if ("Eleve".equalsIgnoreCase(role)) {
            // L'élève est strictement confiné à son profil. Il ne doit voir aucune navigation administrative.
            cacherBouton(elevesNavButton);
            cacherBouton(optionsNavButton);
            cacherBouton(moyennesNavButton);
            cacherBouton(classesNavButton);
            cacherBouton(professeursNavButton);
            cacherBouton(parentsNavButton);

            // On le redirige instantanément vers son profil personnel
            showEleveProfileTab(null);
        }
        else if ("Professeur".equalsIgnoreCase(role)) {
            // Le professeur accède au volet pédagogique, mais pas à la structure de l'établissement
            cacherBouton(monProfilNavButton); // N'est pas un élève
            cacherBouton(classesNavButton);
            cacherBouton(professeursNavButton);
            cacherBouton(parentsNavButton);
            cacherBouton(optionsNavButton);

            // Redirection par défaut vers la liste de ses élèves
            showElevesTab(null);
        }
        else if ("Proviseur".equalsIgnoreCase(role) || "Secretariat".equalsIgnoreCase(role)) {
            // L'administration a accès à tous les modules fonctionnels (sauf l'espace personnel élève)
            cacherBouton(monProfilNavButton);

            if (classesNavButton != null) {
                classesNavButton.setVisible(true);
                classesNavButton.setManaged(true);
            }

            // Redirection par défaut vers la gestion des classes
            showClassesTab(null);
        }
    }

    /**
     * Utilitaire permettant de masquer complètement un bouton de l'interface graphique.
     * En définissant {@code managed} à false, JavaFX recalcule la mise en page
     * pour absorber l'espace vide laissé par le bouton masqué.
     *
     * @param bouton Le bouton JavaFX à cacher.
     */
    private void cacherBouton(Button bouton) {
        if (bouton != null) {
            bouton.setVisible(false);
            bouton.setManaged(false);
        }
    }

    /**
     * Rend tous les onglets du tableau de bord invisibles.
     * Doit obligatoirement être invoqué avant de rendre un nouvel onglet visible
     * pour éviter les superpositions graphiques.
     */
    private void cacherTousLesOnglets() {
        if (eleveProfileTab != null) { eleveProfileTab.setVisible(false); eleveProfileTab.setManaged(false); }
        if (elevesTab != null) { elevesTab.setVisible(false); elevesTab.setManaged(false); }
        if (classesTab != null) { classesTab.setVisible(false); classesTab.setManaged(false); }
        if (optionsTab != null) { optionsTab.setVisible(false); optionsTab.setManaged(false); }
        if (moyennesTab != null) { moyennesTab.setVisible(false); moyennesTab.setManaged(false); }
        if (professeursTab != null) { professeursTab.setVisible(false); professeursTab.setManaged(false); }
        if (parentsTab != null) { parentsTab.setVisible(false); parentsTab.setManaged(false); }
    }

    // =========================================================================
    // MÉTHODES DE NAVIGATION (ÉVÉNEMENTS DES BOUTONS)
    // =========================================================================

    @FXML
    void showEleveProfileTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (eleveProfileTab != null) { eleveProfileTab.setVisible(true); eleveProfileTab.setManaged(true); }
    }

    @FXML
    void showElevesTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (elevesTab != null) { elevesTab.setVisible(true); elevesTab.setManaged(true); }
    }

    @FXML
    void showClassesTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (classesTab != null) { classesTab.setVisible(true); classesTab.setManaged(true); }
    }

    @FXML
    void showOptionsTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (optionsTab != null) { optionsTab.setVisible(true); optionsTab.setManaged(true); }
    }

    @FXML
    void showMoyennesTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (moyennesTab != null) { moyennesTab.setVisible(true); moyennesTab.setManaged(true); }
    }

    @FXML
    void showProfesseursTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (professeursTab != null) { professeursTab.setVisible(true); professeursTab.setManaged(true); }
    }

    @FXML
    void showParentsTab(ActionEvent event) {
        cacherTousLesOnglets();
        if (parentsTab != null) { parentsTab.setVisible(true); parentsTab.setManaged(true); }
    }

    // =========================================================================
    // GESTION DE LA SESSION
    // =========================================================================

    /**
     * Déconnecte l'utilisateur actif.
     * Purge le singleton {@link UserSession} et redirige l'application
     * vers la page de connexion initiale.
     *
     * @param event L'événement d'action généré par le clic.
     */
    @FXML
    void handleLogout(ActionEvent event) {
        UserSession.getInstance().logout();
        try {
            SceneManager.switchToLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
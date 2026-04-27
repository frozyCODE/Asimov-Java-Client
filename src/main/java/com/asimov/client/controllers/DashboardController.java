package com.asimov.client.controllers;

import com.asimov.client.utils.SceneManager;
import com.asimov.client.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Orchestrateur principal de l'interface utilisateur.
 * Gère la navigation entre les modules et adapte l'affichage selon les droits d'accès (RBAC).
 */
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button elevesNavButton, optionsNavButton, moyennesNavButton, classesNavButton;

    @FXML private Node elevesTab, eleveProfileTab, classesTab, optionsTab, moyennesTab;

    @FXML private ElevesTabController elevesTabController;
    @FXML private EleveProfileTabController eleveProfileTabController;
    @FXML private ClassesTabController classesTabController;
    @FXML private OptionsTabController optionsTabController;
    @FXML private MoyennesTabController moyennesTabController;

    /**
     * Initialise la session, configure les accès et définit l'onglet par défaut au lancement.
     */
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        welcomeLabel.setText("Session : " + session.getPrenom() + " " + session.getNom());

        configurerAcces(session);
        showElevesTab();
    }

    /**
     * Restreint les fonctionnalités administratives pour le rôle Élève.
     *
     * @param session Session de l'utilisateur connecté.
     */
    private void configurerAcces(UserSession session) {
        boolean estEleve = session.hasRole("Eleve");

        if (estEleve) {
            elevesNavButton.setText("Mon Profil");
        }

        classesNavButton.setVisible(!estEleve);
        classesNavButton.setManaged(!estEleve);

        optionsNavButton.setVisible(!estEleve);
        optionsNavButton.setManaged(!estEleve);

        moyennesNavButton.setVisible(!estEleve);
        moyennesNavButton.setManaged(!estEleve);
    }

    /**
     * Oriente vers le profil personnel ou la gestion globale des élèves selon le rôle.
     */
    @FXML
    private void showElevesTab() {
        UserSession session = UserSession.getInstance();

        if (session.hasRole("Eleve")) {
            switchTab(eleveProfileTab, elevesNavButton);
            if (eleveProfileTabController != null) {
                eleveProfileTabController.loadData();
            }
        } else {
            switchTab(elevesTab, elevesNavButton);
            if (elevesTabController != null) {
                elevesTabController.loadData();
            }
        }
    }

    /**
     * Affiche l'onglet de gestion des classes.
     */
    @FXML
    private void showClassesTab() {
        switchTab(classesTab, classesNavButton);
        if (classesTabController != null) {
            classesTabController.loadData();
        }
    }

    /**
     * Affiche l'onglet de gestion des options.
     */
    @FXML
    private void showOptionsTab() {
        switchTab(optionsTab, optionsNavButton);
        if (optionsTabController != null) {
            optionsTabController.loadData();
        }
    }

    /**
     * Affiche l'onglet de gestion des moyennes.
     */
    @FXML
    private void showMoyennesTab() {
        switchTab(moyennesTab, moyennesNavButton);
        if (moyennesTabController != null) {
            moyennesTabController.loadData();
        }
    }

    /**
     * Permute visuellement les modules de l'application en masquant les vues inactives.
     *
     * @param activeTab Le composant FXML à afficher.
     * @param activeBtn Le bouton de navigation à mettre en surbrillance.
     */
    private void switchTab(Node activeTab, Button activeBtn) {
        Node[] tabs = {elevesTab, eleveProfileTab, classesTab, optionsTab, moyennesTab};
        Button[] btns = {elevesNavButton, classesNavButton, optionsNavButton, moyennesNavButton};

        for (Node tab : tabs) {
            if (tab != null) {
                tab.setVisible(false);
                tab.setManaged(false);
            }
        }

        for (Button btn : btns) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-button-active");
            }
        }

        if (activeTab != null) {
            activeTab.setVisible(true);
            activeTab.setManaged(true);
        }

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }

    /**
     * Termine la session de l'utilisateur et redirige vers la page de connexion.
     */
    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            SceneManager.switchToLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
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
 * Gère la navigation et l'affichage conditionnel selon les rôles.
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button elevesNavButton, optionsNavButton, moyennesNavButton, classesNavButton, parentsNavButton;
    @FXML
    private Node elevesTab, eleveProfileTab, classesTab, optionsTab, moyennesTab, parentsTab, mesEnfantsTab;

    // Injection automatique des contrôleurs des onglets inclus (doit correspondre à
    // fx:id + "Controller")
    @FXML
    private ElevesTabController elevesTabController;
    @FXML
    private EleveProfileTabController eleveProfileTabController;
    @FXML
    private ClassesTabController classesTabController;
    @FXML
    private OptionsTabController optionsTabController;
    @FXML
    private MoyennesTabController moyennesTabController;
    @FXML
    private ParentsTabController parentsTabController;
    @FXML
    private MesEnfantsController mesEnfantsTabController; // Correction du nom ici

    /**
     * Initialise le dashboard et sélectionne la vue par défaut selon le rôle.
     */
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        welcomeLabel
                .setText("Session : " + session.getPrenom() + " " + session.getNom() + " (" + session.getRole() + ")");
        configurerAcces(session);

        // Sélection de l'onglet initial sécurisée
        if (session.hasRole("Parent")) {
            showMesEnfantsTab();
        } else if (session.hasRole("Eleve")) {
            showElevesTab();
        } else {
            showElevesTab();
        }
    }

    /**
     * Masque les boutons de navigation non autorisés.
     */
    private void configurerAcces(UserSession session) {
        boolean estAdmin = session.hasRole("Secretariat") || session.hasRole("Proviseur");
        boolean estProf = session.hasRole("Professeur");
        boolean estParent = session.hasRole("Parent");

        parentsNavButton.setVisible(estAdmin);
        parentsNavButton.setManaged(estAdmin);

        optionsNavButton.setVisible(estAdmin);
        optionsNavButton.setManaged(estAdmin);

        classesNavButton.setVisible(estAdmin || estProf);
        classesNavButton.setManaged(estAdmin || estProf);

        // Les moyennes sont réservées aux admins et profs
        moyennesNavButton.setVisible(estAdmin || estProf);
        moyennesNavButton.setManaged(estAdmin || estProf);

        if (session.hasRole("Eleve"))
            elevesNavButton.setText("Mon Profil");
        if (estParent)
            elevesNavButton.setText("Mes Enfants");
    }

    @FXML
    private void showElevesTab() {
        UserSession s = UserSession.getInstance();
        if (s.hasRole("Eleve")) {
            switchTab(eleveProfileTab, elevesNavButton);
            if (eleveProfileTabController != null)
                eleveProfileTabController.loadData();
        } else if (s.hasRole("Parent")) {
            showMesEnfantsTab();
        } else {
            switchTab(elevesTab, elevesNavButton);
            if (elevesTabController != null)
                elevesTabController.loadData();
        }
    }

    @FXML
    private void showParentsTab() {
        if (!UserSession.getInstance().hasRole("Secretariat") && !UserSession.getInstance().hasRole("Proviseur"))
            return;
        switchTab(parentsTab, parentsNavButton);
        if (parentsTabController != null)
            parentsTabController.loadParents();
    }

    @FXML
    private void showMesEnfantsTab() {
        switchTab(mesEnfantsTab, elevesNavButton);
        if (mesEnfantsTabController != null) {
            mesEnfantsTabController.loadData();
        } else {
            System.err.println("[ERREUR] mesEnfantsTabController est null !");
        }
    }

    @FXML
    private void showClassesTab() {
        if (UserSession.getInstance().hasRole("Parent") || UserSession.getInstance().hasRole("Eleve"))
            return;
        switchTab(classesTab, classesNavButton);
        if (classesTabController != null)
            classesTabController.loadData();
    }

    @FXML
    private void showOptionsTab() {
        if (!UserSession.getInstance().hasRole("Secretariat") && !UserSession.getInstance().hasRole("Proviseur"))
            return;
        switchTab(optionsTab, optionsNavButton);
        if (optionsTabController != null)
            optionsTabController.loadData();
    }

    @FXML
    private void showMoyennesTab() {
        if (UserSession.getInstance().hasRole("Parent") || UserSession.getInstance().hasRole("Eleve"))
            return;
        switchTab(moyennesTab, moyennesNavButton);
        if (moyennesTabController != null)
            moyennesTabController.loadData();
    }

    /**
     * Gère la visibilité des nœuds pour simuler un système d'onglets.
     */
    private void switchTab(Node tab, Button btn) {
        Node[] tabs = { elevesTab, eleveProfileTab, classesTab, optionsTab, moyennesTab, parentsTab, mesEnfantsTab };
        Button[] btns = { elevesNavButton, classesNavButton, optionsNavButton, moyennesNavButton, parentsNavButton };

        for (Node t : tabs)
            if (t != null) {
                t.setVisible(false);
                t.setManaged(false);
            }
        for (Button b : btns)
            if (b != null)
                b.getStyleClass().remove("nav-button-active");

        if (tab != null) {
            tab.setVisible(true);
            tab.setManaged(true);
        }
        if (btn != null)
            btn.getStyleClass().add("nav-button-active");
    }

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

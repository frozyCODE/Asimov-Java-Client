package com.asimov.client.utils;

import com.asimov.client.controllers.AddEditEleveController;
import com.asimov.client.models.Eleve;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Gestionnaire centralisé des scènes JavaFX de l'application.
 * Contrôle la navigation et l'affichage des fenêtres principales et modales.
 */
public class SceneManager {
    private static Stage primaryStage;

    private static final double MIN_WIDTH = 900;
    private static final double MIN_HEIGHT = 700;

    /**
     * Définit la fenêtre principale de l'application et ses dimensions minimales.
     *
     * @param stage L'objet Stage principal fourni par JavaFX.
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
    }

    /**
     * Affiche l'écran de connexion.
     *
     * @throws IOException Si le fichier FXML de la vue est introuvable.
     */
    public static void switchToLogin() throws IOException {
        URL resource = SceneManager.class.getResource("/com/asimov/client/views/Login.fxml");
        if (resource == null) {
            throw new RuntimeException("Fichier FXML Login introuvable.");
        }
        Parent root = FXMLLoader.load(resource);

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Asimov - Connexion");

        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(450);
        primaryStage.setWidth(400);
        primaryStage.setHeight(450);

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Méthode utilitaire pour changer la scène de la fenêtre principale.
     *
     * @param fxmlPath Le chemin vers le fichier FXML cible.
     * @param title    Le titre de la fenêtre.
     * @throws IOException Si le fichier FXML cible est introuvable.
     */
    private static void switchScene(String fxmlPath, String title) throws IOException {
        URL resource = SceneManager.class.getResource(fxmlPath);
        if (resource == null) {
            throw new RuntimeException("Fichier FXML " + fxmlPath + " introuvable.");
        }
        Parent root = FXMLLoader.load(resource);

        Scene currentScene = primaryStage.getScene();

        if (currentScene == null || currentScene.getWidth() < MIN_WIDTH) {
            Scene newScene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
            primaryStage.setScene(newScene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            primaryStage.setWidth(MIN_WIDTH);
            primaryStage.setHeight(MIN_HEIGHT);
            primaryStage.centerOnScreen();
        } else {
            currentScene.setRoot(root);
        }

        primaryStage.setTitle(title);

        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    /**
     * Affiche le tableau de bord principal de l'application.
     *
     * @throws IOException Si le fichier FXML du Dashboard est introuvable.
     */
    public static void switchToDashboard() throws IOException {
        switchScene("/com/asimov/client/views/Dashboard.fxml", "Asimov - Accueil");
    }

    /**
     * Affiche une fenêtre modale pour la création ou la modification d'un élève.
     * Bloque l'exécution de la fenêtre parente jusqu'à sa fermeture.
     *
     * @param eleve L'objet Eleve à éditer ou un objet vierge pour une création.
     * @return Le contrôleur de la modale permettant d'accéder aux données saisies, ou null en cas d'erreur.
     */
    public static AddEditEleveController showEleveEditDialog(Eleve eleve) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL resource = SceneManager.class.getResource("/com/asimov/client/views/AddEditEleveView.fxml");
            if (resource == null) {
                throw new RuntimeException("Fichier FXML AddEditEleveView introuvable.");
            }
            loader.setLocation(resource);
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(eleve.getId() == 0 ? "Ajouter un Élève" : "Modifier un Élève");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            Scene scene = new Scene(page);
            URL cssUrl = SceneManager.class.getResource("/com/asimov/client/css/styles.css");
            if(cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            AddEditEleveController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setEleve(eleve);

            dialogStage.showAndWait();

            return controller;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.asimov.client.controllers;

import com.asimov.client.services.AuthService;
import com.asimov.client.utils.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur de la page de connexion.
 */
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField emailInput;
    @FXML private PasswordField passwordInput;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    private void handleLoginAction() {
        String email = emailInput.getText();
        String password = passwordInput.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        errorLabel.setVisible(false);
        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours...");

        AuthService.loginAsync(email, password)
                .thenAcceptAsync(token -> {
                    if (token != null) {
                        try {
                            // LA CORRECTION EST ICI :
                            // Tout le monde va vers le Dashboard. C'est le Dashboard qui fera le tri (Profil ou Tableau).
                            SceneManager.switchToDashboard();
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Erreur lors du changement de scène", e);
                            showError("Erreur d'interface.");
                        }
                    } else {
                        showError("Identifiants incorrects.");
                    }
                }, Platform::runLater)
                .exceptionally(ex -> {
                    LOGGER.log(Level.WARNING, "Erreur de connexion", ex);
                    Platform.runLater(() -> showError("Erreur de connexion au serveur."));
                    return null;
                })
                .whenCompleteAsync((res, ex) -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter");
                }, Platform::runLater);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
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
 * Contrôleur de la vue d'authentification (Login).
 * <p>
 * Gère la saisie des identifiants, la validation de surface (champs vides, espaces accidentels),
 * la communication asynchrone avec l'API Node.js et la redirection vers l'espace de travail.
 * </p>
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField emailInput;
    @FXML private PasswordField passwordInput;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    /**
     * Déclenché lors de la validation du formulaire de connexion.
     * Applique un nettoyage des données saisies (trim) pour prévenir les erreurs liées aux copier-coller,
     * puis initie le processus d'authentification asynchrone.
     */
    @FXML
    private void handleLoginAction() {
        /* Nettoyage strict des espaces invisibles (ex: fin de chaîne lors d'un copier-coller) */
        String email = emailInput.getText().trim();
        String password = passwordInput.getText().trim();

        LOGGER.info("Tentative de connexion en cours pour : [" + email + "]");

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
                            /* Redirection vers le contrôleur principal qui dispatche les vues selon le rôle */
                            SceneManager.switchToDashboard();
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Échec critique du chargement de la scène Dashboard", e);
                            showError("Erreur d'interface système.");
                        }
                    } else {
                        showError("Identifiants incorrects.");
                    }
                }, Platform::runLater)
                .exceptionally(ex -> {
                    LOGGER.log(Level.WARNING, "Rejet de la connexion par le serveur distant", ex);
                    Platform.runLater(() -> showError("Erreur de connexion au serveur."));
                    return null;
                })
                .whenCompleteAsync((res, ex) -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter");
                }, Platform::runLater);
    }

    /**
     * Affiche un message d'erreur formaté sur l'interface utilisateur.
     *
     * @param message Le texte explicatif de l'erreur à afficher.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
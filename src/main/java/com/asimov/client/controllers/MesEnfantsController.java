package com.asimov.client.controllers;

import com.asimov.client.models.Eleve;
import com.asimov.client.models.Parent;
import com.asimov.client.services.ParentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * Contrôleur gérant l'affichage des enfants pour un utilisateur de rôle Parent.
 * <p>
 * Récupère dynamiquement les informations du parent puis liste ses enfants
 * sous forme de cartes visuelles dans l'interface.
 * </p>
 */
public class MesEnfantsController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label statusLabel;

    /**
     * Charge les données du parent et de ses enfants dès l'affichage de l'onglet.
     */
    public void loadData() {
        cardsContainer.getChildren().clear();
        statusLabel.setText("Chargement de vos enfants...");

        ParentService.recupererMonProfilAsync()
                .thenCompose(parent -> ParentService.recupererMesEnfantsAsync(parent.getId()))
                .thenAccept(this::afficherEnfants)
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Erreur : " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Génère dynamiquement les composants UI pour chaque enfant.
     * 
     * @param enfants Liste des élèves associés au parent.
     */
    private void afficherEnfants(List<Eleve> enfants) {
        Platform.runLater(() -> {
            if (enfants.isEmpty()) {
                statusLabel.setText("Aucun enfant n'est rattaché à votre compte.");
                return;
            }

            statusLabel.setText(enfants.size() + " enfant(s) trouvé(s) :");
            for (Eleve enfant : enfants) {
                VBox card = new VBox(12);
                card.getStyleClass().add("eleve-card");
                card.setPrefSize(250, 160);

                Label nameLabel = new Label(enfant.getPrenom() + " " + enfant.getNom());
                nameLabel.getStyleClass().add("eleve-card-name");

                Label mailLabel = new Label(enfant.getEmail());
                mailLabel.getStyleClass().add("eleve-card-info");

                Label idLabel = new Label("ID Elève : " + enfant.getIdentifiant_csv());
                idLabel.getStyleClass().add("eleve-card-info");

                card.getChildren().addAll(nameLabel, mailLabel, idLabel);
                cardsContainer.getChildren().add(card);
            }
        });
    }
}

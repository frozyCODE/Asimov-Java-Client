package com.asimov.client.controllers;

import com.asimov.client.exceptions.ApiAuthException;
import com.asimov.client.models.Eleve;
import com.asimov.client.models.Option;
import com.asimov.client.services.EleveService;
import com.asimov.client.services.OptionService;
import com.asimov.client.utils.SceneManager;
import com.asimov.client.utils.UserSession;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contrôleur principal de l'application Asimov.
 * Gère le routage interne de la vue principale (Dashboard) en fonction du rôle de l'utilisateur connecté.
 * Supervise les opérations CRUD pour l'annuaire des élèves et le catalogue des options.
 */
public class DashboardController {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private Label welcomeLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Button elevesNavButton;
    @FXML private Button optionsNavButton;

    @FXML private HBox headerBox;
    @FXML private TextField searchField;

    // --- Composants de l'onglet : Gestion des Élèves ---
    @FXML private VBox gestionElevesBox;
    @FXML private TableView<Eleve> elevesTable;
    @FXML private TableColumn<Eleve, Integer> idColumn;
    @FXML private TableColumn<Eleve, String> nomColumn;
    @FXML private TableColumn<Eleve, String> prenomColumn;
    @FXML private TableColumn<Eleve, String> emailColumn;
    @FXML private Button addEleveButton;
    @FXML private Button editEleveButton;
    @FXML private Button deleteEleveButton;

    // --- Composants de l'onglet : Gestion des Options ---
    @FXML private VBox gestionOptionsBox;
    @FXML private TableView<Option> optionsTable;
    @FXML private TableColumn<Option, Integer> optionIdColumn;
    @FXML private TableColumn<Option, String> optionNomColumn;
    @FXML private TextField newOptionField;
    @FXML private Button deleteOptionButton;

    // --- Composants de la vue : Profil Élève ---
    @FXML private VBox profilEleveBox;
    @FXML private Label profilNomLabel;
    @FXML private Label profilEmailLabel;
    @FXML private Label profilOptionsLabel;

    private ObservableList<Eleve> masterEleveData = FXCollections.observableArrayList();
    private ObservableList<Option> masterOptionData = FXCollections.observableArrayList();
    private FilteredList<Eleve> filteredEleveData;

    /**
     * Méthode d'initialisation appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * Configure le message de bienvenue et applique le contrôle d'accès basé sur les rôles (RBAC).
     */
    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            welcomeLabel.setText("Bienvenue, " + session.getPrenom() + " " + session.getNom() + " (" + session.getRole() + ")");
        }

        applyRoleBasedAccessControl(session);
    }

    /**
     * Adapte l'interface utilisateur en fonction du rôle de la session active.
     * Restreint l'accès aux fonctionnalités d'administration pour les élèves.
     *
     * @param session L'instance de la session utilisateur en cours.
     */
    private void applyRoleBasedAccessControl(UserSession session) {
        String role = session.getRole();
        boolean isEleve = "Eleve".equalsIgnoreCase(role);

        if (isEleve) {
            elevesNavButton.setText("Mon Profil");
            optionsNavButton.setVisible(false);
            optionsNavButton.setManaged(false);

            pageTitleLabel.setText("Espace Personnel");
            searchField.setVisible(false);

            gestionElevesBox.setVisible(false);
            gestionElevesBox.setManaged(false);
            gestionOptionsBox.setVisible(false);
            gestionOptionsBox.setManaged(false);

            profilEleveBox.setVisible(true);
            profilEleveBox.setManaged(true);

            profilNomLabel.setText(session.getPrenom() + " " + session.getNom());
            profilEmailLabel.setText(session.getEmail());

            chargerOptionsEleve(session.getId());
        } else {
            setupElevesTable();
            setupOptionsTable();
            showElevesTab();
        }
    }

    // ==========================================
    //   NAVIGATION INTERNE
    // ==========================================

    /**
     * Affiche l'onglet de gestion de l'annuaire des élèves et masque les autres vues.
     * Met à jour le style de la barre de navigation.
     */
    @FXML
    private void showElevesTab() {
        pageTitleLabel.setText("Gestion des Élèves");
        searchField.setVisible(true);

        elevesNavButton.getStyleClass().add("nav-button-active");
        optionsNavButton.getStyleClass().remove("nav-button-active");

        gestionOptionsBox.setVisible(false);
        gestionOptionsBox.setManaged(false);
        gestionElevesBox.setVisible(true);
        gestionElevesBox.setManaged(true);

        loadElevesData();
    }

    /**
     * Affiche l'onglet de gestion du catalogue d'options et masque les autres vues.
     * Met à jour le style de la barre de navigation.
     */
    @FXML
    private void showOptionsTab() {
        pageTitleLabel.setText("Catalogue des Options");
        searchField.setVisible(false);

        optionsNavButton.getStyleClass().add("nav-button-active");
        elevesNavButton.getStyleClass().remove("nav-button-active");

        gestionElevesBox.setVisible(false);
        gestionElevesBox.setManaged(false);
        gestionOptionsBox.setVisible(true);
        gestionOptionsBox.setManaged(true);

        loadOptionsData();
    }

    // ==========================================
    //   GESTION DES ÉLÈVES (ADMINISTRATION)
    // ==========================================

    /**
     * Configure le TableView des élèves, lie les colonnes aux propriétés du modèle
     * et initialise le système de filtrage en temps réel via la barre de recherche.
     */
    private void setupElevesTable() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nomColumn.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        prenomColumn.setCellValueFactory(cellData -> cellData.getValue().prenomProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        filteredEleveData = new FilteredList<>(masterEleveData, p -> true);
        elevesTable.setItems(filteredEleveData);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEleveData.setPredicate(eleve -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return eleve.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        eleve.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                        eleve.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });

        elevesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editEleveButton.setDisable(newSelection == null);
            deleteEleveButton.setDisable(newSelection == null);
        });

        elevesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !editEleveButton.isDisabled()) handleEditEleve();
        });
    }

    /**
     * Lance une requête asynchrone pour récupérer la liste des élèves depuis l'API
     * et met à jour l'interface utilisateur.
     */
    private void loadElevesData() {
        elevesTable.setPlaceholder(new ProgressIndicator());
        masterEleveData.clear();

        EleveService.recupererTousAsync()
                .thenAcceptAsync(eleves -> {
                    masterEleveData.addAll(eleves);
                    elevesTable.setPlaceholder(new Label("Aucun élève trouvé."));
                }, Platform::runLater)
                .exceptionally(ex -> {
                    handleApiException(ex);
                    return null;
                });
    }

    /**
     * Ouvre la modale de création d'un nouvel élève.
     * Orchestre l'appel séquentiel de l'API pour la création du profil puis l'affectation des options.
     */
    @FXML
    private void handleAddEleve() {
        Eleve newEleve = new Eleve();
        AddEditEleveController controller = SceneManager.showEleveEditDialog(newEleve);

        if (controller != null && controller.isOkClicked()) {
            addEleveButton.setDisable(true);
            EleveService.createEleveAsync(newEleve)
                    .thenCompose(eleve -> {
                        List<Integer> nouveauxIds = controller.getSelectedOptionIds();
                        CompletableFuture<Void> optionChain = CompletableFuture.completedFuture(null);
                        for (Integer id : nouveauxIds) {
                            optionChain = optionChain.thenCompose(v -> OptionService.choisirOptionAsync(eleve.getId(), id));
                        }
                        return optionChain;
                    })
                    .thenAcceptAsync(v -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Succès", "Élève ajouté avec succès !");
                            loadElevesData();
                        });
                    })
                    .exceptionally(ex -> {
                        handleApiException(ex);
                        return null;
                    })
                    .whenCompleteAsync((res, ex) -> Platform.runLater(() -> addEleveButton.setDisable(false)));
        }
    }

    /**
     * Ouvre la modale d'édition pour l'élève sélectionné.
     * Calcule le delta des options (à supprimer / à ajouter) pour optimiser les appels API.
     */
    @FXML
    private void handleEditEleve() {
        Eleve selectedEleve = elevesTable.getSelectionModel().getSelectedItem();
        if (selectedEleve != null) {
            AddEditEleveController controller = SceneManager.showEleveEditDialog(selectedEleve);

            if (controller != null && controller.isOkClicked()) {
                editEleveButton.setDisable(true);
                EleveService.updateEleveAsync(selectedEleve)
                        .thenCompose(eleve -> {
                            List<Integer> nouveauxIds = controller.getSelectedOptionIds();
                            List<Integer> anciensIds = controller.getOptionsInitiales().stream()
                                    .map(Option::getId).collect(Collectors.toList());

                            List<Integer> aSupprimer = new ArrayList<>(anciensIds);
                            aSupprimer.removeAll(nouveauxIds);
                            List<Integer> aAjouter = new ArrayList<>(nouveauxIds);
                            aAjouter.removeAll(anciensIds);

                            CompletableFuture<Void> optionChain = CompletableFuture.completedFuture(null);
                            for (Integer id : aSupprimer) optionChain = optionChain.thenCompose(v -> OptionService.desisterOptionAsync(selectedEleve.getId(), id));
                            for (Integer id : aAjouter) optionChain = optionChain.thenCompose(v -> OptionService.choisirOptionAsync(selectedEleve.getId(), id));

                            return optionChain;
                        })
                        .thenAcceptAsync(v -> {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Élève et options modifiés avec succès !");
                                loadElevesData();
                            });
                        })
                        .exceptionally(ex -> {
                            handleApiException(ex);
                            return null;
                        })
                        .whenCompleteAsync((res, ex) -> Platform.runLater(() -> editEleveButton.setDisable(false)));
            }
        }
    }

    /**
     * Gère la suppression d'un élève après confirmation de l'utilisateur.
     */
    @FXML
    private void handleDeleteEleve() {
        Eleve selectedEleve = elevesTable.getSelectionModel().getSelectedItem();
        if (selectedEleve != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Suppression");
            alert.setHeaderText("Supprimer " + selectedEleve.getPrenom() + " " + selectedEleve.getNom() + " ?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteEleveButton.setDisable(true);
                EleveService.deleteEleveAsync(selectedEleve.getId())
                        .thenAcceptAsync(v -> {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Succès", "Élève supprimé !");
                                loadElevesData();
                            });
                        })
                        .exceptionally(ex -> {
                            handleApiException(ex);
                            return null;
                        })
                        .whenCompleteAsync((res, ex) -> Platform.runLater(() -> deleteEleveButton.setDisable(false)));
            }
        }
    }

    // ==========================================
    //   GESTION DU CATALOGUE D'OPTIONS
    // ==========================================

    /**
     * Configure le TableView des options du catalogue.
     */
    private void setupOptionsTable() {
        optionIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        optionNomColumn.setCellValueFactory(cellData -> cellData.getValue().nomProperty());

        optionsTable.setItems(masterOptionData);

        optionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteOptionButton.setDisable(newSelection == null);
        });
    }

    /**
     * Charge la liste globale des options depuis l'API.
     */
    private void loadOptionsData() {
        optionsTable.setPlaceholder(new ProgressIndicator());
        masterOptionData.clear();

        OptionService.getAllOptionsAsync()
                .thenAcceptAsync(options -> {
                    masterOptionData.addAll(options);
                    optionsTable.setPlaceholder(new Label("Aucune option dans le catalogue."));
                }, Platform::runLater)
                .exceptionally(ex -> {
                    handleApiException(ex);
                    return null;
                });
    }

    /**
     * Crée une nouvelle option d'enseignement dans la base de données.
     */
    @FXML
    private void handleAddOption() {
        String nom = newOptionField.getText();
        if (nom != null && !nom.trim().isEmpty()) {
            OptionService.createOptionAsync(nom.trim())
                    .thenAcceptAsync(v -> {
                        Platform.runLater(() -> {
                            newOptionField.clear();
                            loadOptionsData();
                        });
                    })
                    .exceptionally(ex -> {
                        handleApiException(ex);
                        return null;
                    });
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le nom de l'option ne peut pas être vide.");
        }
    }

    /**
     * Supprime définitivement une option du système après confirmation.
     * Avertit l'utilisateur des conséquences en cascade.
     */
    @FXML
    private void handleDeleteOption() {
        Option selectedOption = optionsTable.getSelectionModel().getSelectedItem();
        if (selectedOption != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Suppression");
            alert.setHeaderText("Supprimer l'option : " + selectedOption.getNom() + " ?");
            alert.setContentText("Attention : cela retirera cette option à tous les élèves inscrits.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteOptionButton.setDisable(true);
                OptionService.deleteOptionAsync(selectedOption.getId())
                        .thenAcceptAsync(v -> {
                            Platform.runLater(() -> {
                                loadOptionsData();
                            });
                        })
                        .exceptionally(ex -> {
                            handleApiException(ex);
                            return null;
                        })
                        .whenCompleteAsync((res, ex) -> Platform.runLater(() -> deleteOptionButton.setDisable(false)));
            }
        }
    }

    // ==========================================
    //   PROFIL ÉLÈVE & FONCTIONS UTILITAIRES
    // ==========================================

    /**
     * Récupère de manière asynchrone les options liées à l'élève connecté
     * et formate l'affichage dans l'interface de profil.
     *
     * @param eleveId L'identifiant de l'élève connecté.
     */
    private void chargerOptionsEleve(int eleveId) {
        profilOptionsLabel.setText("Recherche de vos options...");
        OptionService.getOptionsByEleveAsync(eleveId)
                .thenAcceptAsync(options -> {
                    Platform.runLater(() -> {
                        if (options == null || options.isEmpty()) {
                            profilOptionsLabel.setText("Vous n'êtes inscrit à aucune option.");
                            profilOptionsLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-style: italic;");
                        } else {
                            StringBuilder texteOptions = new StringBuilder();
                            for (int i = 0; i < options.size(); i++) {
                                texteOptions.append("• ").append(options.get(i).getNom());
                                if (i < options.size() - 1) texteOptions.append("\n");
                            }
                            profilOptionsLabel.setText(texteOptions.toString());
                            profilOptionsLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px;");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        profilOptionsLabel.setText("Impossible de charger les options.");
                        profilOptionsLabel.setStyle("-fx-text-fill: #EF5350;");
                    });
                    return null;
                });
    }

    /**
     * Intercepte les erreurs réseau ou API, gère l'expiration des sessions
     * et affiche des notifications d'erreur appropriées.
     *
     * @param ex L'exception propagée par les processus asynchrones.
     */
    private void handleApiException(Throwable ex) {
        Platform.runLater(() -> {
            elevesTable.setPlaceholder(new Label("Erreur de chargement."));
            optionsTable.setPlaceholder(new Label("Erreur de chargement."));
            Throwable cause = ex.getCause();
            if (cause instanceof ApiAuthException) {
                showAlert(Alert.AlertType.WARNING, "Session expirée", "Votre session a expiré. Veuillez vous reconnecter.");
                handleLogout();
            } else {
                LOGGER.log(Level.SEVERE, "Erreur API", ex);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Problème de communication avec le serveur.");
            }
        });
    }

    /**
     * Méthode utilitaire générique pour afficher des boîtes de dialogue natives JavaFX.
     *
     * @param type    Le type d'alerte (INFORMATION, WARNING, ERROR).
     * @param title   Le titre de la fenêtre d'alerte.
     * @param message Le message textuel à afficher.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Déconnecte l'utilisateur courant, invalide la session locale
     * et redirige vers l'écran d'authentification.
     */
    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            SceneManager.switchToLogin();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur de déconnexion", e);
        }
    }
}
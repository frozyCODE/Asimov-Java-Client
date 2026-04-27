package com.asimov.client.utils;

/**
 * Singleton gérant la session de l'utilisateur actuellement connecté.
 * <p>
 * Cette classe stocke les informations d'authentification et les identifiants
 * nécessaires aux appels API durant la durée de vie de l'application.
 * </p>
 */
public class UserSession {
    private static UserSession instance;

    private int id;
    private Integer eleveId;
    private String token;
    private String email;
    private String nom;
    private String prenom;
    private String role;

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     */
    private UserSession() {}

    /**
     * Retourne l'instance unique de la session utilisateur.
     * * @return L'instance UserSession.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Initialise les données de session lors d'une connexion réussie.
     *
     * @param id     L'ID technique de l'utilisateur (utilisateur_id).
     * @param token  Le jeton JWT fourni par le serveur.
     * @param email  L'adresse email de l'utilisateur.
     * @param nom    Le nom de famille.
     * @param prenom Le prénom.
     * @param role   Le rôle utilisateur (ex: PROVISEUR, ELEVE).
     */
    public void login(int id, String token, String email, String nom, String prenom, String role) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    /**
     * Réinitialise toutes les données de session lors de la déconnexion.
     */
    public void logout() {
        this.id = 0;
        this.eleveId = null;
        this.token = null;
        this.email = null;
        this.nom = null;
        this.prenom = null;
        this.role = null;
    }

    /**
     * Vérifie si un utilisateur est actuellement authentifié.
     * * @return true si un jeton est présent, false sinon.
     */
    public boolean isLoggedIn() { return token != null; }

    public int getId() { return id; }

    /**
     * Retourne l'ID métier de l'élève.
     * * @return L'ID élève ou null si l'utilisateur n'est pas un élève.
     */
    public Integer getEleveId() { return eleveId; }

    public void setEleveId(Integer eleveId) { this.eleveId = eleveId; }

    public String getToken() { return token; }

    public String getEmail() { return email; }

    public String getNom() { return nom; }

    public String getPrenom() { return prenom; }

    public String getRole() { return role; }

    /**
     * Vérifie si l'utilisateur possède un rôle spécifique.
     *
     * @param targetRole Le rôle à vérifier.
     * @return true si le rôle correspond (insensible à la casse).
     */
    public boolean hasRole(String targetRole) {
        return this.role != null && this.role.equalsIgnoreCase(targetRole);
    }
}
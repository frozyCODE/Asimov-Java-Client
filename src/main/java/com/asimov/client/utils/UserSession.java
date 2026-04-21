package com.asimov.client.utils;

/**
 * Singleton gérant la session de l'utilisateur actuellement connecté.
 */
public class UserSession {
    private static UserSession instance;

    private int id; // AJOUT DE L'ID
    private String token;
    private String email;
    private String nom;
    private String prenom;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(int id, String token, String email, String nom, String prenom, String role) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    public void logout() {
        this.id = 0;
        this.token = null;
        this.email = null;
        this.nom = null;
        this.prenom = null;
        this.role = null;
    }

    public boolean isLoggedIn() { return token != null; }

    public int getId() { return id; }
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getRole() { return role; }

    public boolean hasRole(String targetRole) {
        return this.role != null && this.role.equalsIgnoreCase(targetRole);
    }
}
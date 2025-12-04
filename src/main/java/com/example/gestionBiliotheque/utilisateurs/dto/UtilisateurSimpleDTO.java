package com.example.gestionBiliotheque.utilisateurs.dto;

/**
 * DTO simplifié pour les utilisateurs
 * Utilisé dans les réponses imbriquées pour éviter les références circulaires
 */
public class UtilisateurSimpleDTO {

    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;

    // Constructeurs
    public UtilisateurSimpleDTO() {
    }

    public UtilisateurSimpleDTO(Long id, String matricule, String nom, String prenom, String email) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

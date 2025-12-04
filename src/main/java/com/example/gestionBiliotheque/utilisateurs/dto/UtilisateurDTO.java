package com.example.gestionBiliotheque.utilisateurs.dto;

import com.example.gestionBiliotheque.utilisateurs.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les données utilisateur
 * Exclut les informations sensibles comme le mot de passe
 */
public class UtilisateurDTO {

    private Long id;

    @NotBlank
    @Size(max = 20)
    private String matricule;

    @NotBlank
    @Size(max = 50)
    private String nom;

    @NotBlank
    @Size(max = 50)
    private String prenom;

    @Email
    @NotBlank
    private String email;

    private Role role;

    private LocalDateTime dateInscription;

    private boolean actif;

    // Constructeurs
    public UtilisateurDTO() {}

    public UtilisateurDTO(Long id, String matricule, String nom, String prenom, 
                         String email, Role role, LocalDateTime dateInscription, boolean actif) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.dateInscription = dateInscription;
        this.actif = actif;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}

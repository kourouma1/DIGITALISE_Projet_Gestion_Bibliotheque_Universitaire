package com.example.gestionBiliotheque.utilisateurs.dto;

import com.example.gestionBiliotheque.utilisateurs.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête pour la mise à jour d'un utilisateur
 * Tous les champs sont optionnels pour permettre les mises à jour partielles
 */
public class UpdateUtilisateurDTO {

    @Size(max = 20, message = "Le matricule ne peut pas dépasser 20 caractères")
    private String matricule;

    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String prenom;

    @Email(message = "L'email doit être valide")
    private String email;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    private Role role;

    private Boolean actif;

    // Constructeurs
    public UpdateUtilisateurDTO() {
    }

    // Getters et Setters
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

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}

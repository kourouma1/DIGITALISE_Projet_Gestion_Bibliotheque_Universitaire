package com.example.gestionBiliotheque.auth.dto;

import com.example.gestionBiliotheque.utilisateurs.dto.UtilisateurDTO;

/**
 * DTO pour la réponse d'authentification
 * Contient le token JWT et les informations de l'utilisateur
 */
public class AuthResponseDTO {

    private String token;
    private String type = "Bearer";
    private UtilisateurDTO utilisateur;

    // Constructeurs
    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, UtilisateurDTO utilisateur) {
        this.token = token;
        this.utilisateur = utilisateur;
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }
}

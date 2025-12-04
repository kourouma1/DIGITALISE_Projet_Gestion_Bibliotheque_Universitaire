package com.example.gestionBiliotheque.emprunt.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO de requête pour la création d'un nouvel emprunt
 * Utilise des IDs pour référencer l'utilisateur et le livre
 */
public class CreateEmpruntDTO {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long utilisateurId;

    @NotNull(message = "L'ID du livre est obligatoire")
    private Long livreId;

    @NotNull(message = "La date de retour prévue est obligatoire")
    @Future(message = "La date de retour prévue doit être dans le futur")
    private LocalDateTime dateRetourPrevue;

    // Constructeurs
    public CreateEmpruntDTO() {
    }

    public CreateEmpruntDTO(Long utilisateurId, Long livreId, LocalDateTime dateRetourPrevue) {
        this.utilisateurId = utilisateurId;
        this.livreId = livreId;
        this.dateRetourPrevue = dateRetourPrevue;
    }

    // Getters et Setters
    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Long getLivreId() {
        return livreId;
    }

    public void setLivreId(Long livreId) {
        this.livreId = livreId;
    }

    public LocalDateTime getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public void setDateRetourPrevue(LocalDateTime dateRetourPrevue) {
        this.dateRetourPrevue = dateRetourPrevue;
    }
}

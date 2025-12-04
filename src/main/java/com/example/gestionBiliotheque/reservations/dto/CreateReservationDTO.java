package com.example.gestionBiliotheque.reservations.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO de requête pour la création d'une nouvelle réservation
 * Utilise des IDs pour référencer l'utilisateur et le livre
 */
public class CreateReservationDTO {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long utilisateurId;

    @NotNull(message = "L'ID du livre est obligatoire")
    private Long livreId;

    @NotNull(message = "La date d'expiration est obligatoire")
    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDateTime dateExpiration;

    // Constructeurs
    public CreateReservationDTO() {
    }

    public CreateReservationDTO(Long utilisateurId, Long livreId, LocalDateTime dateExpiration) {
        this.utilisateurId = utilisateurId;
        this.livreId = livreId;
        this.dateExpiration = dateExpiration;
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

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}

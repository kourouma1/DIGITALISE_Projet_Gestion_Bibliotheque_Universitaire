package com.example.gestionBiliotheque.reservations.dto;

import com.example.gestionBiliotheque.reservations.StatutReservation;

import java.time.LocalDateTime;

/**
 * DTO de requête pour la mise à jour d'une réservation
 * Tous les champs sont optionnels pour permettre les mises à jour partielles
 */
public class UpdateReservationDTO {

    private LocalDateTime dateExpiration;

    private StatutReservation statut;

    // Constructeurs
    public UpdateReservationDTO() {
    }

    // Getters et Setters
    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }
}

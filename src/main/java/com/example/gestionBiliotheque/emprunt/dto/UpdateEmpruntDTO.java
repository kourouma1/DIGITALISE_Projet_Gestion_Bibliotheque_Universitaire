package com.example.gestionBiliotheque.emprunt.dto;

import com.example.gestionBiliotheque.emprunt.StatutEmprunt;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de requête pour la mise à jour d'un emprunt
 * Tous les champs sont optionnels pour permettre les mises à jour partielles
 */
public class UpdateEmpruntDTO {

    private LocalDateTime dateRetourEffective;

    private StatutEmprunt statut;

    @DecimalMin(value = "0.0", message = "La pénalité ne peut pas être négative")
    private BigDecimal penalite;

    // Constructeurs
    public UpdateEmpruntDTO() {
    }

    // Getters et Setters
    public LocalDateTime getDateRetourEffective() {
        return dateRetourEffective;
    }

    public void setDateRetourEffective(LocalDateTime dateRetourEffective) {
        this.dateRetourEffective = dateRetourEffective;
    }

    public StatutEmprunt getStatut() {
        return statut;
    }

    public void setStatut(StatutEmprunt statut) {
        this.statut = statut;
    }

    public BigDecimal getPenalite() {
        return penalite;
    }

    public void setPenalite(BigDecimal penalite) {
        this.penalite = penalite;
    }
}

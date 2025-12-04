package com.example.gestionBiliotheque.reservations.dto;

import com.example.gestionBiliotheque.livres.dto.LivreSimpleDTO;
import com.example.gestionBiliotheque.reservations.StatutReservation;
import com.example.gestionBiliotheque.utilisateurs.dto.UtilisateurSimpleDTO;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les données de réservation
 * Utilise des DTOs simplifiés pour les relations afin d'éviter les références
 * circulaires
 */
public class ReservationDTO {

    private Long id;

    private UtilisateurSimpleDTO utilisateur;

    private LivreSimpleDTO livre;

    private LocalDateTime dateReservation;

    private LocalDateTime dateExpiration;

    private StatutReservation statut;

    // Constructeurs
    public ReservationDTO() {
    }

    public ReservationDTO(Long id, UtilisateurSimpleDTO utilisateur, LivreSimpleDTO livre,
            LocalDateTime dateReservation, LocalDateTime dateExpiration,
            StatutReservation statut) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.livre = livre;
        this.dateReservation = dateReservation;
        this.dateExpiration = dateExpiration;
        this.statut = statut;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UtilisateurSimpleDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurSimpleDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LivreSimpleDTO getLivre() {
        return livre;
    }

    public void setLivre(LivreSimpleDTO livre) {
        this.livre = livre;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

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

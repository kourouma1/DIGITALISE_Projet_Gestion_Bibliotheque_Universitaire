package com.example.gestionBiliotheque.emprunt.dto;

import com.example.gestionBiliotheque.emprunt.StatutEmprunt;
import com.example.gestionBiliotheque.livres.dto.LivreSimpleDTO;
import com.example.gestionBiliotheque.utilisateurs.dto.UtilisateurSimpleDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour les données d'emprunt
 * Utilise des DTOs simplifiés pour les relations afin d'éviter les références
 * circulaires
 */
public class EmpruntDTO {

    private Long id;

    private UtilisateurSimpleDTO utilisateur;

    private LivreSimpleDTO livre;

    private LocalDateTime dateEmprunt;

    private LocalDateTime dateRetourPrevue;

    private LocalDateTime dateRetourEffective;

    private StatutEmprunt statut;

    private BigDecimal penalite;

    // Constructeurs
    public EmpruntDTO() {
    }

    public EmpruntDTO(Long id, UtilisateurSimpleDTO utilisateur, LivreSimpleDTO livre,
            LocalDateTime dateEmprunt, LocalDateTime dateRetourPrevue,
            LocalDateTime dateRetourEffective, StatutEmprunt statut, BigDecimal penalite) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.livre = livre;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.dateRetourEffective = dateRetourEffective;
        this.statut = statut;
        this.penalite = penalite;
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

    public LocalDateTime getDateEmprunt() {
        return dateEmprunt;
    }

    public void setDateEmprunt(LocalDateTime dateEmprunt) {
        this.dateEmprunt = dateEmprunt;
    }

    public LocalDateTime getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public void setDateRetourPrevue(LocalDateTime dateRetourPrevue) {
        this.dateRetourPrevue = dateRetourPrevue;
    }

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

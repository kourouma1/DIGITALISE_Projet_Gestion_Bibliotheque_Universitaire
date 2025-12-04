package com.example.gestionBiliotheque.reservations;


import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reservations")
public class ReservationModel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private UtilisateurModel utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livre_id", nullable = false)
    private LivreModel livre;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateReservation = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    // Constructeurs
    public ReservationModel() {}

    public ReservationModel(UtilisateurModel utilisateur, LivreModel livre, LocalDateTime dateExpiration) {
        this.utilisateur = utilisateur;
        this.livre = livre;
        this.dateExpiration = dateExpiration;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UtilisateurModel getUtilisateur() { return utilisateur; }
    public void setUtilisateur(UtilisateurModel utilisateur) { this.utilisateur = utilisateur; }

    public LivreModel getLivre() { return livre; }
    public void setLivre(LivreModel livre) { this.livre = livre; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationModel that = (ReservationModel) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

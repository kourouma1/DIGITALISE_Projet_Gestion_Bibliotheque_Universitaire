package com.example.gestionBiliotheque.emprunt;


import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "emprunts")
public class EmpruntModel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private UtilisateurModel utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livre_id", nullable = false)
    private LivreModel livre;

    @Column(nullable = false)
    private LocalDateTime dateEmprunt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dateRetourPrevue;

    private LocalDateTime dateRetourEffective;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEmprunt statut = StatutEmprunt.EN_COURS;

    private BigDecimal penalite = BigDecimal.ZERO;

    // Constructeurs
    public EmpruntModel() {}

    public EmpruntModel(UtilisateurModel utilisateur, LivreModel livre, LocalDateTime dateRetourPrevue) {
        this.utilisateur = utilisateur;
        this.livre = livre;
        this.dateRetourPrevue = dateRetourPrevue;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UtilisateurModel getUtilisateur() { return utilisateur; }
    public void setUtilisateur(UtilisateurModel utilisateur) { this.utilisateur = utilisateur; }

    public LivreModel getLivre() { return livre; }
    public void setLivre(LivreModel livre) { this.livre = livre; }

    public LocalDateTime getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDateTime dateEmprunt) { this.dateEmprunt = dateEmprunt; }

    public LocalDateTime getDateRetourPrevue() { return dateRetourPrevue; }
    public void setDateRetourPrevue(LocalDateTime dateRetourPrevue) { this.dateRetourPrevue = dateRetourPrevue; }

    public LocalDateTime getDateRetourEffective() { return dateRetourEffective; }
    public void setDateRetourEffective(LocalDateTime dateRetourEffective) { this.dateRetourEffective = dateRetourEffective; }

    public StatutEmprunt getStatut() { return statut; }
    public void setStatut(StatutEmprunt statut) { this.statut = statut; }

    public BigDecimal getPenalite() { return penalite; }
    public void setPenalite(BigDecimal penalite) { this.penalite = penalite; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpruntModel emprunt = (EmpruntModel) o;
        return id != null && id.equals(emprunt.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

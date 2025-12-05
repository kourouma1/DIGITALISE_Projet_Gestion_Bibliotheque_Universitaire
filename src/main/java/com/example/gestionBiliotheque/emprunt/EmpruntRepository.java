package com.example.gestionBiliotheque.emprunt;

import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des emprunts
 */
@Repository
public interface EmpruntRepository extends JpaRepository<EmpruntModel, Long> {

    /**
     * Compte le nombre d'emprunts actifs d'un utilisateur
     */
    @Query("SELECT COUNT(e) FROM EmpruntModel e WHERE e.utilisateur = :utilisateur AND e.statut = :statut")
    long countByUtilisateurAndStatut(@Param("utilisateur") UtilisateurModel utilisateur,
            @Param("statut") StatutEmprunt statut);

    /**
     * Trouve les emprunts d'un utilisateur par statut
     */
    Page<EmpruntModel> findByUtilisateurAndStatut(UtilisateurModel utilisateur,
            StatutEmprunt statut,
            Pageable pageable);

    /**
     * Trouve tous les emprunts d'un utilisateur
     */
    Page<EmpruntModel> findByUtilisateur(UtilisateurModel utilisateur, Pageable pageable);

    /**
     * Trouve les emprunts en retard (date de retour prévue dépassée et statut
     * EN_COURS)
     */
    @Query("SELECT e FROM EmpruntModel e WHERE e.dateRetourPrevue < :now AND e.statut = 'EN_COURS'")
    Page<EmpruntModel> findOverdueEmprunts(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Calcule la somme des pénalités d'un utilisateur
     */
    @Query("SELECT COALESCE(SUM(e.penalite), 0) FROM EmpruntModel e WHERE e.utilisateur = :utilisateur")
    BigDecimal sumPenalitesByUtilisateur(@Param("utilisateur") UtilisateurModel utilisateur);

    /**
     * Trouve les emprunts en retard pour mise à jour de statut
     */
    @Query("SELECT e FROM EmpruntModel e WHERE e.dateRetourPrevue < :now AND e.statut = 'EN_COURS'")
    List<EmpruntModel> findLoansToMarkAsOverdue(@Param("now") LocalDateTime now);

    /**
     * Trouve les emprunts qui doivent être retournés dans les 24 prochaines heures
     */
    @Query("SELECT e FROM EmpruntModel e WHERE e.dateRetourPrevue BETWEEN :start AND :end AND e.statut = 'EN_COURS'")
    List<EmpruntModel> findLoansDueIn24Hours(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

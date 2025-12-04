package com.example.gestionBiliotheque.reservations;

import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des réservations
 */
@Repository
public interface ReservationRepository extends JpaRepository<ReservationModel, Long> {

    /**
     * Trouve les réservations d'un utilisateur par statut
     */
    Page<ReservationModel> findByUtilisateurAndStatut(UtilisateurModel utilisateur,
            StatutReservation statut,
            Pageable pageable);

    /**
     * Trouve toutes les réservations d'un utilisateur
     */
    Page<ReservationModel> findByUtilisateur(UtilisateurModel utilisateur, Pageable pageable);

    /**
     * Trouve les réservations expirées (EN_ATTENTE et date d'expiration dépassée)
     */
    @Query("SELECT r FROM ReservationModel r WHERE r.dateExpiration < :now AND r.statut = 'EN_ATTENTE'")
    List<ReservationModel> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Trouve les réservations actives pour un livre (EN_ATTENTE), triées par date
     * de réservation
     */
    @Query("SELECT r FROM ReservationModel r WHERE r.livre = :livre AND r.statut = 'EN_ATTENTE' ORDER BY r.dateReservation ASC")
    List<ReservationModel> findActiveReservationsForLivre(@Param("livre") LivreModel livre);

    /**
     * Vérifie si un utilisateur a déjà une réservation active pour un livre
     */
    @Query("SELECT r FROM ReservationModel r WHERE r.utilisateur = :utilisateur AND r.livre = :livre AND r.statut = 'EN_ATTENTE'")
    Optional<ReservationModel> findActiveReservationByUtilisateurAndLivre(
            @Param("utilisateur") UtilisateurModel utilisateur,
            @Param("livre") LivreModel livre);
}

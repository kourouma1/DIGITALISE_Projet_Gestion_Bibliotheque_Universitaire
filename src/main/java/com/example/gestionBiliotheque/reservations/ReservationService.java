package com.example.gestionBiliotheque.reservations;

import com.example.gestionBiliotheque.reservations.dto.CreateReservationDTO;
import com.example.gestionBiliotheque.reservations.dto.ReservationDTO;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface du service de gestion des réservations
 */
public interface ReservationService {

    /**
     * Crée une nouvelle réservation
     */
    ReservationDTO createReservation(CreateReservationDTO createReservationDTO);

    /**
     * Annule une réservation
     */
    void cancelReservation(Long reservationId, UtilisateurModel currentUser);

    /**
     * Récupère les réservations de l'utilisateur
     */
    Page<ReservationDTO> getMesReservations(UtilisateurModel utilisateur, Pageable pageable);

    /**
     * Nettoie les réservations expirées (appelé par tâche planifiée)
     */
    int cleanupExpiredReservations();
}

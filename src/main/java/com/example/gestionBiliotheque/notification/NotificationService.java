package com.example.gestionBiliotheque.notification;

import com.example.gestionBiliotheque.emprunt.EmpruntModel;
import com.example.gestionBiliotheque.reservations.ReservationModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;

/**
 * Service de notification pour envoyer des alertes aux utilisateurs
 */
public interface NotificationService {

    /**
     * Envoie une notification lorsqu'un livre réservé devient disponible
     *
     * @param reservation La réservation concernée
     */
    void sendReservationAvailableNotification(ReservationModel reservation);

    /**
     * Envoie un rappel 24h avant la date de retour d'un emprunt
     *
     * @param emprunt L'emprunt concerné
     */
    void sendReturnReminderNotification(EmpruntModel emprunt);

    /**
     * Envoie une notification de bienvenue à un nouvel utilisateur
     *
     * @param utilisateur Le nouvel utilisateur
     */
    void sendWelcomeNotification(UtilisateurModel utilisateur);

    /**
     * Envoie une notification d'emprunt en retard
     *
     * @param emprunt L'emprunt en retard
     */
    void sendOverdueNotification(EmpruntModel emprunt);
}

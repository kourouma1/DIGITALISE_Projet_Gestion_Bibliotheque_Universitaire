package com.example.gestionBiliotheque.scheduled;

import com.example.gestionBiliotheque.emprunt.EmpruntModel;
import com.example.gestionBiliotheque.emprunt.EmpruntRepository;
import com.example.gestionBiliotheque.emprunt.StatutEmprunt;
import com.example.gestionBiliotheque.notification.NotificationService;
import com.example.gestionBiliotheque.reservations.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tâches planifiées pour la maintenance du système
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Nettoie les réservations expirées
     * Exécuté toutes les heures
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredReservations() {
        logger.info("Démarrage du nettoyage des réservations expirées");
        int count = reservationService.cleanupExpiredReservations();
        logger.info("Nettoyage terminé: {} réservations annulées", count);
    }

    /**
     * Met à jour le statut des emprunts en retard
     * Exécuté tous les jours à minuit
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateOverdueLoansStatus() {
        logger.info("Démarrage de la mise à jour des emprunts en retard");

        List<EmpruntModel> overdueLoans = empruntRepository.findLoansToMarkAsOverdue(LocalDateTime.now());

        int count = 0;
        for (EmpruntModel loan : overdueLoans) {
            loan.setStatut(StatutEmprunt.EN_RETARD);
            empruntRepository.save(loan);
            
            // Envoyer une notification de retard
            try {
                notificationService.sendOverdueNotification(loan);
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi de la notification de retard pour l'emprunt ID: {}", loan.getId(), e);
            }
            
            count++;
        }

        logger.info("Mise à jour terminée: {} emprunts marqués comme EN_RETARD", count);
    }

    /**
     * Envoie des rappels 24h avant la date de retour
     * Exécuté tous les jours à 9h00
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendReturnReminders() {
        logger.info("Démarrage de l'envoi des rappels de retour 24h");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        LocalDateTime dayAfterTomorrow = now.plusHours(48);

        // Trouver les emprunts qui doivent être retournés dans les 24-48 prochaines heures
        List<EmpruntModel> loansDueSoon = empruntRepository.findLoansDueIn24Hours(tomorrow, dayAfterTomorrow);

        int count = 0;
        for (EmpruntModel loan : loansDueSoon) {
            try {
                notificationService.sendReturnReminderNotification(loan);
                count++;
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi du rappel pour l'emprunt ID: {}", loan.getId(), e);
            }
        }

        logger.info("Rappels envoyés: {} notifications de retour", count);
    }
}

package com.example.gestionBiliotheque.scheduled;

import com.example.gestionBiliotheque.emprunt.EmpruntModel;
import com.example.gestionBiliotheque.emprunt.EmpruntRepository;
import com.example.gestionBiliotheque.emprunt.StatutEmprunt;
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
            count++;
        }

        logger.info("Mise à jour terminée: {} emprunts marqués comme EN_RETARD", count);
    }
}

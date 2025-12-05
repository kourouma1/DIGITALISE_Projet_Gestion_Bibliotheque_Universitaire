package com.example.gestionBiliotheque.reports;

import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;

/**
 * Service de génération de rapports
 */
public interface ReportService {

    /**
     * Génère un rapport PDF de l'historique des emprunts d'un utilisateur
     *
     * @param utilisateur L'utilisateur concerné
     * @param startDate Date de début (optionnel)
     * @param endDate Date de fin (optionnel)
     * @return Le fichier PDF généré
     */
    Resource generateLoanHistoryPdf(UtilisateurModel utilisateur, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Génère un rapport PDF de tous les emprunts (ADMIN uniquement)
     *
     * @param startDate Date de début (optionnel)
     * @param endDate Date de fin (optionnel)
     * @return Le fichier PDF généré
     */
    Resource generateAllLoansPdf(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Génère un rapport Excel avec les statistiques de la bibliothèque
     *
     * @return Le fichier Excel généré
     */
    Resource generateStatisticsExcel();

    /**
     * Génère un rapport Excel des emprunts en retard
     *
     * @return Le fichier Excel généré
     */
    Resource generateOverdueLoansExcel();
}

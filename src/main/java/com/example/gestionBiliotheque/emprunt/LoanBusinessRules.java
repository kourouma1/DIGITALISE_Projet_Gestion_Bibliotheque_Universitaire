package com.example.gestionBiliotheque.emprunt;

import com.example.gestionBiliotheque.utilisateurs.Role;

import java.math.BigDecimal;

/**
 * Règles métier centralisées pour le système d'emprunt
 */
public class LoanBusinessRules {

    // Limites d'emprunts par rôle
    public static final int MAX_LOANS_USER = 3;
    public static final int MAX_LOANS_MANAGER = 5;
    public static final int MAX_LOANS_ADMIN = 5;

    // Durées d'emprunt en jours
    public static final int LOAN_DURATION_USER = 14;
    public static final int LOAN_DURATION_MANAGER = 30;
    public static final int LOAN_DURATION_ADMIN = 30;

    // Pénalités
    public static final BigDecimal PENALTY_PER_DAY = new BigDecimal("1000"); // 1000 GNF
    public static final BigDecimal MAX_PENALTY_ALLOWED = new BigDecimal("10000"); // 10000 GNF

    // Réservations
    public static final int RESERVATION_VALIDITY_HOURS = 48;

    /**
     * Retourne le nombre maximum d'emprunts autorisés pour un rôle
     */
    public static int getMaxLoansForRole(Role role) {
        return switch (role) {
            case ETUDIANT -> MAX_LOANS_USER;
            case BIBLIOTHECAIRE -> MAX_LOANS_MANAGER;
            case ADMIN -> MAX_LOANS_ADMIN;
        };
    }

    /**
     * Retourne la durée d'emprunt en jours pour un rôle
     */
    public static int getLoanDurationForRole(Role role) {
        return switch (role) {
            case ETUDIANT -> LOAN_DURATION_USER;
            case BIBLIOTHECAIRE -> LOAN_DURATION_MANAGER;
            case ADMIN -> LOAN_DURATION_ADMIN;
        };
    }

    /**
     * Calcule la pénalité pour un nombre de jours de retard
     */
    public static BigDecimal calculatePenalty(long daysLate) {
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return PENALTY_PER_DAY.multiply(BigDecimal.valueOf(daysLate));
    }
}

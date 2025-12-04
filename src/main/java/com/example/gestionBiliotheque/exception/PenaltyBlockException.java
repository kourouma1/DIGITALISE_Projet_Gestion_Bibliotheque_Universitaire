package com.example.gestionBiliotheque.exception;

import java.math.BigDecimal;

/**
 * Exception levée lorsqu'un utilisateur a trop de pénalités impayées
 */
public class PenaltyBlockException extends RuntimeException {

    private final BigDecimal totalPenalties;
    private final BigDecimal maxAllowed;

    public PenaltyBlockException(BigDecimal totalPenalties, BigDecimal maxAllowed) {
        super(String.format(
                "Vous avez %.0f GNF de pénalités impayées. Veuillez régulariser avant d'emprunter (maximum autorisé: %.0f GNF).",
                totalPenalties, maxAllowed));
        this.totalPenalties = totalPenalties;
        this.maxAllowed = maxAllowed;
    }

    public BigDecimal getTotalPenalties() {
        return totalPenalties;
    }

    public BigDecimal getMaxAllowed() {
        return maxAllowed;
    }
}

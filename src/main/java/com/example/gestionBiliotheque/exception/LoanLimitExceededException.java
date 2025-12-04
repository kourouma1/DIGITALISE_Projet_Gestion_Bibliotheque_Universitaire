package com.example.gestionBiliotheque.exception;

/**
 * Exception levée lorsqu'un utilisateur dépasse sa limite d'emprunts
 */
public class LoanLimitExceededException extends RuntimeException {

    private final int currentLoans;
    private final int maxLoans;

    public LoanLimitExceededException(int currentLoans, int maxLoans) {
        super(String.format("Limite d'emprunts atteinte (%d/%d). Veuillez retourner un livre avant d'emprunter.",
                currentLoans, maxLoans));
        this.currentLoans = currentLoans;
        this.maxLoans = maxLoans;
    }

    public int getCurrentLoans() {
        return currentLoans;
    }

    public int getMaxLoans() {
        return maxLoans;
    }
}

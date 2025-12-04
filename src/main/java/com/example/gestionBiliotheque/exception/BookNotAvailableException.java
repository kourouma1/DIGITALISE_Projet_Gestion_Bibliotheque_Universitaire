package com.example.gestionBiliotheque.exception;

/**
 * Exception levée lorsqu'un livre n'est pas disponible
 * Peut inclure des informations sur une réservation créée automatiquement
 */
public class BookNotAvailableException extends RuntimeException {

    private final Long reservationId;

    public BookNotAvailableException(String message) {
        super(message);
        this.reservationId = null;
    }

    public BookNotAvailableException(String message, Long reservationId) {
        super(message);
        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return reservationId;
    }
}

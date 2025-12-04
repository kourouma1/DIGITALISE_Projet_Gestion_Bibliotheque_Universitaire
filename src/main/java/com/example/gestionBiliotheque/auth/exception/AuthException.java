package com.example.gestionBiliotheque.auth.exception;

/**
 * Exception personnalisée pour les erreurs d'authentification
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

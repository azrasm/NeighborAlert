package com.projekat.user_service.auth;

/**
 * Baca se kada autentifikacija ne uspije (pogrešan username ili lozinka).
 * Rezultira HTTP 401 Unauthorized odgovorom.
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}

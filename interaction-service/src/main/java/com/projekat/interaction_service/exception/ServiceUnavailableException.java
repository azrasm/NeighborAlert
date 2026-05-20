package com.projekat.interaction_service.exception;

/**
 * Baca se kada zavisni mikroservis (report-service ili user-service)
 * nije dostupan u trenutku sinhrone komunikacije.
 *
 * Rezultira HTTP 503 Service Unavailable odgovorom klijentu,
 * dok interaction-service i dalje ostaje aktivan (zadatak 5f).
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

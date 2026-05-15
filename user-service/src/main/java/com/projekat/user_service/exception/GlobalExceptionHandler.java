package com.projekat.user_service.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Greške pri @Valid anotaciji na request body */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("validation_error", "Podaci nisu validni", errors));
    }

    /** Greške pri @Validated anotaciji na query parametre */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            errors.put(field, cv.getMessage());
        });
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("validation_error", "Parametri nisu validni", errors));
    }

    /** 404 - resurs nije pronađen */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("not_found", ex.getMessage(), null));
    }

    /** 401 - pogrešan username ili lozinka pri login-u */
    @ExceptionHandler(com.projekat.user_service.auth.AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            com.projekat.user_service.auth.AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("unauthorized", ex.getMessage(), null));
    }

    /** Poslovne greške (dupli username, nedovoljno bodova, itd.) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("bad_request", ex.getMessage(), null));
    }

    /** Fallback za sve ostale greške */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericExceptions(Exception ex) {
        log.error("Neočekivana greška: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("server_error", "Interna greška servera.", null));
    }
}
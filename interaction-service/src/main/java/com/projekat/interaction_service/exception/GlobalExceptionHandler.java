package com.projekat.interaction_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Hvatanje grešaka VALIDACIJE (npr. prazan tekst komentara)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Uzimamo prvu grešku iz liste validacijskih grešaka
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        
        ErrorResponse error = new ErrorResponse("validation_error", errorMessage);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 2. Hvatanje greške kada nešto NIJE PRONAĐENO (npr. brisanje nepostojećeg ID-a)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("not_found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 3. Hvatanje svih OSTALIH neočekivanih grešaka
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        ex.printStackTrace(); 
        ErrorResponse error = new ErrorResponse("server_error", "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
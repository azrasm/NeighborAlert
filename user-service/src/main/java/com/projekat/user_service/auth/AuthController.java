package com.projekat.user_service.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler za autentifikaciju.
 *
 * Ovi endpointi su JAVNI — ne zahtijevaju JWT token.
 * U gateway konfiguraciji ruta /api/auth/** nema JwtAuthFilter.
 *
 * Trenutno dostupni endpointi:
 *   POST /api/auth/login    — prijava, vraća JWT token
 *   POST /api/auth/register — registracija novog korisnika
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autentifikacija — login i registracija")
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint — jedina javna ruta koja vraća JWT token.
     *
     * Primjer zahtjeva:
     *   POST /api/auth/login
     *   { "username": "admin", "password": "admin123" }
     *
     * Primjer odgovora:
     *   {
     *     "token": "eyJhbGciOiJIUzI1NiJ9...",
     *     "tokenType": "Bearer",
     *     "username": "admin",
     *     "role": "ADMIN",
     *     "expiresIn": 3600
     *   }
     *
     * Klijent čuva token i šalje ga u svakom zahtjevu:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     */
    @PostMapping("/login")
    @Operation(summary = "Prijava korisnika",
               description = "Vraća JWT token koji se koristi za autorizaciju svih ostalih zahtjeva.")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

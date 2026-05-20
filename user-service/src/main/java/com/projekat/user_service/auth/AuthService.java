package com.projekat.user_service.auth;

import com.projekat.user_service.model.User;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servis za autentifikaciju korisnika.
 *
 * Login tok:
 *   1. Pronađi korisnika po usernameu
 *   2. Provjeri lozinku pomoću BCrypt (nikad ne porediti plain text!)
 *   3. Generiši JWT token
 *   4. Vrati token klijentu
 *
 * Logout:
 *   JWT tokeni su stateless — ne čuvaju se na serveru.
 *   "Logout" se implementira na klijentskoj strani brisanjem tokena.
 *   Token ostaje tehnički validan do isteka (expiresIn), što je
 *   prihvatljivo za kratko trajanje tokena (npr. 1h).
 *   Za striktni logout potrebna je blacklist tokena (Redis) — ovo
 *   je opisano u dokumentaciji kao opcija za produkciju.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Autentifikuje korisnika i vraća JWT token.
     *
     * @param request login zahtjev s username-om i lozinkom
     * @return odgovor s JWT tokenom
     * @throws AuthException ako username ne postoji ili lozinka nije ispravna
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Pronađi korisnika
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[AUTH] Neuspješan login — korisnik '{}' ne postoji",
                            request.getUsername());
                    // Namjerno ista poruka kao za krivu lozinku
                    // da napadač ne zna da li username postoji
                    return new AuthException("Pogrešan username ili lozinka.");
                });

        // 2. Provjeri lozinku
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[AUTH] Neuspješan login — pogrešna lozinka za '{}'",
                    request.getUsername());
            throw new AuthException("Pogrešan username ili lozinka.");
        }

        // 3. Generiši token
        String token = jwtUtil.generateToken(user);
        log.info("[AUTH] Uspješan login: user='{}', role='{}'",
                user.getUsername(), user.getRole().getName());

        return new LoginResponseDTO(
                token,
                user.getUsername(),
                user.getRole().getName(),
                expirationMs / 1000 // pretvaramo ms u sekunde
        );
    }
}

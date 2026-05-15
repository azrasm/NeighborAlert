package com.projekat.user_service.security;

import com.projekat.user_service.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility za generisanje JWT tokena u user-service.
 *
 * Token se generiše pri uspješnom login-u i šalje klijentu.
 * Klijent ga čuva i šalje u svakom zahtjevu u Authorization headeru.
 * API Gateway validira token koristeći isti tajni ključ.
 *
 * Sadržaj tokena (payload):
 *   - sub: username korisnika
 *   - role: naziv role (ADMIN, USER, MODERATOR)
 *   - userId: ID korisnika (da mikroservisi znaju ko šalje zahtjev)
 *   - iat: issued at (kada je token kreiran)
 *   - exp: expiration (kada token ističe)
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generiše JWT token za datog korisnika.
     *
     * @param user korisnik koji se ulogovao
     * @return potpisani JWT token kao String
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().getName())
                .claim("userId", user.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}

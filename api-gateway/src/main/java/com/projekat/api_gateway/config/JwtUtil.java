package com.projekat.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Utility za validaciju JWT tokena u API Gateway-u.
 *
 * Gateway SAMO validira tokene — ne kreira ih.
 * Tokeni se kreiraju u user-service pri login-u.
 *
 * Tajni ključ mora biti identičan kao u user-service —
 * jedino tako gateway može provjeriti da je token autentičan.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validira JWT token i vraća claims (payload).
     *
     * @param token JWT token iz Authorization headera (bez "Bearer " prefiksa)
     * @return Claims objekt s podacima iz tokena (username, role, expiration)
     * @throws JwtException ako je token nevalidan, istekao ili je potpis pogrešan
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Provjerava da li je token sintaktički ispravan bez bacanja exceptiona.
     */
    public boolean isValid(String token) {
        try {
            validateAndGetClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Izvlači rolu korisnika iz tokena.
     */
    public String getRole(String token) {
        return validateAndGetClaims(token).get("role", String.class);
    }

    /**
     * Izvlači username korisnika iz tokena.
     */
    public String getUsername(String token) {
        return validateAndGetClaims(token).getSubject();
    }
}

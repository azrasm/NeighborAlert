package com.projekat.user_service.security;

import com.projekat.user_service.model.User;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Utility za generisanje JWT tokena u user-service.
 *
 * Token se generise pri uspjesnom login-u i salje klijentu.
 * Klijent ga cuva i salje u svakom zahtjevu u Authorization headeru.
 * API Gateway validira token koristeci javni RSA kljuc.
 *
 * Asimetricna enkripcija (RS256):
 *   - user-service potpisuje tokene PRIVATNIM kljucem
 *   - api-gateway verifikuje tokene JAVNIM kljucem
 *   - Privatni kljuc nikad ne napusta user-service
 *
 * Sadrzaj tokena (payload):
 *   - sub: username korisnika
 *   - role: naziv role (ADMIN, USER, MODERATOR)
 *   - userId: ID korisnika
 *   - iat: issued at
 *   - exp: expiration
 */
@Component
public class JwtUtil {

    private final RSAPrivateKey privateKey;
    private final long expirationMs;

 public JwtUtil(
        @Value("${jwt.private-key-file}") String privateKeyPath,
        @Value("${jwt.expiration-ms}") long expirationMs) throws Exception {
    String pem = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(privateKeyPath)));
    String stripped = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] decoded = Base64.getDecoder().decode(stripped);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    this.privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    this.expirationMs = expirationMs;
}

    /**
     * Generise JWT token za datog korisnika, potpisan RSA privatnim kljucem (RS256).
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
                .signWith(privateKey)
                .compact();
    }
}

package com.projekat.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility za validaciju JWT tokena u API Gateway-u.
 *
 * Gateway SAMO validira tokene — ne kreira ih.
 * Tokeni se kreiraju u user-service pri login-u, potpisani RSA privatnim kljucem.
 *
 * Asimetricna enkripcija (RS256):
 *   - Validacija se vrsi RSA JAVNIM kljucem
 *   - Javni kljuc se moze slobodno dijeliti izmedju servisa
 *   - Nije moguce kreirati token sa javnim kljucem (jednosmjerno)
 */
@Component
public class JwtUtil {

    private final RSAPublicKey publicKey;

  public JwtUtil(@Value("${jwt.public-key-file}") String publicKeyPath) throws Exception {
    String pem = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(publicKeyPath)));
    String stripped = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
    byte[] decoded = Base64.getDecoder().decode(stripped);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    this.publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
}

    /**
     * Validira JWT token i vraca claims (payload).
     *
     * @param token JWT token iz Authorization headera (bez "Bearer " prefiksa)
     * @return Claims objekt s podacima iz tokena (username, role, expiration)
     * @throws JwtException ako je token nevalidan, istekao ili je potpis pogresan
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Provjerava da li je token sintakticki ispravan bez bacanja exceptiona.
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
     * Izvlaci rolu korisnika iz tokena.
     */
    public String getRole(String token) {
        return validateAndGetClaims(token).get("role", String.class);
    }

    /**
     * Izvlaci username korisnika iz tokena.
     */
    public String getUsername(String token) {
        return validateAndGetClaims(token).getSubject();
    }
}

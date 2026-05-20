package com.projekat.api_gateway.filter;

import com.projekat.api_gateway.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gateway filter koji provjerava JWT token za svaki zasticeni endpoint.
 *
 * Tok provjere:
 *   1. Postoji li Authorization header?
 *   2. Pocinje li s "Bearer "?
 *   3. Je li token validan (potpis RS256, expiration)?
 *   4. Ima li korisnik potrebnu rolu (ako su role konfigurisane za rutu)?
 *   5. Ako sve prodje → proslije zahtjev mikroservisu + dodaj X-Username, X-Role, X-User-Id headere
 *   6. Ako ne prodje → vrati 401 Unauthorized ili 403 Forbidden
 *
 * Mikroservisi dobijaju X-Username, X-Role i X-User-Id headere od gateway-a —
 * ne moraju sami parsirati JWT, gateway je vec to uradio.
 *
 * Napomena: Spring Cloud Gateway koristi WebFlux (reaktivni stack),
 * pa filter mora biti reaktivan (vraca Mono<Void>).
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Dohvati Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 1. Provjeri da li header postoji
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("[JWT] Odbijen zahtjev na {} — nedostaje Authorization header", path);
                return unauthorizedResponse(exchange, "Authorization header nedostaje ili nije Bearer token.");
            }

            // 2. Izvuci token (ukloni "Bearer " prefiks)
            String token = authHeader.substring(BEARER_PREFIX.length());

            // 3. Validiraj token
            try {
                Claims claims = jwtUtil.validateAndGetClaims(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                // 4. Provjera autorizacije po rolama
                if (!config.getRoles().isEmpty() && !config.getRoles().contains(role)) {
                    log.warn("[JWT] Zabranjen pristup: user='{}', role='{}', path='{}', potrebna rola: {}",
                            username, role, path, config.getRoles());
                    return forbiddenResponse(exchange, "Nemate pravo pristupa ovom resursu.");
                }

                log.info("[JWT] Autorizovan zahtjev: user='{}', role='{}', path='{}'",
                        username, role, path);

                // 5. Proslijedi zahtjev s dodatnim headerima
                // Mikroservisi mogu citati ko je ulogovan bez parsiranja JWT-a
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Username", username)
                        .header("X-Role", role != null ? role : "")
                        .header("X-User-Id", claims.get("userId", String.class) != null
                                ? claims.get("userId", String.class) : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException e) {
                log.warn("[JWT] Odbijen zahtjev na {} — neispravan token: {}", path, e.getMessage());
                return unauthorizedResponse(exchange, "Token je neispravan ili je istekao.");
            }
        };
    }

    /**
     * Vraca 401 Unauthorized odgovor s JSON porukom.
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"error\": \"unauthorized\", \"message\": \"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * Vraca 403 Forbidden odgovor s JSON porukom.
     */
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"error\": \"forbidden\", \"message\": \"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Lista rola koje imaju pristup rutama sa ovim filterom.
        // Prazna lista = svi autentifikovani korisnici imaju pristup.
        // Primjer u application.yml:
        //   filters:
        //     - name: JwtAuthFilter
        //       args:
        //         roles: ADMIN
        private List<String> roles = new ArrayList<>();

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}

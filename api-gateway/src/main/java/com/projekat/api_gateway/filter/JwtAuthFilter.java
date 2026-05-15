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

/**
 * Gateway filter koji provjerava JWT token za svaki zaštićeni endpoint.
 *
 * Tok provjere:
 *   1. Postoji li Authorization header?
 *   2. Počinje li s "Bearer "?
 *   3. Je li token validan (potpis, expiration)?
 *   4. Ako sve prođe → proslijedi zahtjev mikroservisu + dodaj X-Username i X-Role headere
 *   5. Ako ne prođe → vrati 401 Unauthorized
 *
 * Mikroservisi dobijaju X-Username i X-Role headere od gateway-a —
 * ne moraju sami parsirati JWT, gateway je već to uradio.
 *
 * Napomena: Spring Cloud Gateway koristi WebFlux (reaktivni stack),
 * pa filter mora biti reaktivan (vraća Mono<Void>).
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

                log.info("[JWT] Autorizovan zahtjev: user='{}', role='{}', path='{}'",
                        username, role, path);

                // 4. Proslijedi zahtjev s dodatnim headerima
                // Mikroservisi mogu čitati ko je ulogovan bez parsiranja JWT-a
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
     * Vraća 401 Unauthorized odgovor s JSON porukom.
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

    public static class Config {
        // Konfiguracija filtera — za sada prazna, može se proširiti
        // (npr. lista rola koje imaju pristup određenoj ruti)
    }
}

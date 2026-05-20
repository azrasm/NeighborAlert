package com.projekat.interaction_service.service;

import com.projekat.interaction_service.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Servis koji demonstrira load balancing putem @LoadBalanced RestTemplate.
 *
 * Razlika između Feign klijenta i @LoadBalanced RestTemplate:
 *
 *   Feign (u CommentService):
 *     - Deklarativni pristup — interfejs s anotacijama
 *     - Automatski load balancing kroz Eureku
 *     - Pogodniji za kompleksnu komunikaciju
 *
 *   @LoadBalanced RestTemplate (ovdje):
 *     - Imperativan pristup — eksplicitni HTTP pozivi
 *     - Isti load balancing mehanizam kao Feign
 *     - Lakše se vidi KOJI URL se koristi i kako load balancing radi
 *     - Koristi se ime servisa umjesto IP:porta → Eureka + LoadBalancer
 *       automatski biraju instancu (round-robin)
 *
 * Ovaj servis se koristi u LoadBalancingController endpointu koji je
 * dizajniran za testiranje load balancinga skriptom.
 *
 * Kada load_balance_test.py pošalje 100 zahtjeva na:
 *   GET http://localhost:8083/api/lb-test/user/{id}
 *
 * Svaki poziv dolazi do ovog servisa koji interno poziva:
 *   http://user-service/api/users/{id}   ← ime servisa, ne port!
 *
 * Spring Cloud LoadBalancer tada bira između registrovanih instanci
 * (npr. user-service:8081 i user-service:8085) round-robin algoritmom.
 */
@Service
public class LoadBalancedCommentService {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancedCommentService.class);

    // Koristi @LoadBalanced RestTemplate iz AppConfig
    private final RestTemplate restTemplate;

    @Autowired
    public LoadBalancedCommentService(RestTemplate loadBalancedRestTemplate) {
        this.restTemplate = loadBalancedRestTemplate;
    }

    /**
     * Dohvata korisnika putem @LoadBalanced RestTemplate.
     *
     * Ključna stvar: URL koristi "user-service" (ime iz Eureke),
     * a NE "localhost:8081" ili "localhost:8085".
     * Spring Cloud LoadBalancer automatski odlučuje na koju instancu
     * ide svaki konkretan poziv.
     *
     * @param userId ID korisnika
     * @return UserDTO od user-service, ili null ako servis nedostupan
     */
    public UserDTO getUserViaLoadBalancer(Long userId) {
        // Ime servisa umjesto hardkodiranog porta — LoadBalancer bira instancu
        String url = "http://user-service/api/users/" + userId;

        try {
            log.info("[LB] Šaljem zahtjev na: {} (LoadBalancer će odabrati instancu)", url);
            UserDTO user = restTemplate.getForObject(url, UserDTO.class);
            log.info("[LB] Odgovor primljen za userId={}", userId);
            return user;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[LB] user-service vratio 404 za userId={}", userId);
            return null;
        } catch (ResourceAccessException e) {
            log.error("[LB] user-service nije dostupan: {}", e.getMessage());
            return null;
        }
    }
}

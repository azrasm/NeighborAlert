package com.projekat.interaction_service.controller;

import com.projekat.interaction_service.dto.UserDTO;
import com.projekat.interaction_service.service.LoadBalancedCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler koji izlaže endpoint za testiranje load balancinga.
 *
 * Ovaj endpoint koristi @LoadBalanced RestTemplate koji automatski
 * raspoređuje zahtjeve na dostupne instance user-service putem Eureke.
 *
 * Dizajn:
 *   Skripta (load_balance_test.py) šalje 100 zahtjeva na OVAJ endpoint.
 *   Svaki zahtjev dolazi do LoadBalancedCommentService koji poziva:
 *     "http://user-service/api/users/{id}"
 *   Spring Cloud LoadBalancer tada bira između registrovanih instanci
 *   user-service i raspoređuje pozive round-robin algoritmom.
 *
 *   Skripta je "glupa" — ne zna za instance, ne bira ručno.
 *   Sve raspoređivanje rade Eureka + Spring Cloud LoadBalancer automatski.
 */
@RestController
@RequestMapping("/api/lb-test")
@Tag(name = "Load Balancing Test", description = "Endpoint za testiranje @LoadBalanced RestTemplate")
public class LoadBalancingController {

    @Autowired
    private LoadBalancedCommentService loadBalancedCommentService;

    /**
     * Dohvata korisnika putem @LoadBalanced RestTemplate.
     *
     * Ovaj endpoint je namjenski dizajniran za load balance testiranje:
     * - Skripta šalje 100 GET zahtjeva OVDJE
     * - Svaki zahtjev interno poziva user-service putem LoadBalancera
     * - LoadBalancer automatski raspoređuje na dostupne instance
     * - Logovi interaction-service pokazuju raspodjelu po instancama
     *
     * @param userId ID korisnika za dohvatanje
     * @return UserDTO od user-service
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Dohvata korisnika putem @LoadBalanced RestTemplate",
        description = "Svaki poziv se automatski raspoređuje na jednu od registrovanih " +
                      "instanci user-service putem Spring Cloud LoadBalancera (Eureka)."
    )
    public ResponseEntity<UserDTO> getUserViaLoadBalancer(@PathVariable Long userId) {
        UserDTO user = loadBalancedCommentService.getUserViaLoadBalancer(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}

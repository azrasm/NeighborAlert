package com.projekat.interaction_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Konfiguracija Spring beans za interaction-service.
 *
 * Ključna komponenta za load balancing (Zadatak 5 — Ribbon/LoadBalancer):
 *
 * @LoadBalanced anotacija na RestTemplate-u govori Spring Cloud-u da
 * presretne svaki poziv i umjesto literalnog URL-a (npr. "http://user-service/api/users")
 * koristi Eureka service discovery za pronalaženje stvarne adrese.
 *
 * Kada postoji više instanci user-service (npr. na portovima 8081 i 8085),
 * Spring Cloud LoadBalancer automatski raspoređuje zahtjeve koristeći
 * round-robin algoritam — BEZ ikakvog hardkodiranja portova ili IP adresa.
 *
 * Napomena o Ribbon vs Spring Cloud LoadBalancer:
 * Spring Cloud 2020+ je zamijenio Ribbon s Spring Cloud LoadBalancer koji
 * dolazi ugrađen u spring-cloud-starter-netflix-eureka-client. Funkcionalnost
 * je identična (round-robin po defaultu), ali bez externe zavisnosti.
 *
 * Primjer upotrebe u LoadBalancedCommentService:
 *   restTemplate.getForObject("http://user-service/api/users/{id}", UserDTO.class, id)
 *   → Eureka razrješava "user-service" → load balancer bira instancu → HTTP poziv
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate s automatskim load balancingom putem Eureke.
     *
     * Razlika od običnog RestTemplate-a:
     *   Obični:    restTemplate.getForObject("http://localhost:8081/api/users/1", ...)
     *              → uvijek ide na port 8081, nema load balancinga
     *
     *   @LoadBalanced: restTemplate.getForObject("http://user-service/api/users/1", ...)
     *              → Eureka pronalazi sve instance "user-service"
     *              → LoadBalancer bira jednu (round-robin)
     *              → stvarni poziv ide na izabranu instancu
     */
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
}

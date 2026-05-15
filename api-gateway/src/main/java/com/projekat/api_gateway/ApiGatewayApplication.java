package com.projekat.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — jedina ulazna tačka u NeighborAlert sistem.
 *
 * Sve što dolazi izvana mora proći kroz gateway na portu 8080.
 * Gateway:
 *   1. Provjeri JWT token (osim za /api/auth/**)
 *   2. Proslijedi zahtjev odgovarajućem mikroservisu putem Eureke
 *   3. Vrati odgovor klijentu
 *
 * Mikroservisi direktno nisu dostupni izvana (trebaju biti firewall-ovani
 * u produkciji tako da primaju zahtjeve samo od gateway-a).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

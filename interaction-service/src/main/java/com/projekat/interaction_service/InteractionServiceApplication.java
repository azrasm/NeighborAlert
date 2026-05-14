package com.projekat.interaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Glavna klasa interaction-service mikroservisa.
 *
 * @EnableFeignClients aktivira sve Feign klijente u paketu "client" —
 *   omogućava deklarativnu sinhronu komunikaciju s report-service i
 *   user-service bez hardkodiranih adresa.
 *
 * @EnableDiscoveryClient registruje ovaj servis u Eureka server i
 *   omogućava dohvatanje adresa drugih servisa po imenu (Zadatak 5d).
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class InteractionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InteractionServiceApplication.class, args);
	}

}

package com.projekat.interaction_service.client;

import com.projekat.interaction_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign klijent za sinhronu komunikaciju s user-service.
 *
 * Koristi Eureka service discovery — "user-service" se razrješava
 * u stvarnu adresu bez ikakvog hardkodiranja porta ili IP-a.
 *
 * Svrha: validacija da korisnik postoji u user-service prije nego
 * što se komentar sačuva u interaction_db, čime se sprečava
 * kreiranje komentara s nevalidnim userId.
 */
@FeignClient(
    name = "user-service",
    fallback = UserServiceFallback.class
)
public interface UserServiceClient {

    /**
     * Dohvata korisnika po ID-u iz user-service.
     * Koristi se za validaciju da userId postoji.
     *
     * @param id ID korisnika
     * @return DTO korisnika, ili null ako fallback aktivan
     */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}

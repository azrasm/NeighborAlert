package com.projekat.interaction_service.client;

import com.projekat.interaction_service.dto.ReportDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign klijent za sinhronu komunikaciju s report-service.
 *
 * Umjesto hardkodiranog URL-a koristi ime servisa "report-service"
 * koje Eureka razrješava u stvarnu IP adresu i port aktivne instance.
 * Spring Cloud LoadBalancer automatski raspoređuje pozive ako postoji
 * više instanci (load balancing je uključen automatski).
 *
 * Scenarij (Zadatak 5a): Kada korisnik kreira komentar, interaction-service
 * sinhronom komunikacijom provjerava da li prijava (report) s tim ID-em
 * zaista postoji u report-service. Bez ove provjere moglo bi doći do
 * nekonzistentnih podataka — komentari koji referenciraju nepostojeće prijave.
 *
 * Dijagram toka:
 *   POST /api/comments
 *     → interaction-service
 *       → GET /api/reports/{id}  [sinhron poziv na report-service putem Eureke]
 *       → GET /api/users/{id}    [sinhron poziv na user-service putem Eureke]
 *       → INSERT u interaction_db (samo ako su oba poziva uspješna)
 *     ← 201 Created / 404 / 503
 */
@FeignClient(
    name = "report-service",           // mora odgovarati spring.application.name u report-service
    fallback = ReportServiceFallback.class
)
public interface ReportServiceClient {

    /**
     * Dohvata prijavu po ID-u iz report-service.
     * Koristi se za validaciju da reportId postoji prije čuvanja komentara.
     *
     * @param id ID prijave
     * @return DTO s podacima prijave, ili null ako fallback aktivan
     */
    @GetMapping("/api/reports/{id}")
    ReportDTO getReportById(@PathVariable("id") Long id);
}

package com.projekat.administration_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.projekat.administration_service.config.RabbitMQConfig;
import com.projekat.administration_service.dto.ReportResolvedEvent;
import com.projekat.administration_service.service.AdministrationService;

/**
 * =========================================================
 *                    Zadatak 8. RabbitMQ
 * =========================================================
* Opis:
* Consumer zaduzen za obradu rollback promjena u bazi.
 * Ukoliko bilo koji servis u kasnijoj fazi lanca
 * baci gresku, generise se kaskadni rollback unazad. 
 * Izvrsava inverznu transakciju i vraca podatke 
 * u administraciji u prvobitno stanje
 * =========================================================
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrationRollbackConsumer  {

    private final AdministrationService administrationService;

    @RabbitListener(queues = RabbitMQConfig.ROLLBACK_QUEUE)
    public void handleAdministrationRollback(ReportResolvedEvent failedEvent) {
        log.warn("PRIMLJEN ROLLBACK SIGNAL! Pokrece se inverzna akciju za Report ID: {}", failedEvent.getReportId());
        
        try {
            // metoda iz servis klase za vracanje u prvobitno stanje
            administrationService.rollbackStatusChange(failedEvent.getReportId());
            
            log.info("Uspješno izvršena inverzna akcija u bazi administracije.");
        } catch (Exception e) {
            log.error("Greška prilikom izvršavanja inverzne akcije: {}", e.getMessage());
        }
    }
}
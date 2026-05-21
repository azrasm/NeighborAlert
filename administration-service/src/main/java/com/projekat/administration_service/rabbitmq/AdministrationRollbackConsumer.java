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
    // ANSI Kodovi za boje u terminalu
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";

    private final AdministrationService administrationService;

    @RabbitListener(queues = RabbitMQConfig.ROLLBACK_QUEUE)
    public void handleAdministrationRollback(ReportResolvedEvent failedEvent) {
        log.warn(ANSI_RED + "ROLLBACK! Pokrece se inverzna akciju za Report ID: {}", failedEvent.getReportId());
        
        try {
            // metoda iz servis klase za vracanje u prvobitno stanje
            administrationService.rollbackStatusChange(failedEvent.getReportId());
            
            log.info(ANSI_GREEN + "Uspjesno izvrsena inverzna akcija u bazi administracije.");
        } catch (Exception e) {
            log.error(ANSI_RED + "Greska prilikom izvrsavanja inverzne akcije: {}", e.getMessage());
        }
    }
}
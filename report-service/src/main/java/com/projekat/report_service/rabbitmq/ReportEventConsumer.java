package com.projekat.report_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.projekat.report_service.dto.AssignmentCompletedEvent;
import com.projekat.report_service.dto.ReportResolvedEvent;
import com.projekat.report_service.service.ReportService;

import jakarta.transaction.Transactional;

import com.projekat.report_service.config.RabbitMQConfig;
import com.projekat.report_service.dto.ReportDTO;

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
public class ReportEventConsumer {
    // ANSI Kodovi za boje u terminalu
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";

    private final RabbitTemplate rabbitTemplate;
    private final ReportService reportService;

    // Slusa uspjesan zavrsetak iz administracije
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.ASSIGNMENT_COMPLETED_QUEUE)
    public void handleAssignmentCompleted(AssignmentCompletedEvent event) {
        log.info(ANSI_YELLOW + "Primljen event iz administracije za Report ID: {}", event.getReportId());
        
        try {
            // Lokalna transakcija: 
            // Azuriranje statusa prijave u novi status
            reportService.updateReportStatusByName(event.getReportId(), event.getNewStatus());
            
            // Ako je upis u bazu uspjesan i novi status je "Riješeno"
            // saljemo poruku za bodove
             // Pokrece se RabbitMQ Saga samo ako administrator postavlja status na "Riješeno"
            if ("Riješeno".equalsIgnoreCase(event.getNewStatus())) {
                ReportDTO targetReport = reportService.getReportById(event.getReportId());
                ReportResolvedEvent nextEvent = new ReportResolvedEvent(event.getReportId(), targetReport.getUserId(), 50);
                log.info(ANSI_GREEN + "Report azuriran. Salje se zahtjev za dodjelu bodova korisniku: {}", targetReport.getUserId());
            
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SUCCESS_ROUTING_KEY, nextEvent);
            }
            
        } catch (Exception e) {
            log.error(ANSI_RED + "Greska pri upisu u Report bazu. Pokrece se rollback za administraciju. Greska: {}", e.getMessage());
            
            // Inverzna akcija: 
            // Saljemo originalni event nazad
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROLLBACK_ROUTING_KEY, event);
        }
    }

    // Slusa kaskadni rollback ako user-service baci gresku
    @RabbitListener(queues = RabbitMQConfig.ROLLBACK_QUEUE)
    public void handleReportRollback(ReportResolvedEvent failedEvent) {
        log.warn(ANSI_RED + "ROLLBACK! Vracanje statusa prijave ID: {}", failedEvent.getReportId());
        
        try {
            // Inverzna akcija: Vratite status prijave sa "Riješeno" nazad
            reportService.rollbackReportStatus(failedEvent.getReportId());
            
            log.info(ANSI_GREEN + "Uspjesno ponisten status prijave u report-service bazi.");

            // Slanje istog event na exchange kako bi i administracija radila rollback
            log.info(ANSI_YELLOW + "Prosljeduje se rollback prema administraciji za Report ID: {}", failedEvent.getReportId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ADMIN_ROLLBACK_KEY, failedEvent);
            
        } catch (Exception e) {
            log.error(ANSI_RED + "Greska prilikom izvrsavanja inverzne akcije u report-service: {}", e.getMessage());
        }
    }
}
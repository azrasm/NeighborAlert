package com.projekat.report_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.projekat.report_service.dto.AssignmentCompletedEvent;
import com.projekat.report_service.dto.ReportResolvedEvent;
import com.projekat.report_service.service.ReportService;
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

    private final RabbitTemplate rabbitTemplate;
    private final ReportService reportService;

    // Slusa uspjesan zavrsetak iz administracije
    @RabbitListener(queues = RabbitMQConfig.ASSIGNMENT_COMPLETED_QUEUE)
    public void handleAssignmentCompleted(AssignmentCompletedEvent event) {
        log.info("Primljen event iz administracije za Report ID: {}", event.getReportId());
        
        try {
            // Lokalna transakcija: 
            // Azuriranje statusa prijave u "Riješeno"
            reportService.updateReportStatusByName(event.getReportId(), "Riješeno");
            
            // Ako je upis u bazu uspjesan
            // saljemo poruku za bodove
            ReportDTO targetReport = reportService.getReportById(event.getReportId());
            ReportResolvedEvent nextEvent = new ReportResolvedEvent(event.getReportId(), targetReport.getUserId(), 50);
            log.info("Report ažuriran. Šaljem zahtjev za dodjelu bodova korisniku: {}", targetReport.getUserId());
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SUCCESS_ROUTING_KEY, nextEvent);
            
        } catch (Exception e) {
            log.error("Greška pri upisu u Report bazu. Pokrećem rollback za administraciju. Greška: {}", e.getMessage());
            
            // Inverzna akcija: 
            // Saljemo originalni event nazad
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROLLBACK_ROUTING_KEY, event);
        }
    }

    // Slusa kaskadni rollback ako user-service baci gresku
    @RabbitListener(queues = RabbitMQConfig.ROLLBACK_QUEUE)
    public void handleReportRollback(ReportResolvedEvent failedEvent) {
        log.warn("PRIMLJEN KASKADNI ROLLBACK! Vraćanje statusa prijave ID: {}", failedEvent.getReportId());
        
        try {
            // Inverzna akcija: Vratite status prijave sa "Riješeno" nazad
            reportService.rollbackReportStatus(failedEvent.getReportId());
            
            log.info("Uspješno poništen status prijave u report-service bazi.");

            // Slanje istog event na exchange kako bi i administracija radila rollback
            log.info("Prosljeđujem rollback signal prema administraciji za Report ID: {}", failedEvent.getReportId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROLLBACK_ROUTING_KEY, failedEvent);
            
        } catch (Exception e) {
            log.error("Greška prilikom izvršavanja inverzne akcije u report-service: {}", e.getMessage());
        }
    }
}
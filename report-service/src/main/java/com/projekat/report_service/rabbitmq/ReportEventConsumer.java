package com.projekat.report_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
// import com.neighboralert.reportservice.service.ReportService;

import com.projekat.report_service.dto.AssignmentCompletedEvent;
import com.projekat.report_service.dto.ReportResolvedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventConsumer {

    private final RabbitTemplate rabbitTemplate;
    // private final ReportService reportService;

    private static final String EXCHANGE = "neighboralert.exchange";
    private static final String SUCCESS_ROUTING_KEY = "report.resolved";
    private static final String ROLLBACK_ROUTING_KEY = "saga.rollback";

    // 1. Sluša uspješan završetak iz administracije
    @RabbitListener(queues = "report.assignment.completed.queue")
    public void handleAssignmentCompleted(AssignmentCompletedEvent event) {
        log.info("Primljen event iz administracije za Report ID: {}", event.getReportId());
        
        try {
            // LOKALNA TRANSAKCIJA 2: Ažuriranje statusa prijave u "RIJEŠENO"
            // reportService.updateReportStatus(event.getReportId(), "RIJEŠENO");
            
            // Ako je upis u bazu uspješan, IDEMO DALJE - šaljemo poruku za bodove
            ReportResolvedEvent nextEvent = new ReportResolvedEvent(event.getReportId(), event.getUserId(), 50);
            log.info("Report ažuriran. Šaljem zahtjev za dodjelu bodova korisniku: {}", event.getUserId());
            
            rabbitTemplate.convertAndSend(EXCHANGE, SUCCESS_ROUTING_KEY, nextEvent);
            
        } catch (Exception e) {
            log.error("Greška pri upisu u Report bazu. Pokrećem rollback za administraciju. Greška: {}", e.getMessage());
            
            // INVERZNA AKCIJA (Vrati unazad): Šaljemo originalni event nazad na rollback ključ
            rabbitTemplate.convertAndSend(EXCHANGE, ROLLBACK_ROUTING_KEY, event);
        }
    }

    // 2. Sluša kaskadni rollback ako user-service baci grešku
    @RabbitListener(queues = "report.rollback.queue")
    public void handleReportRollback(ReportResolvedEvent failedEvent) {
        log.warn("PRIMLJEN KASKADNI ROLLBACK! Vraćanje statusa prijave ID: {}", failedEvent.getReportId());
        
        try {
            // INVERZNA AKCIJA: Vratite status prijave sa "RIJEŠENO" nazad na npr. "U_TOKU"
            // reportService.updateReportStatus(failedEvent.getReportId(), "U_TOKU");
            
            log.info("Uspješno poništen status prijave u report-service bazi.");
        } catch (Exception e) {
            log.error("Greška prilikom izvršavanja inverzne akcije u report-service: {}", e.getMessage());
        }
    }
}
package com.projekat.administration_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.projekat.administration_service.dto.AssignmentCompletedEvent;
import com.projekat.administration_service.service.AdministrationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrationRollbackConsumer  {

    private final AdministrationService administrationService;

    // Sluša namjenski red za rollback administracije
    @RabbitListener(queues = "administration.rollback.queue")
    public void handleAdministrationRollback(AssignmentCompletedEvent failedEvent) {
        log.warn("PRIMLJEN ROLLBACK SIGNAL! Pokrećem inverznu akciju za Report ID: {}", failedEvent.getReportId());
        
        try {
            // POZIV TVOJE INVERZNE METODE IZ SERVISA:
            administrationService.rollbackStatusChange(failedEvent.getReportId());
            
            log.info("Uspješno izvršena inverzna akcija u bazi administracije.");
        } catch (Exception e) {
            log.error("Greška prilikom izvršavanja inverzne akcije: {}", e.getMessage());
        }
    }
}
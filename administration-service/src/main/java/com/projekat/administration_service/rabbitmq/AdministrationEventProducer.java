package com.projekat.administration_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.projekat.administration_service.config.RabbitMQConfig;
import com.projekat.administration_service.dto.AssignmentCompletedEvent;

/**
 * =========================================================
 *                    Zadatak 8. RabbitMQ
 * =========================================================
* Opis:
 * Klasa zaduzena za slanje (produciranje) poruka na RabbitMQ exchange
 * Kada administrator oznazi prijavu kao rijesenu
 * asinhrono obavjestava ostale mikroservise u lancu (report-service)
 * da azuriraju podatke (report se oznaci kao rijesen)
 * =========================================================
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrationEventProducer {

    // ANSI Kodovi za boje u terminalu
    private static final String ANSI_YELLOW = "\u001B[33m";

    private final RabbitTemplate rabbitTemplate;

    public void produceAssignmentCompleted(Long assignmentId, Long reportId, String newStatus) {
        // Kreiranje event-a
        AssignmentCompletedEvent event = new AssignmentCompletedEvent(assignmentId, reportId, newStatus);
        
        log.info(ANSI_YELLOW + "Slanje poruke na RabbitMQ: Assignment {} je zavrsen.", assignmentId);
        
        // Slanje objekta u odgovarajuci queue unutar report-service
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }
}
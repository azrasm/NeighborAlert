package com.projekat.administration_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.projekat.administration_service.dto.AssignmentCompletedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrationEventProducer {

    private final RabbitTemplate rabbitTemplate;
    
    // Naziv exchange-a iz naše konfiguracije
    private static final String EXCHANGE = "neighboralert.exchange";
    private static final String ROUTING_KEY = "assignment.completed";

    public void produceAssignmentCompleted(Long assignmentId, Long reportId, Long userId) {
        AssignmentCompletedEvent event = new AssignmentCompletedEvent(assignmentId, reportId, userId);
        
        log.info("Slanje poruke na RabbitMQ: Assignment {} je završen.", assignmentId);
        
        // Slanje objekta koji se automatski pretvara u JSON
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
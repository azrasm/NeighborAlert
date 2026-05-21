package com.projekat.user_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.projekat.user_service.service.UserService;
import com.projekat.user_service.dto.ReportResolvedEvent;
import com.projekat.user_service.config.RabbitMQConfig;

/**
 * =========================================================
 *                    Zadatak 8. RabbitMQ
 * =========================================================
* Opis:
* Consumer zaduzen za dodjelu bodova i za 
*  obradu rollbacka (obavjestava report-service) ako dodje do greske.
 * =========================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;

    // SLusa report-service
    // Ako je report uspjesno oznacen kao zavrsen, dodjeluje bodove
    @RabbitListener(queues = RabbitMQConfig.REPORT_RESOLVED_QUEUE)
    public void handleAwardPoints(ReportResolvedEvent event) {
        log.info("Zahtjev za dodjelu {} bodova korisniku ID: {}", event.getPointsToAward(), event.getUserId());
        
        try {
            // Lokalna transakcija: Dodjela bodova
            userService.addPointsToUserScore(event.getUserId(), event.getPointsToAward());
        
            log.info("BODOVI USPJEŠNO DODIJELJENI. Saga uspješno završena za Report ID: {}. Podaci konzistentni!", event.getReportId());
            
        } catch (Exception e) {
            log.error("KRIZA! Neuspješna dodjela bodova korisniku {}. Pokrećem svesistemski rollback!", event.getUserId());
            
            // Slanje poruke report-service-u da se izvrsava rollback
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROLLBACK_ROUTING_KEY, event);
        }
    }
}
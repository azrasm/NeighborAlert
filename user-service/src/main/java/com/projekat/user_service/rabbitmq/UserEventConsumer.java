package com.projekat.user_service.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
// import com.neighboralert.userservice.service.UserService;

import com.projekat.user_service.dto.ReportResolvedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final RabbitTemplate rabbitTemplate;
    // private final UserService userService;

    private static final String EXCHANGE = "neighboralert.exchange";
    private static final String ROLLBACK_ROUTING_KEY = "saga.rollback";

    @RabbitListener(queues = "user.report.resolved.queue")
    public void handleAwardPoints(ReportResolvedEvent event) {
        log.info("Zahtjev za dodjelu {} bodova korisniku ID: {}", event.getPointsToAward(), event.getUserId());
        
        try {
            // LOKALNA TRANSAKCIJA 3: Dodavanje bodova u bazu podataka korisnika
            // userService.addPointsToUserScore(event.getUserId(), event.getPointsToAward());
            
            // !!! AKCIJA JE FINALNA !!!
            log.info("BODOVI USPJEŠNO DODIJELJENI. Saga uspješno završena za Report ID: {}. Podaci konzistentni!", event.getReportId());
            
        } catch (Exception e) {
            log.error("KRIZA! Neuspješna dodjela bodova korisniku {}. Pokrećem svesistemski rollback!", event.getUserId());
            
            // Recikliramo isti event i šaljemo ga na globalni saga.rollback ključ
            // Ovu poruku će primiti i report.rollback.queue i administration.rollback.queue!
            rabbitTemplate.convertAndSend(EXCHANGE, ROLLBACK_ROUTING_KEY, event);
        }
    }
}
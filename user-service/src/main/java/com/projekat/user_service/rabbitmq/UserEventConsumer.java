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
    // ANSI Kodovi za boje u terminalu
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";

    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;

    // SLusa report-service
    // Ako je report uspjesno oznacen kao zavrsen, dodjeluje bodove
    @RabbitListener(queues = RabbitMQConfig.REPORT_RESOLVED_QUEUE)
    public void handleAwardPoints(ReportResolvedEvent event) {
        log.info(ANSI_YELLOW + "Zahtjev za dodjelu {} bodova korisniku ID: {}", event.getPointsToAward(), event.getUserId());
        
        try {
             /*if (true) { 
        throw new RuntimeException("SIMULIRANA GREŠKA: Korisnički servis nije dostupan, pokrećem SAGA ROLLBACK!"); 
    }*/
            // Lokalna transakcija: Dodjela bodova
            userService.addPointsToUserScore(event.getUserId(), event.getPointsToAward());
        
            log.info(ANSI_GREEN + "BODOVI USPJESNO DODIJELJENI. Saga uspjesno zavrsena za Report ID: {}. Podaci konzistentni!", event.getReportId());
            
        } catch (Exception e) {
            log.error(ANSI_RED + "Neuspjesna dodjela bodova korisniku {}. Pokrece se rollback", event.getUserId());
            
            // Slanje poruke report-service-u da se izvrsava rollback
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROLLBACK_ROUTING_KEY, event);
        }
    }
}
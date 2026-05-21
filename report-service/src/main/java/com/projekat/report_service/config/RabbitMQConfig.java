package com.projekat.report_service.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
/**
 * =========================================================
 *                    Zadatak 8. RabbitMQ
 * =========================================================
 *
 * Konfiguracija:
 * Imenovanje kljuceva
 * Definisanje exchange-a
 * Definisanje Queue za uspjesni i neuspjesne dogadjaje 
 * =========================================================
 */

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "neighboralert.exchange";
    
    // Slusa poruke iz administracije kada je report rijesen
    public static final String ASSIGNMENT_COMPLETED_QUEUE = "report.assignment.completed.queue";
    public static final String ASSIGNMENT_COMPLETED_ROUTING_KEY = "assignment.completed";

    // Slusa globalni rollback ako user-service baci gresku
    public static final String ROLLBACK_QUEUE = "report.rollback.queue";
    public static final String ROLLBACK_ROUTING_KEY = "saga.rollback";

    // Poseban kljuc za rollback ka administraciji
    public static final String ADMIN_ROLLBACK_KEY = "administration.rollback";

    public static final String SUCCESS_ROUTING_KEY = "report.resolved";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Queue za primanje obavijesti o rijesenim reportovima
    @Bean
    public Queue ticketCompletedQueue() {
        return new Queue(ASSIGNMENT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding bindingTicketCompleted(Queue ticketCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ticketCompletedQueue).to(exchange).with(ASSIGNMENT_COMPLETED_ROUTING_KEY);
    }

    // Queue za roolback akcije u slucaju greske
    @Bean
    public Queue rollbackQueue() {
        return new Queue(ROLLBACK_QUEUE, true);
    }

    @Bean
    public Binding bindingRollback(Queue rollbackQueue, TopicExchange exchange) {
        return BindingBuilder.bind(rollbackQueue).to(exchange).with(ROLLBACK_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(@NonNull ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

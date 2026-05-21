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
 ********  Zadatak 8. RabbitMQ **********
 * 
 */

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "neighboralert.exchange";
    
    // Sluša poruke iz administracije da je tiket završen
    public static final String TICKET_COMPLETED_QUEUE = "report.ticket.completed.queue";
    public static final String TICKET_COMPLETED_ROUTING_KEY = "ticket.completed";

    // Sluša globalni rollback ako user-service baci grešku
    public static final String ROLLBACK_QUEUE = "report.rollback.queue";
    public static final String ROLLBACK_ROUTING_KEY = "saga.rollback";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Red za primanje obavijesti o završenim tiketima
    @Bean
    public Queue ticketCompletedQueue() {
        return new Queue(TICKET_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding bindingTicketCompleted(Queue ticketCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ticketCompletedQueue).to(exchange).with(TICKET_COMPLETED_ROUTING_KEY);
    }

    // Red za kompenzacijsku (inverznu) akciju u Report servisu
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

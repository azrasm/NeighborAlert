package com.projekat.administration_service.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // Exchange
    public static final String EXCHANGE = "neighboralert.exchange";

    // Rollback queue u slucaju greske 
    public static final String ROLLBACK_QUEUE = "administration.rollback.queue";
    public static final String ROLLBACK_ROUTING_KEY = "saga.rollback";

    public static final String ROUTING_KEY = "assignment.completed";

    // Definisanje Topic Exchange-a
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    //Definisanje Queue rollback u administraciji
    @Bean
    public Queue rollbackQueue() {
        return new Queue(ROLLBACK_QUEUE, true); // durable = true (znaci da cuva poruke i ako se Rabbit restartuje)
    }

    // Binding rollback queue na exchange sa routing kljucem
    @Bean
    public Binding bindingRollback(Queue rollbackQueue, TopicExchange exchange) {
        return BindingBuilder.bind(rollbackQueue).to(exchange).with(ROLLBACK_ROUTING_KEY);
    }

    // Konfiguracija Jackson konvertera za automatsku serijalizaciju Java objekata u json
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Konfiguracija RabbitTemplate da koristi json umjesto podrazumijevane Java serijalizacije
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

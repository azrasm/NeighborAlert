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

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "neighboralert.exchange";
    public static final String ROLLBACK_QUEUE = "administration.rollback.queue";
    public static final String ROLLBACK_ROUTING_KEY = "saga.rollback";

    // 1. Definisanje Topic Exchange-a
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // 2. Definisanje Queue-a za inverznu akciju (Rollback) u administraciji
    @Bean
    public Queue rollbackQueue() {
        return new Queue(ROLLBACK_QUEUE, true); // durable = true (čuva poruke i ako se Rabbit restartuje)
    }

    // 3. Vezivanje (Binding) rollback queue-a na exchange sa routing ključem
    @Bean
    public Binding bindingRollback(Queue rollbackQueue, TopicExchange exchange) {
        return BindingBuilder.bind(rollbackQueue).to(exchange).with(ROLLBACK_ROUTING_KEY);
    }

    // 4. Konfiguracija Jackson konvertera za automatsku serijalizaciju Java objekata u JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 5. Konfiguracija RabbitTemplate-a da koristi JSON umjesto podrazumijevane Java serijalizacije
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

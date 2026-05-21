package com.projekat.user_service.config;

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
    
    // Sluša poruke iz report-service da je prijava uspješno riješena kako bi dodijelio bodove
    public static final String REPORT_RESOLVED_QUEUE = "user.report.resolved.queue";
    public static final String REPORT_RESOLVED_ROUTING_KEY = "report.resolved";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Red za primanje poruka o dodjeli bodova
    @Bean
    public Queue reportResolvedQueue() {
        return new Queue(REPORT_RESOLVED_QUEUE, true);
    }

    @Bean
    public Binding bindingReportResolved(Queue reportResolvedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reportResolvedQueue).to(exchange).with(REPORT_RESOLVED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
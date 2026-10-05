package com.ejemplo.pagos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_PAGOS = "pagos.exchange";
    public static final String QUEUE_PAGO_CREADO = "pago.creado.queue";
    public static final String QUEUE_PAGO_CREADO_DLQ = "pago.creado.dlq";
    public static final String ROUTING_KEY_PAGO_CREADO = "pago.creado";

    @Bean
    public TopicExchange pagosExchange() {
        return new TopicExchange(EXCHANGE_PAGOS, true, false);
    }

    @Bean
    public Queue pagoCreadoQueue() {
        return QueueBuilder.durable(QUEUE_PAGO_CREADO)
                .deadLetterExchange(EXCHANGE_PAGOS)
                .deadLetterRoutingKey("pago.creado.dlq")
                .build();
    }

    @Bean
    public Queue pagoCreadoDlq() {
        return QueueBuilder.durable(QUEUE_PAGO_CREADO_DLQ).build();
    }

    @Bean
    public Binding pagoCreadoBinding(Queue pagoCreadoQueue, TopicExchange pagosExchange) {
        return BindingBuilder.bind(pagoCreadoQueue).to(pagosExchange).with(ROUTING_KEY_PAGO_CREADO);
    }

    @Bean
    public Binding pagoCreadoDlqBinding(Queue pagoCreadoDlq, TopicExchange pagosExchange) {
        return BindingBuilder.bind(pagoCreadoDlq).to(pagosExchange).with("pago.creado.dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

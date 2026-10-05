package com.ejemplo.pagos.messaging;

import com.ejemplo.pagos.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PagoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PagoEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public PagoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarPagoCreado(PagoCreadoEvent evento) {
        log.info("Publicando evento pago.creado para pago #{}", evento.pagoId());
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_PAGOS,
            RabbitConfig.ROUTING_KEY_PAGO_CREADO,
            evento
        );
    }
}

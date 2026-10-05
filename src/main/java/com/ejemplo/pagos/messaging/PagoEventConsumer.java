package com.ejemplo.pagos.messaging;

import com.ejemplo.pagos.client.BancoExternoClient;
import com.ejemplo.pagos.config.RabbitConfig;
import com.ejemplo.pagos.entity.IntentoPago;
import com.ejemplo.pagos.entity.Pago;
import com.ejemplo.pagos.repository.IntentoPagoRepository;
import com.ejemplo.pagos.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PagoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagoEventConsumer.class);

    private final PagoRepository pagoRepo;
    private final IntentoPagoRepository intentoRepo;
    private final BancoExternoClient bancoClient;
    private final PagoEventConsumer self;

    public PagoEventConsumer(PagoRepository pagoRepo,
                             IntentoPagoRepository intentoRepo,
                             BancoExternoClient bancoClient,
                             @org.springframework.context.annotation.Lazy PagoEventConsumer self) {
        this.pagoRepo = pagoRepo;
        this.intentoRepo = intentoRepo;
        this.bancoClient = bancoClient;
        this.self = self;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_PAGO_CREADO)
    public void procesarPagoCreado(PagoCreadoEvent evento) {
        log.info("Recibido evento pago.creado para pago #{}", evento.pagoId());
        try {
            self.procesarEnTransaccion(evento);
        } catch (Exception ex) {
            // Relanzamos para que RabbitMQ reintente
            log.warn("Reintento programado para pago #{}: {}", evento.pagoId(), ex.getMessage());
            throw ex;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarEnTransaccion(PagoCreadoEvent evento) {
        Pago pago = pagoRepo.findById(evento.pagoId()).orElse(null);
        if (pago == null) {
            log.error("Pago #{} no encontrado, descartando evento", evento.pagoId());
            return;
        }

        // Si ya está APROBADO o RECHAZADO, no reintentar
        if (pago.getEstado() == Pago.Estado.APROBADO || pago.getEstado() == Pago.Estado.RECHAZADO) {
            log.info("Pago #{} ya está en estado final {}, se ignora el reintento", pago.getId(), pago.getEstado());
            return;
        }

        try {
            BancoExternoClient.RespuestaBanco resp = bancoClient.autorizar(
                evento.cuentaOrigen(),
                evento.cuentaDestino(),
                evento.monto()
            );

            pago.setEstado(resp.aprobado() ? Pago.Estado.APROBADO : Pago.Estado.RECHAZADO);
            pago.setMensaje(resp.mensaje());
            pago.setActualizadoEn(LocalDateTime.now());
            pagoRepo.save(pago);

            registrarIntento(pago, pago.getEstado().name(), resp.mensaje());
            log.info("Pago #{} actualizado a {}", pago.getId(), pago.getEstado());

        } catch (RuntimeException ex) {
            pago.setEstado(Pago.Estado.EN_REVISION);
            pago.setMensaje("Timeout del banco: " + ex.getMessage());
            pago.setActualizadoEn(LocalDateTime.now());
            pagoRepo.save(pago);
            registrarIntento(pago, "TIMEOUT", ex.getMessage());
            log.warn("Pago #{} marcado como EN_REVISION, se reintentará", pago.getId());
            throw ex;  // Relanzar → RabbitMQ reintenta
        }
    }

    private void registrarIntento(Pago pago, String resultado, String detalle) {
        IntentoPago intento = new IntentoPago();
        intento.setPago(pago);
        intento.setResultado(resultado);
        intento.setDetalle(detalle);
        intentoRepo.save(intento);
    }
}

package com.ejemplo.pagos.service;

import com.ejemplo.pagos.entity.Cuenta;
import com.ejemplo.pagos.entity.IntentoPago;
import com.ejemplo.pagos.entity.Pago;
import com.ejemplo.pagos.messaging.PagoCreadoEvent;
import com.ejemplo.pagos.messaging.PagoEventPublisher;
import com.ejemplo.pagos.repository.CuentaRepository;
import com.ejemplo.pagos.repository.IntentoPagoRepository;
import com.ejemplo.pagos.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final CuentaRepository cuentaRepo;
    private final PagoRepository pagoRepo;
    private final IntentoPagoRepository intentoRepo;
    private final PagoEventPublisher publisher;

    public PagoService(CuentaRepository cuentaRepo, PagoRepository pagoRepo,
                       IntentoPagoRepository intentoRepo, PagoEventPublisher publisher) {
        this.cuentaRepo = cuentaRepo;
        this.pagoRepo = pagoRepo;
        this.intentoRepo = intentoRepo;
        this.publisher = publisher;
    }

    @Transactional
    public Pago iniciarPago(String idempotencyKey, String numeroOrigen, String numeroDestino, BigDecimal monto) {

        Optional<Pago> existente = pagoRepo.findByIdempotencyKey(idempotencyKey);
        if (existente.isPresent()) {
            log.info("Pago con idempotencyKey {} ya existe, devolviendo existente", idempotencyKey);
            return existente.get();
        }

        Cuenta origen = cuentaRepo.findByNumero(numeroOrigen)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no existe: " + numeroOrigen));
        Cuenta destino = cuentaRepo.findByNumero(numeroDestino)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no existe: " + numeroDestino));

        Pago pago = new Pago();
        pago.setIdempotencyKey(idempotencyKey);
        pago.setCuentaOrigen(origen);
        pago.setCuentaDestino(destino);
        pago.setMonto(monto);
        pago.setEstado(Pago.Estado.PENDIENTE);
        pago.setCreadoEn(LocalDateTime.now());

        String motivo = validar(origen, monto);
        if (motivo != null) {
            pago.setEstado(Pago.Estado.RECHAZADO);
            pago.setMensaje(motivo);
        }

        pago = pagoRepo.save(pago);

        // Registrar intento inicial
        registrarIntento(pago, pago.getEstado().name(),
                pago.getMensaje() != null ? pago.getMensaje() : "Pago registrado, pendiente de autorización");

        // Solo publicar evento si el pago quedó PENDIENTE (validaciones síncronas OK)
        if (pago.getEstado() == Pago.Estado.PENDIENTE) {
            publisher.publicarPagoCreado(new PagoCreadoEvent(
                pago.getId(),
                origen.getNumero(),
                destino.getNumero(),
                monto
            ));
        }

        return pago;
    }

    private String validar(Cuenta origen, BigDecimal monto) {
        if (!origen.getActiva()) return "Cuenta origen inactiva";
        if (origen.getSaldo().compareTo(monto) < 0) return "Saldo insuficiente";
        if (monto.compareTo(origen.getLimiteDiario()) > 0) return "Excede límite diario";
        return null;
    }

    private void registrarIntento(Pago pago, String resultado, String detalle) {
        IntentoPago intento = new IntentoPago();
        intento.setPago(pago);
        intento.setResultado(resultado);
        intento.setDetalle(detalle);
        intentoRepo.save(intento);
    }

    public List<Pago> listarPagos() {
        return pagoRepo.findAllByOrderByCreadoEnDesc();
    }

    public Pago obtenerPago(Long id) {
        return pagoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + id));
    }

    public List<IntentoPago> intentosDePago(Long pagoId) {
        return intentoRepo.findByPagoIdOrderByTimestampAsc(pagoId);
    }

    public List<Cuenta> listarCuentas() {
        return cuentaRepo.findAll();
    }
}

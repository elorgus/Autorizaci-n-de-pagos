package com.ejemplo.pagos.controller;

import com.ejemplo.pagos.dto.*;
import com.ejemplo.pagos.entity.Cuenta;
import com.ejemplo.pagos.entity.IntentoPago;
import com.ejemplo.pagos.entity.Pago;
import com.ejemplo.pagos.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @PostMapping("/pagos")
    public ResponseEntity<PagoResponse> crearPago(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PagoRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        Pago pago = service.iniciarPago(idempotencyKey, req.numeroOrigen(), req.numeroDestino(), req.monto());
        return ResponseEntity.ok(toResponse(pago));
    }

    @GetMapping("/pagos")
    public List<PagoResponse> listarPagos() {
        return service.listarPagos().stream().map(this::toResponse).toList();
    }

    @GetMapping("/pagos/{id}")
    public PagoResponse obtenerPago(@PathVariable Long id) {
        return toResponse(service.obtenerPago(id));
    }

    @GetMapping("/pagos/{id}/intentos")
    public List<IntentoPagoResponse> intentos(@PathVariable Long id) {
        return service.intentosDePago(id).stream().map(this::toIntentoResponse).toList();
    }

    @GetMapping("/cuentas")
    public List<CuentaResponse> listarCuentas() {
        return service.listarCuentas().stream().map(this::toCuentaResponse).toList();
    }

    private PagoResponse toResponse(Pago p) {
        return new PagoResponse(
            p.getId(),
            p.getIdempotencyKey(),
            p.getCuentaOrigen().getNumero(),
            p.getCuentaDestino().getNumero(),
            p.getMonto(),
            p.getEstado().name(),
            p.getMensaje(),
            p.getCreadoEn()
        );
    }

    private CuentaResponse toCuentaResponse(Cuenta c) {
        return new CuentaResponse(c.getId(), c.getNumero(), c.getTitular(),
            c.getSaldo(), c.getLimiteDiario(), c.getActiva());
    }

    private IntentoPagoResponse toIntentoResponse(IntentoPago i) {
        return new IntentoPagoResponse(i.getId(), i.getResultado(), i.getDetalle(), i.getTimestamp());
    }
}

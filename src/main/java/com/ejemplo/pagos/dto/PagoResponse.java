package com.ejemplo.pagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
    Long id,
    String idempotencyKey,
    String cuentaOrigen,
    String cuentaDestino,
    BigDecimal monto,
    String estado,
    String mensaje,
    LocalDateTime creadoEn
) {}

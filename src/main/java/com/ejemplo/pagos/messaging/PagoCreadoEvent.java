package com.ejemplo.pagos.messaging;

import java.math.BigDecimal;

public record PagoCreadoEvent(
    Long pagoId,
    String cuentaOrigen,
    String cuentaDestino,
    BigDecimal monto
) {}

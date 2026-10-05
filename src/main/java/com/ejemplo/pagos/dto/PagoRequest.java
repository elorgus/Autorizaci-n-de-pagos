package com.ejemplo.pagos.dto;

import java.math.BigDecimal;

public record PagoRequest(
    String numeroOrigen,
    String numeroDestino,
    BigDecimal monto
) {}

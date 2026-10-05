package com.ejemplo.pagos.dto;

import java.math.BigDecimal;

public record CuentaResponse(
    Long id,
    String numero,
    String titular,
    BigDecimal saldo,
    BigDecimal limiteDiario,
    Boolean activa
) {}

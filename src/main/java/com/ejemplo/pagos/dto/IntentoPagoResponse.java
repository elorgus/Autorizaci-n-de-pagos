package com.ejemplo.pagos.dto;

import java.time.LocalDateTime;

public record IntentoPagoResponse(
    Long id,
    String resultado,
    String detalle,
    LocalDateTime timestamp
) {}

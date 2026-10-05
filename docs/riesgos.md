# Riesgos y mitigaciones

| # | Riesgo | Impacto | Probabilidad | Mitigacion |
|---|---|---|---|---|
| R1 | Doble cobro por reintento del cliente | Critico | Alta | Idempotency-Key + unique constraint en BD; devolver la respuesta original si ya existe |
| R2 | Timeout del banco externo deja pagos colgados | Alto | Media | Timeout configurable + estado EN_REVISION + reintentos con backoff + DLQ |
| R3 | Perdida de eventos si RabbitMQ cae | Alto | Media | Colas durables + mensajes persistentes + healthcheck + alerta si la cola crece |

## Detalle R1 - Doble cobro
**Mitigacion:** El cliente envia un `Idempotency-Key` (UUID) en el header. La tabla `pagos` tiene un constraint UNIQUE sobre `idempotency_key`. Si llega dos veces, la API devuelve el pago existente sin crear uno nuevo. Adicionalmente, el `PagoService.iniciarPago` verifica primero con SELECT, y luego protege con el constraint por si hay race conditions.

## Detalle R2 - Timeout del banco
**Mitigacion:** El consumer marca el pago como `EN_REVISION` cuando el banco no responde. Con `max-attempts: 3` y `multiplier: 2.0`, reintenta hasta 3 veces. Si agota los intentos, va a DLQ para revision manual.

## Detalle R3 - Perdida de eventos
**Mitigacion:** Colas durables + `persistent: true` en los mensajes. Healthcheck de RabbitMQ en Compose. Si la cola supera un umbral, alerta. La DLQ se monitorea manualmente.

# Metricas

## Metrica de negocio
**Tasa de pagos duplicados**

- Definicion: porcentaje de intentos de pago rechazados por idempotencia / total de intentos.
- Objetivo: 0% de duplicados reales (los intentos duplicados son OK y devuelven el mismo pago).
- Fuente: tabla `pagos` + logs de idempotencia.
- Accion: si hay duplicados reales, revisar el constraint UNIQUE y el flujo del cliente.

## Metrica tecnica
**Latencia p95 de POST /api/v1/pagos**

- Definicion: percentil 95 del tiempo de respuesta del endpoint de creacion de pago.
- Objetivo: menor a 500 ms (sin contar la llamada al banco, que es asincrona).
- Fuente: Actuator + Micrometer, logs de Spring.
- Accion: si supera, revisar indices en `idempotency_key`, pool de conexiones, o cachear validaciones.

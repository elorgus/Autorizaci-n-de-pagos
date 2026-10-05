# ADR-001: Monolito modular con procesamiento asincrono via cola

## Estado
Aceptada

## Contexto
El sistema debe autorizar pagos desde una app movil. Requisitos clave:
- Validar cuenta, limites e idempotencia de forma sincrona (respuesta inmediata).
- Llamar a un banco externo que puede tardar segundos o fallar.
- Registrar cada intento incluso si el banco falla.
- Evitar cobros duplicados si el cliente reintenta.

El volumen esperado no requiere microservicios en la primera version.

## Decision
Implementar un **monolito modular** con:
- API REST sincrona para validacion + registro + idempotencia.
- Cola RabbitMQ para desacoplar la llamada al banco externo.
- Consumer asincrono con reintentos con backoff y DLQ.
- Estado intermedio EN_REVISION para timeouts.

Modulos internos:
- Cuentas
- Pagos
- Idempotencia
- Banco externo (cliente)
- Conciliacion

## Consecuencias
**Positivas:**
- Respuesta rapida al cliente (no espera al banco).
- Si el banco cae, el pago queda en cola o DLQ, no se pierde.
- Auditoria de cada intento.
- Facil extraer el modulo de pagos a microservicio en el futuro.

**Negativas:**
- Mayor complejidad operativa (mas contenedores).
- Necesita monitoreo de la cola y DLQ.
- El cliente debe consultar el estado final (no es sincrono).

**Mitigaciones:**
- Endpoint GET /pagos/{id} para polling.
- Notificacion push al cliente cuando cambia el estado (futuro).
- Dashboard de DLQ.

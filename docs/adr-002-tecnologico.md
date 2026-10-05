# ADR-002: Stack Spring Boot + PostgreSQL + RabbitMQ + OpenAPI

## Estado
Aceptada

## Contexto
El sistema necesita:
- API REST con contrato claro para la app movil.
- Persistencia transaccional con constraints de idempotencia.
- Mensajeria con reintentos y DLQ.
- Facil integracion con el banco externo via HTTP.

## Decision
- **Spring Boot 3.4 + Java 21**: ecosistema maduro, Spring AMQP, Actuator.
- **PostgreSQL 16**: unique constraint en idempotency_key, ACID.
- **RabbitMQ 3.13**: colas durables, DLQ, reintentos con backoff.
- **OpenAPI (springdoc)**: contrato para la app movil.
- **Docker Compose**: stack reproducible.
- **Idempotency-Key en header**: UUID generado por el cliente.

## Consecuencias
**Positivas:**
- El contrato OpenAPI permite generar SDKs para Android/iOS.
- El constraint unique en BD garantiza idempotencia incluso con race conditions.
- Reintentos configurables en Spring AMQP.

**Negativas:**
- RabbitMQ requiere operacion (backups, monitoring).
- Spring Boot es pesado en memoria comparado con alternativas.

**Alternativas consideradas:**
- Kafka: overkill para el volumen; RabbitMQ es mas simple.
- Node.js + BullMQ: descartado por menor madurez en transacciones ACID.
- REST sincrono al banco: descartado por timeouts y necesidad de reintentos.

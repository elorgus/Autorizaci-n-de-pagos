# App 2 — Autorización de pagos

Sistema para una cooperativa que recibe pagos desde su aplicación móvil. Valida cuenta, verifica límites, registra cada intento (incluso si el banco falla) y garantiza idempotencia.

## 📑 Entregable

| Sección | Enlace |
|---|---|
| Objetivo, actores y alcance | [§1](#1-objetivo-actores-y-alcance) |
| Requisitos funcionales y de calidad | [§2](#2-requisitos) |
| Diagrama C4 contexto | [docs/c4-contexto.mmd](docs/c4-contexto.mmd) |
| Diagrama C4 contenedores | [docs/c4-contenedores.mmd](docs/c4-contenedores.mmd) |
| Flujo de operación crítica | [docs/flujo-critico.mmd](docs/flujo-critico.mmd) |
| Stack con justificación | [§3](#3-stack-propuesto) |
| ADRs | [ADR-001](docs/adr-001-arquitectonico.md), [ADR-002](docs/adr-002-tecnologico.md) |
| Riesgos | [docs/riesgos.md](docs/riesgos.md) |
| Métricas | [docs/metricas.md](docs/metricas.md) |

---

## 1. Objetivo, actores y alcance

### Objetivo
Autorizar pagos móviles validando la cuenta, los límites y registrando cada intento. Debe evitar cobros duplicados incluso si el cliente reintenta o el banco externo falla.

### Actores
- **Cliente móvil** — inicia el pago desde la app.
- **Operador de cooperativa** — revisa conciliación y DLQ.
- **Banco externo** — autoriza o rechaza el pago (sistema externo).
- **Proveedor de notificaciones** — informa al cliente del resultado.

### Alcance
**Dentro:**
- Validación síncrona: cuenta existe, activa, saldo, límite diario.
- Idempotencia vía `Idempotency-Key` en header.
- Llamada asíncrona al banco externo vía RabbitMQ.
- Reintentos con backoff exponencial y DLQ.
- Auditoría de cada intento de pago.

**Fuera:**
- Conciliación contable formal (se audita, no se contabiliza).
- Facturación electrónica.
- Multi-moneda.

---

## 2. Requisitos

### Funcionales
| ID | Requisito |
|---|---|
| RF-01 | Validar cuenta origen, saldo y límite diario antes de aceptar el pago |
| RF-02 | Garantizar idempotencia con `Idempotency-Key` |
| RF-03 | Publicar evento `pago.creado` a RabbitMQ |
| RF-04 | Consumer llama al banco externo con timeout configurable |
| RF-05 | Reintentar con backoff exponencial (2s, 4s, 8s) hasta 3 veces |
| RF-06 | Mover a DLQ si agota reintentos |
| RF-07 | Registrar cada intento en tabla `intentos_pago` |
| RF-08 | Endpoint `GET /api/v1/pagos/{id}` para consultar estado |

### De calidad
| ID | Requisito |
|---|---|
| RNF-01 | Health check `/actuator/health` con DB y broker |
| RNF-02 | Latencia p95 de `POST /pagos` menor a 500 ms |
| RNF-03 | Frontend responsive con auto-refresh |
| RNF-04 | Docker Compose multi-servicio reproducible |

---

## 3. Stack propuesto

| Capa | Tecnología | Justificación |
|---|---|---|
| Backend | Spring Boot 3.4 (Java 21) | Ecosistema maduro, Spring AMQP, Actuator |
| BD | PostgreSQL 16 | ACID, unique constraint para idempotencia |
| Mensajería | RabbitMQ 3.13 | Colas durables, DLQ, reintentos con backoff |
| Contrato | OpenAPI (springdoc) | SDK para Android/iOS |
| Frontend | HTML/CSS/JS vanilla | Demo funcional sin build step |
| Contenedores | Docker + Compose | Multi-servicio reproducible |

Detalle completo en [ADR-002](docs/adr-002-tecnologico.md).

---

## 4. Ejecución rápida

```bash
docker compose up -d --build
# http://localhost:8081

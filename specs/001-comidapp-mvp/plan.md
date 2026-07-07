# Implementation Plan: ComidApp MVP — Pedidos y Entrega Online

**Branch**: `001-comidapp-mvp` | **Date**: 2026-06-09 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-comidapp-mvp/spec.md` (aprobado 100%, sesión de clarificación del 2026-06-09)

---

## Summary

ComidApp es una plataforma web full-stack de pedidos y entrega de comida rápida para una empresa de hamburguesas con 5 locales físicos. El sistema permite a los usuarios explorar menús por local, gestionar un carrito de compras (restringido a un local por sesión), confirmar pedidos con pago por tarjeta (pasarela externa) o efectivo (confirmado por el repartidor), y hacer seguimiento del estado del pedido con notificaciones Web Push.

El approach técnico adopta arquitectura en capas (dominio / aplicación / infraestructura / API) en el backend Java + Spring Boot, y una SPA React+Vite mobile-first en el frontend, con MySQL como base de datos relacional y comunicación via REST+JWT.

---

## Technical Context

**Language/Version**: Java 17 (backend), TypeScript 5.x (frontend)

**Backend Framework**: Spring Boot 3.x + Spring Web + Spring Security

**Frontend Framework**: React 19 + Vite 5

**ORM**: JPA / Hibernate (entidades anotadas, migraciones SQL versionadas)

**Storage**: MySQL 8.0 (esquema normalizado, índices explícitos)

**Testing**: JUnit 5 + Mockito (unitarios) + Spring Boot Test + MockMvc (integración)

**Target Platform**: Web responsive (mobile-first), sin app nativa

**Performance Goals**: LCP < 2,5 s en 4G simulado; respuesta API < 3 s con 500 usuarios activos simultáneos

**Constraints**: PCI-DSS delegado a pasarela; sin GPS, IA, wearables. Carrito restringido a un local. Pedidos solo en horario de atención. Timeout de 1 h en estado RECIBIDO.

**Scale/Scope**: ~500 clientes activos; 5 locales fijos; 13 historias de usuario; FR-001→FR-033

---

## Constitution Check

*GATE: Debe pasar antes de la Fase 0. Re-verificar después del diseño en Fase 1.*

| Principio | Gate | Estado |
|-----------|------|--------|
| **P1 — La Especificación Manda** | Toda tarea se traza a FR-xxx o HU-xx de spec.md | ✅ Trazabilidad mantenida en data-model.md y contracts/ |
| **P2 — Dominio OO Real** | `Pedido`, `Carrito`, `Local` encapsulan reglas propias. Sin clases anémicas. | ✅ Verificado en data-model.md: Pedido.puedeAvanzar(), Carrito.calcularTotal(), etc. |
| **P3 — Separación de Capas** | Controllers sin lógica de negocio. Repos abstraen persistencia. | ✅ Estructura backend: `src/domain/`, `src/application/`, `src/infrastructure/`, `src/api/` |
| **P4 — Código Backend Limpio** | Sin `instanceof` para ramificación. Métodos < 40 líneas. Validaciones fuera de controllers. | ✅ Validaciones en capa de servicio. Polimorfismo vía enums Java. |
| **P5 — Enums para Tipos Cerrados** | `EstadoPedido`, `MetodoPago`, `EstadoPago`, `RolUsuario` como enums Java. | ✅ Definidos en `com.comidapp.domain.enums` |
| **P6 — Patrones Solo si Simplifican** | No se fuerzan patrones. Repository pattern justificado para abstraer JPA. | ✅ Sin over-engineering |
| **P7 — Tests para Reglas Críticas** | Tests obligatorios: carrito un local, disponibilidad al confirmar, transición de estados, control de acceso. | ✅ Cubiertos en fases de testing |
| **P8 — Índices MySQL** | Índices en `pedido.cliente_id`, `pedido.estado`, `usuario.email`, `item_menu.local_id`. | ✅ Definidos en data-model.md y migraciones SQL |
| **P9 — Mobile-First + LCP < 2,5 s** | CSS mobile-first, lazy loading por ruta, imágenes optimizadas, code splitting Vite. | ✅ Aplicado en estructura frontend |
| **P10 — No Implementar en Spec** | Solo documentos en esta fase. | ✅ Sin código |

---

## Project Structure

### Documentation (this feature)

```text
specs/001-comidapp-mvp/
├── plan.md              ← este archivo
├── research.md          ← decisiones técnicas: pasarela, Web Push, índices
├── data-model.md        ← esquema MySQL con tablas, columnas, PK/FK e índices
├── quickstart.md        ← levantar el proyecto localmente desde cero
├── contracts/
│   └── openapi.yaml     ← contratos REST de todos los endpoints
└── checklists/
    └── requirements.md  ← checklist de calidad (aprobado 16/16)
```

### Source Code (repository root — estructura objetivo post-implementación)

```text
backend/
├── build.gradle                       ← configuración Gradle + dependencias
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/comidapp/
│   │   │   ├── domain/
│   │   │   │   ├── enums/
│   │   │   │   │   ├── EstadoPedido.java
│   │   │   │   │   ├── MetodoPago.java
│   │   │   │   │   ├── EstadoPago.java
│   │   │   │   │   └── RolUsuario.java
│   │   │   │   ├── entities/
│   │   │   │   │   ├── Pedido.java     ← reglas: puedeAvanzar(), cancelar()
│   │   │   │   │   ├── Carrito.java    ← reglas: calcularTotal(), validarLocal()
│   │   │   │   │   ├── Local.java      ← reglas: estaAbierto()
│   │   │   │   │   └── ItemMenu.java   ← reglas: estaDisponible()
│   │   │   │   └── exceptions/
│   │   │   │       └── DomainException.java
│   │   │   ├── application/
│   │   │   │   ├── pedidos/
│   │   │   │   │   ├── ConfirmarPedidoUseCase.java
│   │   │   │   │   ├── ActualizarEstadoUseCase.java
│   │   │   │   │   └── CancelarPedidoVencidoUseCase.java
│   │   │   │   ├── pagos/
│   │   │   │   │   └── ProcesarPagoUseCase.java
│   │   │   │   ├── carrito/
│   │   │   │   │   └── GestionarCarritoUseCase.java
│   │   │   │   └── notificaciones/
│   │   │   │       └── EnviarNotificacionUseCase.java
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── JpaPedidoRepository.java
│   │   │   │   │   ├── JpaCarritoRepository.java
│   │   │   │   │   ├── JpaProductoRepository.java
│   │   │   │   │   └── JpaUsuarioRepository.java
│   │   │   │   ├── payment/
│   │   │   │   │   └── MercadoPagoAdapter.java
│   │   │   │   ├── notifications/
│   │   │   │   │   └── WebPushService.java
│   │   │   │   └── jobs/
│   │   │   │       └── CancelacionVencidosJob.java
│   │   │   └── api/
│   │   │       ├── controllers/       ← solo delegan a use cases, sin lógica de negocio
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── MenuController.java
│   │   │       │   ├── CarritoController.java
│   │   │       │   ├── PedidoController.java
│   │   │       │   ├── PagoController.java
│   │   │       │   ├── AdminController.java
│   │   │       │   └── NotificacionController.java
│   │   │       ├── dto/               ← request/response DTOs con Jakarta Validation
│   │   │       └── config/
│   │   │           ├── SecurityConfig.java
│   │   │           └── WebConfig.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/         ← migraciones SQL versionadas (Flyway)
│   └── test/
│       └── java/com/comidapp/
│           ├── domain/
│           ├── application/
│           └── integration/
└── docker-compose.yml

frontend/
├── src/
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── LocalesPage.tsx        ← selección del local
│   │   ├── MenuPage.tsx           ← menú del local + carrito lateral
│   │   ├── ConfirmacionPage.tsx   ← resumen + pago
│   │   ├── SeguimientoPage.tsx    ← estado del pedido en tiempo real
│   │   ├── HistorialPage.tsx      ← paginado, 10 por página
│   │   ├── PerfilPage.tsx
│   │   ├── AdminPage.tsx          ← gestión productos por local
│   │   └── RepartidorPage.tsx     ← confirmación entrega en efectivo
│   ├── components/
│   │   ├── carrito/
│   │   ├── menu/
│   │   ├── pedido/
│   │   └── shared/
│   ├── context/
│   │   ├── AuthContext.tsx        ← ya implementado
│   │   └── CarritoContext.tsx
│   ├── hooks/
│   │   ├── useCarrito.ts
│   │   ├── usePedido.ts
│   │   └── useWebPush.ts
│   ├── services/
│   │   └── api.ts                 ← cliente HTTP centralizado
│   └── types/
│       └── enums.ts               ← espejo de enums del backend
└── package.json
```

---

## Complexity Tracking

| Decisión | Justificación | Alternativa rechazada |
|----------|--------------|----------------------|
| JPA/Hibernate como ORM | Estándar de la industria Java, integración nativa con Spring Boot, migraciones SQL versionadas | MyBatis: más control SQL pero más verboso, sin generación automática de schema |
| JUnit 5 + Mockito para tests | Framework estándar Java, integración nativa con Spring Boot Test, MockMvc para endpoints | TestNG: menos integración con Spring ecosystem |
| Repository Pattern en infra | Abstrae JPA para facilitar testing con mocks y separación de capas | Acceso directo a EntityManager desde servicios: acopla infra al dominio |
| Web Push via VAPID | No requiere servicio externo pago; estándar W3C; funciona con app cerrada | Email/SMS: costo operativo, dependencia externa adicional |
| Jakarta Validation para DTOs | Estándar Java, anotaciones declarativas, integración automática con Spring | Validación manual: código duplicado, propenso a errores |

---

## Traceability Matrix

| Historia de Usuario | FRs Asociados | Componentes Backend | Componentes Frontend |
|--------------------|---------------|---------------------|----------------------|
| HU1 — Registro | FR-001 | AuthController, UsuarioUseCase | RegisterPage |
| HU2 — Login/Logout | FR-002, FR-003 | AuthController, JwtService | LoginPage, AuthContext |
| HU3 — Menú por local | FR-004, FR-005, FR-006, FR-033 | MenuController, ProductoRepository | LocalesPage, MenuPage |
| HU4 — Carrito | FR-007, FR-007b, FR-007c, FR-008, FR-009, FR-010, FR-011 | CarritoController, GestionarCarritoUseCase | MenuPage, CarritoContext |
| HU5 — Confirmar pedido | FR-011, FR-012, FR-013 | PedidoController, ConfirmarPedidoUseCase | ConfirmacionPage |
| HU6 — Pago con tarjeta | FR-014, FR-017b | PagoController, ProcesarPagoUseCase, MercadoPagoAdapter | ConfirmacionPage |
| HU7 — Pago en efectivo | FR-015, FR-016 | PagoController, RepartidorController | RepartidorPage |
| HU8 — Seguimiento estado | FR-017, FR-017b, FR-019, FR-032 | PedidoController, ActualizarEstadoUseCase, CancelarPedidoVencidoUseCase | SeguimientoPage |
| HU9 — Perfil e historial | FR-020, FR-021 | UsuarioController | HistorialPage, PerfilPage |
| HU10 — Notificaciones | FR-018 | WebPushService, NotificacionController | useWebPush hook |
| HU11 — Repartidor | FR-016, FR-029, FR-030 | RepartidorController | RepartidorPage |
| HU12 — Admin catálogo | FR-022, FR-023, FR-024, FR-025 | AdminController, ProductoRepository | AdminPage |
| HU13 — Admin usuarios | FR-026, FR-027, FR-028, FR-031 | AdminController, UsuarioRepository | AdminPage |

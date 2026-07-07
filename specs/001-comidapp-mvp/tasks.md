# ComidApp MVP — Lista de Tareas de Implementación

**Feature**: `001-comidapp-mvp` | **Fecha**: 2026-06-19  
**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

> Este plan está alineado con backend **Java 21 + Spring Boot + JPA/Hibernate** y frontend **React + TypeScript**.

---

## — BACKEND —

### Fase 1 — Setup e Infraestructura Base

#### T001 — Inicializar proyecto Spring Boot en `backend/`
- **Archivos**: `backend/build.gradle`, `backend/settings.gradle`, `backend/.gitignore`, `backend/src/main/resources/application.properties`
- **FR**: todos (prerequisito)
- **Depende de**: —
- **Descripción**: Crear proyecto base Spring Boot 4 + Java 21 con dependencias web, security, data-jpa, validation, mysql, jwt, openapi, test.

#### T002 — Configurar estructura de paquetes y capas [P]
- **Archivos**: `backend/src/main/java/com/comidapp/**`
- **FR**: —
- **Depende de**: T001
- **Descripción**: Crear paquetes por capas: domain, application, infrastructure, api.

#### T003 — Configurar JUnit 5, Mockito, H2 y JaCoCo [P]
- **Archivos**: `backend/build.gradle`, `backend/src/test/resources/application-test.properties`
- **FR**: —
- **Depende de**: T001
- **Descripción**: Dejar pipeline de test listo con cobertura.

### Fase 2 — Dominio y Entidades

#### T004 — Crear enums de dominio [P]
- **Archivos**: `backend/src/main/java/com/comidapp/domain/enums/*.java`
- **FR**: FR-013, FR-014, FR-017
- **Depende de**: T002
- **Descripción**: Definir `EstadoPedido`, `MetodoPago`, `EstadoPago`, `RolUsuario`.

#### T005 — Crear excepciones de dominio [P]
- **Archivos**: `backend/src/main/java/com/comidapp/domain/exceptions/*.java`
- **FR**: FR-007, FR-011, FR-012, FR-033
- **Depende de**: T002
- **Descripción**: Excepciones tipadas para reglas críticas.

#### T006 — Crear entidad `Pedido.java`
- **Archivos**: `backend/src/main/java/com/comidapp/domain/entities/Pedido.java`
- **FR**: FR-017, FR-017b, FR-032
- **Depende de**: T004, T005
- **Descripción**: Entidad JPA con reglas `puedeAvanzar`, `cancelar`, `estaVencido`.

#### T007 — Crear entidad `Carrito.java`
- **Archivos**: `backend/src/main/java/com/comidapp/domain/entities/Carrito.java`, `ItemCarrito.java`
- **FR**: FR-007, FR-007b, FR-007c, FR-008, FR-009, FR-010
- **Depende de**: T004, T005
- **Descripción**: Restricción un solo local, cálculo de total, validación para confirmar.

#### T008 — Crear entidad `Local.java` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/domain/entities/Local.java`, `HorarioLocal.java`
- **FR**: FR-004, FR-033
- **Depende de**: T004, T005
- **Descripción**: Horarios y lógica `estaAbierto`.

#### T009 — Crear entidad `ItemMenu.java` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/domain/entities/ItemMenu.java`, `Producto.java`
- **FR**: FR-005, FR-006, FR-007c
- **Depende de**: T004
- **Descripción**: Catálogo por local, disponibilidad y precio.

#### T010 — Crear entidades complementarias [P]
- **Archivos**: `Usuario.java`, `Pago.java`, `ItemPedido.java`, `HistorialEstadoPedido.java`, `SuscripcionPush.java`
- **FR**: FR-001, FR-002, FR-014, FR-018
- **Depende de**: T004
- **Descripción**: Completar modelo de dominio JPA.

### Fase 3 — Base de Datos

#### T011 — Crear migración SQL inicial
- **Archivos**: `backend/src/main/resources/db/migration/V1__init.sql`
- **FR**: todos
- **Depende de**: T004, T006, T007, T008, T009, T010
- **Descripción**: Crear 12 tablas y sus índices.

### Fase 4 — Repositorios

#### T012 — Crear `UsuarioRepository` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/infrastructure/persistence/UsuarioRepository.java`
- **FR**: FR-001, FR-002, FR-021, FR-026, FR-027, FR-028
- **Depende de**: T010, T011
- **Descripción**: Interface JpaRepository y queries por email/rol.

#### T013 — Crear `ProductoRepository` + `ItemMenuRepository` [P]
- **Archivos**: `ProductoRepository.java`, `ItemMenuRepository.java`
- **FR**: FR-004, FR-005, FR-006, FR-022, FR-023, FR-024, FR-025
- **Depende de**: T009, T011
- **Descripción**: Queries de menú, disponibilidad y CRUD admin.

#### T014 — Crear `PedidoRepository` [P]
- **Archivos**: `PedidoRepository.java`
- **FR**: FR-011, FR-017, FR-019, FR-020, FR-029, FR-030, FR-031, FR-032
- **Depende de**: T006, T011
- **Descripción**: Historial, estado, asignación repartidor, vencidos.

#### T015 — Crear `CarritoRepository` [P]
- **Archivos**: `CarritoRepository.java`, `ItemCarritoRepository.java`
- **FR**: FR-007, FR-008, FR-009, FR-010
- **Depende de**: T007, T011
- **Descripción**: Lectura/escritura de carrito e ítems.

#### T016 — Crear repositorios restantes [P]
- **Archivos**: `LocalRepository.java`, `PagoRepository.java`, `SuscripcionPushRepository.java`, `HistorialEstadoPedidoRepository.java`
- **FR**: FR-004, FR-014, FR-018, FR-033
- **Depende de**: T008, T010, T011
- **Descripción**: Persistencia auxiliar.

### Fase 5 — Use Cases

#### T017 — Crear `GestionarCarritoUseCase`
- **Archivos**: `backend/src/main/java/com/comidapp/application/carrito/GestionarCarritoUseCase.java`
- **FR**: FR-007, FR-007b, FR-007c, FR-008, FR-009, FR-010
- **Depende de**: T007, T013, T015
- **Descripción**: Orquestación carrito y disponibilidad en vivo.

#### T018 — Crear `ConfirmarPedidoUseCase`
- **Archivos**: `backend/src/main/java/com/comidapp/application/pedidos/ConfirmarPedidoUseCase.java`
- **FR**: FR-011, FR-012, FR-013, FR-033
- **Depende de**: T006, T007, T008, T009, T014, T015, T017
- **Descripción**: Confirmación con validación completa y creación de pago.

#### T019 — Crear `ActualizarEstadoUseCase`
- **Archivos**: `backend/src/main/java/com/comidapp/application/pedidos/ActualizarEstadoUseCase.java`
- **FR**: FR-017, FR-017b, FR-029, FR-030
- **Depende de**: T006, T014
- **Descripción**: Validar transición, permisos y notificación.

#### T020 — Crear `CancelarPedidoVencidoUseCase`
- **Archivos**: `backend/src/main/java/com/comidapp/application/pedidos/CancelarPedidoVencidoUseCase.java`
- **FR**: FR-032
- **Depende de**: T006, T014, T022
- **Descripción**: Cancelación automática de pedidos vencidos.

#### T021 — Crear `ProcesarPagoUseCase`
- **Archivos**: `backend/src/main/java/com/comidapp/application/pagos/ProcesarPagoUseCase.java`
- **FR**: FR-014, FR-017b
- **Depende de**: T014, T019
- **Descripción**: Inicio de pago, webhook y reintento.

#### T022 — Crear `EnviarNotificacionUseCase`
- **Archivos**: `backend/src/main/java/com/comidapp/application/notificaciones/EnviarNotificacionUseCase.java`
- **FR**: FR-018
- **Depende de**: T012, T016
- **Descripción**: Suscripción/desuscripción y envío push.

### Fase 6 — Integraciones

#### T023 — Crear `MercadoPagoAdapter` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/infrastructure/payment/MercadoPagoAdapter.java`
- **FR**: FR-014, FR-017b
- **Depende de**: T001, T004
- **Descripción**: Integración SDK MP (preferencias, consulta pago, firma webhook).

#### T024 — Crear `WebPushService` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/infrastructure/notifications/WebPushService.java`
- **FR**: FR-018
- **Depende de**: T001
- **Descripción**: Push VAPID en Java.

### Fase 7 — API REST

#### T025 — Configurar aplicación, CORS y exception handler
- **Archivos**: `ComidappApplication.java`, `WebConfig.java`, `GlobalExceptionHandler.java`
- **FR**: todos
- **Depende de**: T002, T005
- **Descripción**: Boot app + manejo global de errores.

#### T026 — Configurar Spring Security + JWT
- **Archivos**: `SecurityConfig.java`, `JwtAuthFilter.java`, `JwtService.java`
- **FR**: FR-002, FR-003
- **Depende de**: T025, T012
- **Descripción**: Autenticación y autorización por rol.

#### T027 — Crear DTOs y validación Jakarta
- **Archivos**: `backend/src/main/java/com/comidapp/api/dto/*.java`
- **FR**: FR-001, FR-007, FR-011, FR-017, FR-025
- **Depende de**: T004, T026
- **Descripción**: Requests/responses tipados con Bean Validation.

#### T028 — Crear `AuthController`
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/AuthController.java`
- **FR**: FR-001, FR-002, FR-003
- **Depende de**: T012, T026, T027
- **Descripción**: Registro/login/logout.

#### T029 — Crear `MenuController` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/MenuController.java`
- **FR**: FR-004, FR-005, FR-006, FR-033
- **Depende de**: T013, T016, T026
- **Descripción**: Locales, menú y disponibilidad.

#### T030 — Crear `CarritoController`
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/CarritoController.java`
- **FR**: FR-007, FR-007b, FR-007c, FR-008, FR-009, FR-010
- **Depende de**: T017, T026, T027
- **Descripción**: CRUD de carrito.

#### T031 — Crear `PedidoController`
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/PedidoController.java`
- **FR**: FR-011, FR-012, FR-013, FR-017, FR-019, FR-020, FR-029, FR-030, FR-031
- **Depende de**: T018, T019, T014, T026, T027
- **Descripción**: Confirmación, estado, historial y panel repartidor.

#### T032 — Crear `PagoController`
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/PagoController.java`
- **FR**: FR-014, FR-017b
- **Depende de**: T021, T023, T026
- **Descripción**: Webhook + reintento de pago.

#### T033 — Crear `AdminController`
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/AdminController.java`
- **FR**: FR-022, FR-023, FR-024, FR-025, FR-026, FR-027, FR-028
- **Depende de**: T012, T013, T026, T027
- **Descripción**: Gestión catálogo y usuarios.

#### T034 — Crear `NotificacionController` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/NotificacionController.java`
- **FR**: FR-018
- **Depende de**: T022, T024, T026, T027
- **Descripción**: Endpoints de suscripción push.

#### T035 — Crear `UsuarioController` [P]
- **Archivos**: `backend/src/main/java/com/comidapp/api/controllers/UsuarioController.java`
- **FR**: FR-020, FR-021
- **Depende de**: T012, T026, T027
- **Descripción**: Perfil de usuario.

#### T036 — Crear job `CancelacionVencidosJob`
- **Archivos**: `backend/src/main/java/com/comidapp/infrastructure/jobs/CancelacionVencidosJob.java`
- **FR**: FR-032
- **Depende de**: T020, T025
- **Descripción**: `@Scheduled` cada 5 minutos.

### Fase 8 — Tests Dominio

#### T037 — Tests unitarios de `Pedido.java` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/domain/PedidoTest.java`
- **FR**: FR-017, FR-017b, FR-032
- **Depende de**: T006
- **Descripción**: Transiciones válidas/inválidas y vencimiento.

#### T038 — Tests unitarios de `Carrito.java` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/domain/CarritoTest.java`
- **FR**: FR-007, FR-007b, FR-007c
- **Depende de**: T007
- **Descripción**: Total, conflicto de local y validación confirmación.

#### T039 — Tests unitarios de `Local.java` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/domain/LocalTest.java`
- **FR**: FR-033
- **Depende de**: T008
- **Descripción**: Abierto/cerrado y límites horarios.

#### T040 — Tests unitarios de `ItemMenu.java` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/domain/ItemMenuTest.java`
- **FR**: FR-007c
- **Depende de**: T009
- **Descripción**: `estaDisponible()`.

### Fase 9 — Tests Use Cases

#### T041 — Tests `GestionarCarritoUseCase` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/application/GestionarCarritoUseCaseTest.java`
- **FR**: FR-007, FR-007b, FR-007c
- **Depende de**: T017, T038
- **Descripción**: Flujo completo con mocks de repositorios.

#### T042 — Tests `ConfirmarPedidoUseCase` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/application/ConfirmarPedidoUseCaseTest.java`
- **FR**: FR-011, FR-012, FR-013, FR-033
- **Depende de**: T018, T041
- **Descripción**: Confirmación efectivo/tarjeta y errores.

#### T043 — Tests `ActualizarEstadoUseCase` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/application/ActualizarEstadoUseCaseTest.java`
- **FR**: FR-017, FR-017b, FR-029
- **Depende de**: T019, T037
- **Descripción**: Transición, permisos y cobro efectivo.

#### T044 — Tests `CancelarPedidoVencidoUseCase` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/application/CancelarPedidoVencidoUseCaseTest.java`
- **FR**: FR-032
- **Depende de**: T020, T037
- **Descripción**: Cancelación automática de vencidos.

### Fase 10 — Tests Integración API

#### T045 — Tests integración `AuthController` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/integration/AuthControllerIT.java`
- **FR**: FR-001, FR-002, FR-003
- **Depende de**: T028, T003, T011
- **Descripción**: Registro/login/logout y auth.

#### T046 — Tests integración `MenuController` [P]
- **Archivos**: `backend/src/test/java/com/comidapp/integration/MenuControllerIT.java`
- **FR**: FR-004, FR-005, FR-006
- **Depende de**: T029, T003, T011
- **Descripción**: Locales/menú/disponibilidad.

#### T047 — Tests integración `CarritoController`
- **Archivos**: `backend/src/test/java/com/comidapp/integration/CarritoControllerIT.java`
- **FR**: FR-007, FR-007b, FR-007c, FR-008, FR-009, FR-010
- **Depende de**: T030, T003, T011
- **Descripción**: CRUD carrito y conflicto local.

#### T048 — Tests integración `PedidoController` + `PagoController`
- **Archivos**: `backend/src/test/java/com/comidapp/integration/PedidoPagoControllerIT.java`
- **FR**: FR-011, FR-012, FR-013, FR-014, FR-017, FR-017b, FR-033
- **Depende de**: T031, T032, T003, T011
- **Descripción**: Confirmación, webhook y permisos.

#### T049 — Tests integración `AdminController`
- **Archivos**: `backend/src/test/java/com/comidapp/integration/AdminControllerIT.java`
- **FR**: FR-022, FR-026, FR-027, FR-028
- **Depende de**: T033, T003, T011
- **Descripción**: Gestión admin por rol.

---

## — FRONTEND —

### Fase 11 — Setup Frontend

#### T050 — Inicializar proyecto React + Vite + TypeScript en `frontend/`
- **Archivos**: `frontend/package.json`, `frontend/tsconfig.json`, `frontend/vite.config.ts`, `frontend/.env.example`, `frontend/index.html`
- **FR**: todos
- **Depende de**: —
- **Descripción**: Bootstrap del frontend.

#### T051 — Configurar router, lazy loading y estructura [P]
- **Archivos**: `frontend/src/main.tsx`, `frontend/src/App.tsx`, `frontend/src/router.tsx`
- **FR**: P9
- **Depende de**: T050
- **Descripción**: Estructura y ruteo base.

#### T052 — Crear `frontend/public/sw.js` [P]
- **Archivos**: `frontend/public/sw.js`
- **FR**: FR-018
- **Depende de**: T050
- **Descripción**: Service worker push.

### Fase 12 — Páginas y Componentes

#### T053 — Crear `LoginPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/LoginPage.tsx`
- **FR**: FR-002
- **Depende de**: T051, T063, T065
- **Descripción**: Adaptar login existente al nuevo contrato.

#### T054 — Crear `RegisterPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/RegisterPage.tsx`
- **FR**: FR-001
- **Depende de**: T051, T065
- **Descripción**: Registro sin DNI.

#### T055 — Crear `LocalesPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/LocalesPage.tsx`
- **FR**: FR-004, FR-033
- **Depende de**: T051, T065, T069
- **Descripción**: Selección local desde API.

#### T056 — Crear `MenuPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/MenuPage.tsx`, `frontend/src/components/menu/ItemMenuCard.tsx`, `frontend/src/components/carrito/CarritoLateral.tsx`
- **FR**: FR-005, FR-006, FR-007c
- **Depende de**: T051, T064, T065, T066, T069
- **Descripción**: Menú y carrito lateral.

#### T057 — Crear `ConfirmacionPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/ConfirmacionPage.tsx`
- **FR**: FR-011, FR-012, FR-013, FR-014
- **Depende de**: T051, T064, T065, T069
- **Descripción**: Confirmación y pago.

#### T058 — Crear `SeguimientoPage.tsx`
- **Archivos**: `frontend/src/pages/SeguimientoPage.tsx`, `frontend/src/components/pedido/LineaTiempo.tsx`
- **FR**: FR-017, FR-019
- **Depende de**: T051, T065, T067, T069
- **Descripción**: Seguimiento con polling.

#### T059 — Crear `HistorialPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/HistorialPage.tsx`
- **FR**: FR-020
- **Depende de**: T051, T065, T069
- **Descripción**: Historial paginado.

#### T060 — Crear `PerfilPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/PerfilPage.tsx`
- **FR**: FR-021
- **Depende de**: T051, T065, T069
- **Descripción**: Perfil usuario.

#### T061 — Crear `AdminPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/AdminPage.tsx`, `frontend/src/components/shared/TabNav.tsx`
- **FR**: FR-022, FR-023, FR-024, FR-025, FR-026, FR-027, FR-028
- **Depende de**: T051, T065, T069
- **Descripción**: Panel admin por local.

#### T062 — Crear `RepartidorPage.tsx` [PARTIAL]
- **Archivos**: `frontend/src/pages/RepartidorPage.tsx`
- **FR**: FR-016, FR-029, FR-030
- **Depende de**: T051, T065, T069
- **Descripción**: Panel repartidor.

### Fase 13 — Contextos, Hooks y API Client

#### T063 — Crear `AuthContext.tsx` [PARTIAL]
- **Archivos**: `frontend/src/context/AuthContext.tsx`
- **FR**: FR-002, FR-003
- **Depende de**: T051, T069
- **Descripción**: Auth con JWT claim `id`.

#### T064 — Crear `CarritoContext.tsx`
- **Archivos**: `frontend/src/context/CarritoContext.tsx`
- **FR**: FR-007, FR-007b, FR-007c, FR-008, FR-009, FR-010
- **Depende de**: T051, T065, T069
- **Descripción**: Estado global carrito.

#### T065 — Crear `frontend/src/services/api.ts` [PARTIAL]
- **Archivos**: `frontend/src/services/api.ts`
- **FR**: todos
- **Depende de**: T051, T063
- **Descripción**: Cliente HTTP centralizado por recursos.

#### T066 — Crear `useCarrito.ts`
- **Archivos**: `frontend/src/hooks/useCarrito.ts`
- **FR**: FR-007c, FR-011
- **Depende de**: T064, T065
- **Descripción**: Polling disponibilidad cada 30s.

#### T067 — Crear `usePedido.ts` [P]
- **Archivos**: `frontend/src/hooks/usePedido.ts`
- **FR**: FR-017, FR-019
- **Depende de**: T065
- **Descripción**: Polling de estado del pedido.

#### T068 — Crear `useWebPush.ts` [P]
- **Archivos**: `frontend/src/hooks/useWebPush.ts`
- **FR**: FR-018
- **Depende de**: T052, T065
- **Descripción**: Gestión de suscripción push.

#### T069 — Crear `frontend/src/types/enums.ts` [P]
- **Archivos**: `frontend/src/types/enums.ts`
- **FR**: FR-013, FR-017
- **Depende de**: T051
- **Descripción**: Espejo de enums backend.

### Fase 14 — Integración FE-BE

#### T070 — Integrar Login + Register con backend Java [P]
- **Archivos**: `frontend/src/pages/LoginPage.tsx`, `frontend/src/pages/RegisterPage.tsx`
- **FR**: FR-001, FR-002
- **Depende de**: T053, T054, T028
- **Descripción**: Integración auth end-to-end.

#### T071 — Integrar `LocalesPage` con `GET /locales` [P]
- **Archivos**: `frontend/src/pages/LocalesPage.tsx`
- **FR**: FR-004, FR-033
- **Depende de**: T055, T029
- **Descripción**: Locales reales desde backend.

#### T072 — Integrar `MenuPage` + `CarritoContext` con API
- **Archivos**: `frontend/src/pages/MenuPage.tsx`, `frontend/src/context/CarritoContext.tsx`
- **FR**: FR-005, FR-007, FR-007b, FR-007c
- **Depende de**: T056, T064, T066, T030
- **Descripción**: Flujo menú-carrito.

#### T073 — Integrar `ConfirmacionPage` con `POST /pedidos` + redirect MP
- **Archivos**: `frontend/src/pages/ConfirmacionPage.tsx`
- **FR**: FR-011, FR-012, FR-013, FR-014
- **Depende de**: T057, T031, T032
- **Descripción**: Confirmación con efectivo y tarjeta.

#### T074 — Integrar `SeguimientoPage` con polling
- **Archivos**: `frontend/src/pages/SeguimientoPage.tsx`
- **FR**: FR-017, FR-019
- **Depende de**: T058, T067, T031
- **Descripción**: Estado en tiempo real.

#### T075 — Integrar `HistorialPage` con paginación [P]
- **Archivos**: `frontend/src/pages/HistorialPage.tsx`
- **FR**: FR-020
- **Depende de**: T059, T031
- **Descripción**: Historial paginado por estado.

#### T076 — Integrar `RepartidorPage` con cambio de estado [P]
- **Archivos**: `frontend/src/pages/RepartidorPage.tsx`
- **FR**: FR-016, FR-029, FR-030
- **Depende de**: T062, T031
- **Descripción**: Avance de estados por repartidor.

#### T077 — Integrar `AdminPage` con endpoints `/admin` [P]
- **Archivos**: `frontend/src/pages/AdminPage.tsx`
- **FR**: FR-022, FR-023, FR-024, FR-025, FR-026, FR-027, FR-028
- **Depende de**: T061, T033
- **Descripción**: Catálogo y usuarios admin.

#### T078 — Integrar `useWebPush` + Service Worker
- **Archivos**: `frontend/src/hooks/useWebPush.ts`, `frontend/public/sw.js`
- **FR**: FR-018
- **Depende de**: T068, T052, T034
- **Descripción**: Notificaciones push completas.

### Fase 15 — Validación Final

#### T079 — Levantar entorno completo según `quickstart.md`
- **Archivos**: —
- **FR**: CE-001 → CE-010
- **Depende de**: T036, T049, T051
- **Descripción**: Setup local y ejecución de tests.

#### T080 — Ejecutar flujo crítico E2E
- **Archivos**: —
- **FR**: CE-001 → CE-006
- **Depende de**: T079
- **Descripción**: Flujo completo cliente-admin-repartidor.

#### T081 — Validar criterios no funcionales
- **Archivos**: —
- **FR**: NFR-001 → NFR-008
- **Depende de**: T080
- **Descripción**: Rendimiento, seguridad, cobertura y responsive.

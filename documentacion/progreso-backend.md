# 📋 Documento de Progreso — ComidApp Backend

**Proyecto:** ComidApp — Aplicación de delivery de comida  
**Equipo:** Alfaro – Tedesco  
**Materia:** Metodologías de Trabajo y Negocio (MTN) — TP5  
**Fecha:** 19 de junio de 2026  
**Repositorio:** [Instituto-Politecnico-Modelo/2026-MTN-TP5--ComidApp-SDD](https://github.com/Instituto-Politecnico-Modelo/2026-MTN-TP5--ComidApp-SDD)

---

## 1. Resumen Ejecutivo

Se migró el backend del TP3 (que estaba en `TP3-Backend-ALFARO-TEDESCO/demo/`) a una **arquitectura limpia por capas** dentro de la carpeta `backend/`, preservando toda la lógica de negocio existente y extendiendo la funcionalidad.

### Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 4.0.5 |
| Build tool | Gradle | 9.4.1 |
| Base de datos | MySQL | 8.0 |
| Base de datos (test) | H2 | En memoria |
| ORM | Hibernate / JPA | 7.2.7 |
| Seguridad | Spring Security + JWT | jjwt 0.12.6 |
| Documentación API | Swagger / OpenAPI | springdoc 2.8.6 |
| Pagos | Mercado Pago SDK | 2.1.29 |
| Notificaciones | Web Push (VAPID) | web-push 5.1.1 |
| Tests | JUnit 5 + Mockito + JaCoCo | — |

---

## 2. Arquitectura del Proyecto

```
backend/src/main/java/com/comidapp/
├── ComidAppApplication.java          ← Punto de entrada
├── domain/                           ← Capa de dominio (29 archivos)
│   ├── entities/                     ← Entidades JPA
│   │   ├── Usuario.java              (SINGLE_TABLE, DNI como PK)
│   │   ├── Cliente.java              (hereda Usuario, dirEntrega, ciudad)
│   │   ├── Admin.java                (hereda Usuario)
│   │   ├── Repartidor.java           (hereda Usuario, disponible)
│   │   ├── Local.java                (estaAbierto(), horarios)
│   │   ├── HorarioLocal.java         (día, horaApertura, horaCierre)
│   │   ├── Producto.java             (vinculado a Local)
│   │   ├── Carrito.java              (restricción un solo local)
│   │   ├── ItemCarrito.java          (producto + cantidad)
│   │   ├── Pedido.java               (estados, transiciones, vencimiento)
│   │   ├── ItemPedido.java           (snapshot precio unitario)
│   │   ├── Pago.java                 (MercadoPago o efectivo)
│   │   └── SuscripcionPush.java      (Web Push VAPID)
│   ├── enums/
│   │   ├── EstadoPedido.java         (PENDIENTE → CONFIRMADO → ... → ENTREGADO)
│   │   ├── MetodoPago.java           (EFECTIVO, TARJETA)
│   │   ├── EstadoPago.java           (PENDIENTE, APROBADO, RECHAZADO)
│   │   ├── RolUsuario.java           (ADMIN, CLIENTE, REPARTIDOR)
│   │   └── DiaSemana.java            (LUNES–DOMINGO, valores en español)
│   └── exceptions/                   ← Excepciones tipadas
│       ├── DomainException.java
│       ├── ConflictoException.java
│       ├── CredencialesInvalidasException.java
│       ├── NoAutorizadoException.java
│       ├── RecursoNoEncontradoException.java
│       ├── TransicionEstadoException.java
│       ├── CarritoLocalException.java
│       └── FueraDeHorarioException.java
│
├── application/                      ← Casos de uso (8 archivos)
│   ├── auth/
│   │   └── AutenticacionService.java (login con BCrypt legacy, registro)
│   ├── carrito/
│   │   └── GestionarCarritoUseCase.java (agregar, modificar, eliminar, vaciar)
│   ├── pedidos/
│   │   ├── ConfirmarPedidoUseCase.java  (validar carrito, local abierto, asignar repartidor)
│   │   ├── ActualizarEstadoUseCase.java (transiciones + permisos por rol)
│   │   └── CancelarPedidoVencidoUseCase.java (@Scheduled cada 5 min)
│   ├── pagos/
│   │   └── ProcesarPagoUseCase.java     (webhook MP, confirmar efectivo)
│   └── notificaciones/
│       └── EnviarNotificacionUseCase.java (suscribir, enviar push)
│
├── infrastructure/                   ← Infraestructura (18 archivos)
│   ├── persistence/                  ← Repositorios JPA
│   │   ├── UsuarioRepository.java
│   │   ├── ProductoRepository.java
│   │   ├── PedidoRepository.java     (findVencidos con JPQL)
│   │   ├── CarritoRepository.java
│   │   ├── ItemCarritoRepository.java
│   │   ├── LocalRepository.java
│   │   ├── PagoRepository.java
│   │   └── SuscripcionPushRepository.java
│   ├── security/
│   │   ├── JwtService.java           (generar/validar tokens con claims)
│   │   ├── JwtAuthFilter.java        (filtro Bearer token)
│   │   ├── UserDetailsServiceImpl.java
│   │   └── SecurityConfig.java       (BCrypt, stateless, rutas públicas)
│   ├── payment/
│   │   └── MercadoPagoAdapter.java   (crear preferencias SDK)
│   ├── notifications/
│   │   └── WebPushService.java       (VAPID + BouncyCastle)
│   └── config/
│       └── DevDataLoader.java        (seed data solo en perfil dev)
│
└── api/                              ← Capa API REST (20 archivos)
    ├── controllers/
    │   ├── AuthController.java       (/api/v1/auth)
    │   ├── MenuController.java       (/api/v1/locales, /api/v1/locales/{id}/menu)
    │   └── CarritoController.java    (/api/v1/carrito)
    ├── dto/                          ← DTOs con Jakarta Validation
    │   ├── LoginDTO.java
    │   ├── RegistroClienteDTO.java
    │   ├── RegistroRepartidorDTO.java
    │   ├── ConfirmarPedidoDTO.java
    │   ├── AgregarItemCarritoDTO.java
    │   ├── PerfilDTO.java
    │   ├── PedidoResponseDTO.java
    │   ├── ItemPedidoDTO.java
    │   ├── ProductoDTO.java
    │   └── ProductoRequestDTO.java
    └── config/
        ├── WebConfig.java            (CORS localhost:5173)
        ├── GlobalExceptionHandler.java (mapeo excepciones → HTTP status)
        └── SwaggerConfig.java        (OpenAPI + bearerAuth)
```

**Total: 76 archivos Java** (29 domain + 8 application + 18 infrastructure + 20 api + 1 main)

---

## 3. Tareas Completadas (T001–T040)

Cada tarea fue committeada individualmente con `fixes #N` para cerrar automáticamente el issue correspondiente en GitHub.

| Tarea | Issue | Descripción | Commit |
|-------|-------|-------------|--------|
| T001 | #205 | Inicializar proyecto Spring Boot 4 + Java 21 | `ef0eead` |
| T002 | #206 | Estructura de paquetes por capas | `26b4984` |
| T003 | #207 | Pipeline de tests JUnit 5 + H2 + JaCoCo | `8fd1f99` |
| T004 | #208 | Enums de dominio (EstadoPedido, MetodoPago, etc.) | `ee25f11` |
| T005 | #209 | Excepciones de dominio tipadas | `66a928a` |
| T006 | #210 | Entidad Pedido con reglas de dominio | `a057347` |
| T007 | #211 | Entidad Carrito con restricción un solo local | `06a3e01` |
| T008 | #212 | Entidad Local con horarios y lógica estaAbierto | `2574341` |
| T009 | #213 | Entidad Producto con disponibilidad y local | `b41d0c2` |
| T010 | #214 | Entidades complementarias (ItemPedido, Pago, etc.) | `04a3e17` |
| T011 | #215 | Migración SQL con 10 tablas, índices y seed data | `cd7f80b` |
| T012 | #216 | UsuarioRepository con queries por email y rol | `72e18e0` |
| T013 | #217 | ProductoRepository con queries por local y categoría | `aa5c73c` |
| T014 | #218 | PedidoRepository con historial y query de vencidos | `f2922a5` |
| T015 | #219 | CarritoRepository e ItemCarritoRepository | `9810ef2` |
| T016 | #220 | LocalRepository, PagoRepository, SuscripcionPushRepository | `e4f845d` |
| T017 | #221 | GestionarCarritoUseCase (agregar, modificar, eliminar) | `c582a80` |
| T018 | #222 | ConfirmarPedidoUseCase (validación + asignar repartidor) | `f605a1e` |
| T019 | #223 | ActualizarEstadoUseCase (transiciones + permisos) | `29ed864` |
| T020 | #224 | CancelarPedidoVencidoUseCase (@Scheduled) | `8f60c0b` |
| T021 | #225 | ProcesarPagoUseCase (webhook MP + efectivo) | `bd77e2a` |
| T022 | #226 | EnviarNotificacionUseCase (push notifications) | `570574f` |
| T023 | #227 | MercadoPagoAdapter (SDK preferencias) | `d9cf94a` |
| T024 | #228 | WebPushService (VAPID + BouncyCastle) | `6818d36` |
| T025 | #229 | CORS, GlobalExceptionHandler, SwaggerConfig | `44abd13` |
| T026 | #230 | Spring Security JWT (filtro, servicio, roles) | `ca153f6` |
| T027 | #231 | DTOs con Jakarta Validation | `4da9ac7` |
| T028 | #232 | AuthController + AutenticacionService | `de40552` |
| T029 | #233 | MenuController (locales + menú) | `6935c98` |
| T030 | #234 | CarritoController (CRUD carrito) | `6b3290d` |
| — | — | Perfil dev + DataLoader + fixes JSON | `5379a3e` |
| — | — | Fix: correcciones endpoints, productos, serialización | `7586684` |
| T031 | #235 | PedidoController (confirmar, historial, estado) | `27e1b23` |
| T032 | #236 | PagoController (webhook MP, iniciar, efectivo) | `6529574` |
| T033 | #237 | AdminController (CRUD productos, usuarios) | `b007ff0` |
| T034 | #238 | NotificacionController (suscripción push) | `0c9354f` |
| T035 | #239 | UsuarioController (ver/editar perfil) | `ce813f4` |
| T036 | #240 | JobsController (cancelación vencidos manual) | `d55d6df` |
| T037 | #241 | Tests unitarios Pedido (13 tests) | `b5e80c1` |
| T038 | #242 | Tests unitarios Carrito (11 tests) | `52b6dd9` |
| T039 | #243 | Tests unitarios Local (9 tests) | `336b93c` |
| T040 | #244 | Tests unitarios Producto (6 tests) | `8257230` |

**Progreso: 40 de 81 tareas completadas (49%)**

---

## 3b. Tareas Completadas (T041–T050): Tests de Integración + JaCoCo

| Tarea | Issue | Descripción | Commit |
|-------|-------|-------------|--------|
| T041 | #245 | Tests integración AuthController | ✅ |
| T042 | #246 | Tests integración MenuController | ✅ |
| T043 | #247 | Tests integración CarritoController | ✅ |
| T044 | #248 | Tests integración PedidoController | ✅ |
| T045 | #249 | Tests integración PagoController | ✅ |
| T046 | #250 | Tests integración AdminController | ✅ |
| T047 | #251 | Tests integración UsuarioController | ✅ |
| T048 | #252 | Tests integración NotificacionController | ✅ |
| T049 | #253 | Tests integración seguridad | ✅ |
| T050 | #254 | Cobertura JaCoCo > 70% | ✅ |

---

## 3c. Tareas Completadas (T052–T066): Mejoras y Nuevos Endpoints

Se implementaron 15 mejoras adicionales al backend, cada una con commit individual cerrando su issue.

| Tarea | Issue | Descripción | Commit |
|-------|-------|-------------|--------|
| T052 | #256 | Configuración CORS con bean explícito (localhost:5173, :3000) | `a853054` |
| T053 | #257 | Endpoint búsqueda de productos por nombre/categoría | `288c30e` |
| T054 | #258 | Paginación en historial de pedidos del cliente | `d61f3a7` |
| T055 | #259 | Endpoint cambiar contraseña (valida actual + min 6 chars) | `917829d` |
| T056 | #260 | Estadísticas admin + filtrado de pedidos por estado | `17cdce0` |
| T057 | #261 | Datos de prueba ampliados (6 usuarios totales) | `818f252` |
| T058 | #270 | Historial y estadísticas del repartidor | `62b2971` |
| T059 | #263 | Asignación de repartidor solo si está disponible | `d730a8c` |
| T060 | #264 | Validación contraseña mínimo 6 caracteres en registro | `a2cd466` |
| T061 | #260 | Filtros pedidos por estado para admin (incluido en T056) | `17cdce0` |
| T062 | #262 | Endpoint locales con estado abierto/cerrado | `22b2cfb` |
| T063 | #267 | Endpoint categorías de productos disponibles | `22b2cfb` |
| T064 | #268 | Health check endpoint público | `ca565d6` |
| T065 | #269 | README completo con documentación de endpoints | `c3816a9` |
| T066 | #266 | Endpoint pedido activo del cliente | `f073061` |

### Nuevos Endpoints Agregados (T052–T066)

| Endpoint | Método | Auth | Descripción |
|---|---|---|---|
| `/api/v1/health` | GET | ❌ | Health check del sistema |
| `/api/v1/productos/buscar?nombre=X&categoria=Y` | GET | 🔒 | Búsqueda global de productos |
| `/api/v1/productos/categorias` | GET | 🔒 | Listar categorías disponibles |
| `/api/v1/locales/estado` | GET | 🔒 | Locales con indicador abierto/cerrado |
| `/api/v1/pedidos/mis-pedidos?page=0&size=10` | GET | 🔒 CLIENTE | Historial paginado |
| `/api/v1/pedidos/activo` | GET | 🔒 CLIENTE | Pedido activo (no finalizado) |
| `/api/v1/pedidos/repartidor/historial` | GET | 🔒 REPARTIDOR | Historial + stats del repartidor |
| `/api/v1/perfil/password` | PUT | 🔒 | Cambiar contraseña |
| `/api/v1/perfil/disponibilidad` | PUT | 🔒 REPARTIDOR | Toggle disponibilidad |
| `/api/v1/admin/estadisticas` | GET | 🔒 ADMIN | Dashboard con totales y métricas |
| `/api/v1/admin/pedidos?estado=X` | GET | 🔒 ADMIN | Pedidos filtrados por estado |

**Progreso actualizado: 65 de 81 tareas completadas (80%)**

---

## 4. Lógica Migrada del TP3

Se preservó toda la lógica de negocio del backend original (`TP3-Backend-ALFARO-TEDESCO/demo/`):

| Concepto TP3 | Ubicación Nueva | Qué se conservó |
|---|---|---|
| `Usuario` con SINGLE_TABLE | `domain/entities/Usuario.java` | DNI como PK, discriminador `tipo`, herencia |
| `Cliente`, `Admin`, `Repartidor` | `domain/entities/*.java` | Mismos campos, `@DiscriminatorValue` |
| `AutenticacionService.login()` | `application/auth/AutenticacionService.java` | BCrypt legacy migration (texto plano → hash) |
| `JwtService` | `infrastructure/security/JwtService.java` | Claims: mail, tipo, dni |
| `JwtAuthFilter` | `infrastructure/security/JwtAuthFilter.java` | Bearer token, SecurityContext |
| `SecurityConfig` | `infrastructure/security/SecurityConfig.java` | BCrypt, stateless, rutas públicas |
| `UserDetailsServiceImpl` | `infrastructure/security/UserDetailsServiceImpl.java` | ROLE_ prefix |
| `PedidoService` (asignar repartidor) | `application/pedidos/ConfirmarPedidoUseCase.java` | Random repartidor disponible |
| `ProductoService` | `api/controllers/MenuController.java` | Filtro por local + categoría |
| `CorsConfig` | `api/config/WebConfig.java` | localhost:5173 |
| `SwaggerConfig` | `api/config/SwaggerConfig.java` | bearerAuth |
| Mensajes de error en español | `domain/exceptions/*.java` + `GlobalExceptionHandler` | Mismo tono |

---

## 5. Endpoints Disponibles y Probados

### 🟢 Funcionando (probados con curl)

| Endpoint | Método | Auth | Descripción | Estado |
|---|---|---|---|---|
| `/api/v1/auth/registro/cliente` | POST | ❌ | Registrar cliente nuevo | ✅ Probado |
| `/api/v1/auth/login` | GET | ❌ | Login, devuelve JWT | ✅ Probado |
| `/api/v1/locales` | GET | 🔒 | Listar locales con horarios | ✅ Probado |
| `/api/v1/locales/{id}` | GET | 🔒 | Obtener un local | ✅ Implementado |
| `/api/v1/locales/{id}/menu` | GET | 🔒 | Menú del local (filtro categoría) | ✅ Probado |
| `/api/v1/carrito` | GET | 🔒 | Ver carrito del usuario | ✅ Probado |
| `/api/v1/carrito/items` | POST | 🔒 | Agregar item al carrito | ✅ Probado |
| `/api/v1/carrito/items/{id}` | PUT | 🔒 | Modificar cantidad | ✅ Implementado |
| `/api/v1/carrito/items/{id}` | DELETE | 🔒 | Eliminar item | ✅ Implementado |
| `/api/v1/carrito` | DELETE | 🔒 | Vaciar carrito | ✅ Implementado |
| `/api/v1/pedidos` | POST | 🔒 CLIENTE | Confirmar pedido desde carrito | ✅ Implementado |
| `/api/v1/pedidos/mis-pedidos` | GET | 🔒 CLIENTE | Historial pedidos del cliente | ✅ Implementado |
| `/api/v1/pedidos/repartidor` | GET | 🔒 REPARTIDOR | Pedidos asignados | ✅ Implementado |
| `/api/v1/pedidos/{id}` | GET | 🔒 | Obtener un pedido | ✅ Implementado |
| `/api/v1/pedidos` | GET | 🔒 ADMIN | Listar todos los pedidos | ✅ Implementado |
| `/api/v1/pedidos/{id}/estado` | PUT | 🔒 ADMIN/REPARTIDOR | Actualizar estado | ✅ Implementado |
| `/api/v1/pedidos/{id}/cancelar` | PUT | 🔒 | Cancelar pedido | ✅ Implementado |
| `/api/v1/pagos/{pedidoId}/iniciar` | POST | 🔒 | Iniciar pago | ✅ Implementado |
| `/api/v1/pagos/webhook/mercadopago` | POST | ❌ | Webhook MP | ✅ Implementado |
| `/api/v1/pagos/{pedidoId}/confirmar-efectivo` | POST | 🔒 REPARTIDOR | Confirmar efectivo | ✅ Implementado |
| `/api/v1/perfil` | GET | 🔒 | Ver perfil propio | ✅ Implementado |
| `/api/v1/perfil` | PUT | 🔒 | Editar perfil propio | ✅ Implementado |
| `/api/v1/admin/productos` | GET/POST | 🔒 ADMIN | Listar/crear productos | ✅ Implementado |
| `/api/v1/admin/productos/{id}` | PUT/DELETE | 🔒 ADMIN | Editar/eliminar producto | ✅ Implementado |
| `/api/v1/admin/usuarios` | GET | 🔒 ADMIN | Listar usuarios | ✅ Implementado |
| `/api/v1/admin/repartidores` | POST | 🔒 ADMIN | Registrar repartidor | ✅ Implementado |
| `/api/v1/notificaciones/suscribir` | POST/DELETE | 🔒 | Suscripción push | ✅ Implementado |
| `/api/v1/admin/jobs/cancelar-vencidos` | POST | 🔒 ADMIN | Forzar cancelación | ✅ Implementado |
| `/swagger-ui/index.html` | GET | ❌ | Swagger UI | ✅ Probado |

---

### Ejemplos de JSON por endpoint

#### `GET /api/v1/auth/login?mail=juan@test.com&contrasenia=admin123`

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqdWFuQHRlc3QuY29tIiwidGlwbyI6IkNMSUVOVEUiLCJkbmkiOjEyMzQ1Njc4LCJpYXQiOjE3ODIxMjgyMTEsImV4cCI6MTc4MjczMzAxMX0..."
}
```

#### `POST /api/v1/auth/registro/cliente`

Request:
```json
{
  "dni": 99887766,
  "nombre": "Ana",
  "apellido": "Garcia",
  "mail": "ana@mail.com",
  "contrasenia": "miClave123",
  "telefono": 351444555,
  "dirEntrega": "Calle Falsa 123",
  "ciudad": "Cordoba"
}
```

Response:
```json
{
  "mensaje": "Cliente registrado correctamente"
}
```

#### `GET /api/v1/locales`

```json
[
  {
    "id": 1,
    "nombre": "ComidApp Centro",
    "direccion": "Av. San Martin 100, Centro",
    "telefono": "0351-4001001",
    "horarios": [
      {
        "diaSemana": "LUNES",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      },
      {
        "diaSemana": "MARTES",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      },
      {
        "diaSemana": "MIERCOLES",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      },
      {
        "diaSemana": "JUEVES",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      },
      {
        "diaSemana": "VIERNES",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      },
      {
        "diaSemana": "SABADO",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      },
      {
        "diaSemana": "DOMINGO",
        "horaApertura": "12:00:00",
        "horaCierre": "22:00:00"
      }
    ]
  }
]
```

#### `GET /api/v1/locales/1/menu`

```json
[
  {
    "nombre": "Hamburguesa Simple",
    "descripcion": "Pan brioche, carne 150g, lechuga, tomate, queso tybo",
    "precioUnitario": 10500.0,
    "categoria": "hamburguesas",
    "imagenUrl": null,
    "disponible": true
  },
  {
    "nombre": "Hamburguesa Doble",
    "descripcion": "Pan brioche, doble carne 300g, queso cheddar, bacon crocante",
    "precioUnitario": 14000.0,
    "categoria": "hamburguesas",
    "imagenUrl": null,
    "disponible": true
  },
  {
    "nombre": "Papas Fritas Grandes",
    "descripcion": "Papas fritas crocantes con sal y especias, porcion grande",
    "precioUnitario": 6500.0,
    "categoria": "acompañamientos",
    "imagenUrl": null,
    "disponible": true
  },
  {
    "nombre": "Coca-Cola 500ml",
    "descripcion": "Gaseosa Coca-Cola linea regular 500ml",
    "precioUnitario": 3500.0,
    "categoria": "bebidas",
    "imagenUrl": null,
    "disponible": true
  }
]
```

#### `POST /api/v1/carrito/items`

Request:
```json
{
  "productoId": 1,
  "cantidad": 2
}
```

Response:
```json
{
  "id": 1,
  "cliente": {
    "dni": 12345678,
    "nombre": "Juan",
    "apellido": "Perez",
    "mail": "juan@test.com",
    "telefono": 351555123,
    "tipo": "CLIENTE",
    "dirEntrega": "Av. Colon 123",
    "ciudad": "Cordoba"
  },
  "local": {
    "id": 1,
    "nombre": "ComidApp Centro",
    "direccion": "Av. San Martin 100, Centro",
    "telefono": "0351-4001001",
    "horarios": [
      {
        "diaSemana": "LUNES",
        "horaApertura": "11:00:00",
        "horaCierre": "23:00:00"
      }
    ]
  },
  "items": [
    {
      "id": 1,
      "producto": {
        "id": 1,
        "nombre": "Hamburguesa Simple",
        "descripcion": "Pan brioche, carne 150g, lechuga, tomate, queso tybo",
        "precioUnitario": 10500.0,
        "categoria": "hamburguesas",
        "imagenUrl": null,
        "disponible": true
      },
      "cantidad": 2,
      "subtotal": 21000.0
    }
  ]
}
```

### ✅ Tests Unitarios (39 tests, 0 failures)

| Suite | Tests | Descripción |
|---|---|---|
| `PedidoTest` | 13 | Transiciones de estado, cancelación, vencimiento, cálculo total |
| `CarritoTest` | 11 | Restricción un solo local, agregar/eliminar/modificar items |
| `LocalTest` | 9 | estaAbiertoEn(), DiaSemana conversión, horarios |
| `ProductoTest` | 6 | Disponibilidad, ProductoDTO sin ids, categorías |

---

## 6. Cómo Probar el Backend

### Levantar en modo producción (MySQL)

```bash
cd backend
./gradlew bootRun
```

Esto levanta el backend en `http://localhost:3001` con:
- **MySQL 8.0** (base de datos `comida_app`, usuario `root`, contraseña `alumnoipm`)
- **6 usuarios precargados** automáticamente (si la base está vacía)
- **5 locales con horarios** y **42+ productos** (hamburguesas, acompañamientos, bebidas)
- **DDL auto-update**: las tablas se crean/actualizan automáticamente

### Requisitos

1. Tener MySQL corriendo en `localhost:3306`
2. Crear la base de datos: `CREATE DATABASE comida_app;`
3. Verificar credenciales en `application.properties` (`root` / `alumnoipm`)

### Usuarios de prueba (DevDataLoader — 6 usuarios)

| Email | Contraseña | Rol | DNI | Nombre |
|---|---|---|---|---|
| `admin@comidapp.ar` | `admin123` | ADMIN | 11111111 | Admin ComidApp |
| `juan@test.com` | `admin123` | CLIENTE | 12345678 | Juan Perez |
| `laura@test.com` | `admin123` | CLIENTE | 22334455 | Laura Gomez |
| `pedro@test.com` | `admin123` | CLIENTE | 33445566 | Pedro Martinez |
| `maria@test.com` | `admin123` | REPARTIDOR | 87654321 | Maria Lopez |
| `carlos@test.com` | `admin123` | REPARTIDOR | 11223344 | Carlos Ruiz |

> ⚠️ **Todas las contraseñas son `admin123`** (hasheadas con BCrypt en la base de datos)

### Probar con curl

```bash
# Login (GET con query params)
TOKEN=$(curl -s 'http://localhost:3001/api/v1/auth/login?mail=juan@test.com&contrasenia=admin123' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Listar locales
curl -s http://localhost:3001/api/v1/locales \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Ver menú del local 1
curl -s http://localhost:3001/api/v1/locales/1/menu \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# Agregar item al carrito
curl -s -X POST http://localhost:3001/api/v1/carrito/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productoId":1,"cantidad":2}' | python3 -m json.tool
```

---

## 7. Tareas Pendientes (T067–T081)

### Futuras: Frontend React (T067–T081)

Integración del frontend React con el backend migrado.

---

## 8. Decisiones Técnicas Importantes

1. **MySQL como base de datos principal**: con `spring.jpa.hibernate.ddl-auto=update` las tablas se crean/actualizan automáticamente. H2 solo se usa para tests.

2. **`@JsonIgnore` en `contrasenia`**: evita que el hash BCrypt se exponga en las respuestas JSON.

3. **`@JsonIgnore` en back-references**: evita referencias circulares al serializar (ej: `ItemCarrito.carrito`, `HorarioLocal.local`, `Pago.pedido`).

4. **`FetchType.EAGER` selectivo**: en `Carrito` (cliente, local, items), `Local` (horarios) y `Producto` (local) para evitar `LazyInitializationException` fuera de transacciones. El resto se mantiene LAZY.

5. **`DevDataLoader` sin restricción de perfil**: carga datos de prueba programáticamente si la base está vacía (`count() == 0`). Funciona tanto con H2 como con MySQL.

6. **Compatibilidad BCrypt legacy**: el login detecta si la contraseña está en texto plano y la migra automáticamente a BCrypt.

7. **Enum `DiaSemana` propio**: en lugar de `java.time.DayOfWeek`, para que los valores se persistan en español (LUNES, MARTES, etc.) en la base de datos.

8. **Login como GET**: el endpoint `/api/v1/auth/login` usa `@GetMapping` con `@RequestParam` para recibir `mail` y `contrasenia` como query params (consistente con el frontend del TP3).

9. **ProductoDTO sin id/localId**: el menú público no expone identificadores internos — el carrito usa el `productoId` que se conoce internamente.

---

*Documento actualizado el 25/06/2026*

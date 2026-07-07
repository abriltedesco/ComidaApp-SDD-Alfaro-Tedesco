# Data Model: ComidApp MVP

**Feature**: `specs/001-comidapp-mvp/`  
**Date**: 2026-06-09  
**Motor**: MySQL 8.0  
**ORM**: JPA / Hibernate (Spring Data JPA)

---

## Diagrama de Entidades (ERD en texto)

```
usuario (1) ──────────────── (N) pedido
usuario (1) ──────────────── (1) carrito (activo, temporal)
usuario (1) ──────────────── (N) suscripcion_push

local (1) ─────────────────── (N) item_menu
local (1) ─────────────────── (N) horario_local
local (1) ─────────────────── (N) pedido

producto (1) ───────────────── (N) item_menu
producto (1) ───────────────── (N) item_pedido
producto (1) ───────────────── (N) item_carrito

carrito (1) ────────────────── (N) item_carrito
carrito (1) ────────────────── (1) local (restricción: un solo local)

pedido (1) ─────────────────── (N) item_pedido
pedido (1) ─────────────────── (1) pago
pedido (1) ─────────────────── (1) usuario (cliente)
pedido (1) ─────────────────── (0..1) usuario (repartidor)
```

---

## Enums

```sql
-- EstadoPedido
RECIBIDO | EN_PREPARACION | EN_CAMINO | ENTREGADO | CANCELADO

-- EstadoPago
PENDIENTE | APROBADO | RECHAZADO | REVERTIDO

-- MetodoPago
TARJETA | EFECTIVO

-- RolUsuario
CLIENTE | ADMIN | REPARTIDOR
```

---

## Tablas

### `usuario`

Almacena todos los actores del sistema (clientes, repartidores y admin).

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre del usuario |
| `apellido` | VARCHAR(100) | NOT NULL | Apellido del usuario |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email de login |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash bcrypt de la contraseña |
| `telefono` | VARCHAR(20) | NULL | Teléfono de contacto |
| `direccion_entrega` | VARCHAR(300) | NULL | Dirección predeterminada de entrega |
| `rol` | ENUM('CLIENTE','ADMIN','REPARTIDOR') | NOT NULL, DEFAULT 'CLIENTE' | Rol del usuario |
| `activo` | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete; si 0 no puede iniciar sesión |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Índices**:
```sql
UNIQUE INDEX idx_usuario_email (email)
INDEX idx_usuario_rol (rol)
```

**Notas**:
- El campo `password_hash` se rellena con bcrypt (cost factor 12).
- El campo `telefono` se almacena como VARCHAR para soportar formatos internacionales y evitar overflow de INT.
- `activo = 0` invalida cualquier sesión JWT existente del usuario (el middleware de auth verifica este campo).

---

### `local`

Los 5 establecimientos físicos de la empresa (datos estáticos en el MVP).

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre del local (ej: "El Monstruo") |
| `direccion` | VARCHAR(300) | NOT NULL | Dirección física |
| `barrio` | VARCHAR(100) | NOT NULL | Barrio o zona |
| `tiempo_estimado_min` | INT | NOT NULL | Tiempo estimado de entrega en minutos |
| `activo` | TINYINT(1) | NOT NULL, DEFAULT 1 | Si está operativo |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Notas**:
- Los 5 locales se insertan vía seed/datos iniciales. No hay UI de creación de locales en el MVP.
- El horario de atención se gestiona en la tabla separada `horario_local`.

---

### `horario_local`

Franjas horarias de apertura y cierre por local y día de la semana.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `local_id` | INT UNSIGNED | NOT NULL, FK → local.id | |
| `dia_semana` | TINYINT | NOT NULL | 0=Domingo, 1=Lunes, ..., 6=Sábado |
| `hora_apertura` | TIME | NOT NULL | Ej: '11:00:00' |
| `hora_cierre` | TIME | NOT NULL | Ej: '23:00:00' |

**Índices**:
```sql
INDEX idx_horario_local_id (local_id)
```

**Regla de negocio** (usada por FR-033):
```sql
SELECT COUNT(*) FROM horario_local
WHERE local_id = ?
  AND dia_semana = DAYOFWEEK(NOW()) - 1
  AND hora_apertura <= TIME(NOW())
  AND hora_cierre >= TIME(NOW());
-- Si COUNT > 0 → local abierto
```

---

### `producto`

Ítems del catálogo de la empresa (hamburguesas y otros artículos).

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `nombre` | VARCHAR(200) | NOT NULL | Nombre del producto |
| `descripcion` | TEXT | NULL | Descripción detallada |
| `imagen_url` | VARCHAR(500) | NULL | URL de la imagen del producto |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Notas**:
- El precio no vive en `producto` sino en `item_menu` (puede diferir por local).
- `producto` es la entidad compartida del catálogo; `item_menu` es la relación con disponibilidad y precio por local.

---

### `item_menu`

Relación entre un Producto y un Local: define disponibilidad y precio por local.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `local_id` | INT UNSIGNED | NOT NULL, FK → local.id | |
| `producto_id` | INT UNSIGNED | NOT NULL, FK → producto.id | |
| `precio` | DECIMAL(10,2) | NOT NULL | Precio vigente en este local |
| `disponible` | TINYINT(1) | NOT NULL, DEFAULT 1 | Si el producto está activo en este local |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Índices**:
```sql
UNIQUE INDEX idx_item_menu_local_producto (local_id, producto_id)
INDEX idx_item_menu_local_disponible (local_id, disponible)
```

**Notas**:
- El índice compuesto `(local_id, disponible)` cubre la query más frecuente: menú activo de un local.
- Cuando el admin desactiva un producto, solo se actualiza `disponible = 0` en `item_menu` para ese local. Los demás locales no se ven afectados.
- `precio` en `item_menu` es el precio vigente. Los `item_pedido` capturan el precio al momento de la confirmación (son inmutables).

---

### `carrito`

Carrito de compras temporal asociado a un usuario y a un único local.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `usuario_id` | INT UNSIGNED | NOT NULL, UNIQUE, FK → usuario.id | Un usuario tiene como máximo 1 carrito activo |
| `local_id` | INT UNSIGNED | NOT NULL, FK → local.id | Local del carrito (restricción: un solo local) |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Índices**:
```sql
UNIQUE INDEX idx_carrito_usuario (usuario_id)
INDEX idx_carrito_local (local_id)
```

**Notas**:
- La restricción `UNIQUE (usuario_id)` garantiza que solo existe un carrito activo por usuario.
- Al confirmar el pedido, el carrito se elimina (los datos pasan a `item_pedido`).
- Si el cliente quiere cambiar de local, el carrito se vacía y se actualiza `local_id`.

---

### `item_carrito`

Ítems dentro del carrito temporal.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `carrito_id` | INT UNSIGNED | NOT NULL, FK → carrito.id ON DELETE CASCADE | |
| `item_menu_id` | INT UNSIGNED | NOT NULL, FK → item_menu.id | Referencia al item del menú de este local |
| `cantidad` | SMALLINT UNSIGNED | NOT NULL, DEFAULT 1, CHECK (cantidad >= 1) | |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Índices**:
```sql
INDEX idx_item_carrito_carrito (carrito_id)
UNIQUE INDEX idx_item_carrito_carrito_item (carrito_id, item_menu_id)
```

**Notas**:
- `ON DELETE CASCADE` en `carrito_id` elimina todos los ítems cuando se elimina el carrito.
- El índice `UNIQUE (carrito_id, item_menu_id)` impide duplicar el mismo producto en el carrito; se actualiza `cantidad` en su lugar.

---

### `pedido`

Orden de compra confirmada. Captura el estado en el momento de la confirmación.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `cliente_id` | INT UNSIGNED | NOT NULL, FK → usuario.id | Cliente que realizó el pedido |
| `local_id` | INT UNSIGNED | NOT NULL, FK → local.id | Local del que proviene el pedido |
| `repartidor_id` | INT UNSIGNED | NULL, FK → usuario.id | Repartidor asignado (puede ser NULL hasta asignación) |
| `estado` | ENUM('RECIBIDO','EN_PREPARACION','EN_CAMINO','ENTREGADO','CANCELADO') | NOT NULL, DEFAULT 'RECIBIDO' | Estado del pedido |
| `metodo_pago` | ENUM('TARJETA','EFECTIVO') | NOT NULL | Método seleccionado al confirmar |
| `precio_total` | DECIMAL(10,2) | NOT NULL | Total calculado al momento de la confirmación |
| `costo_envio` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | Costo de envío aplicado |
| `direccion_entrega` | VARCHAR(300) | NOT NULL | Copia de la dirección al momento del pedido |
| `fecha_creacion` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| `fecha_actualizacion` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Índices**:
```sql
INDEX idx_pedido_cliente (cliente_id)
INDEX idx_pedido_repartidor (repartidor_id)
INDEX idx_pedido_estado (estado)
INDEX idx_pedido_local (local_id)
INDEX idx_pedido_cliente_estado (cliente_id, estado)
INDEX idx_pedido_estado_fecha (estado, fecha_creacion)
```

**Notas**:
- `precio_total` es la suma de `item_pedido.precio_unitario * cantidad` + `costo_envio`. Es inmutable.
- `direccion_entrega` se copia del perfil del usuario al momento de la confirmación (evita que cambios en el perfil afecten pedidos activos).
- El índice compuesto `(estado, fecha_creacion)` es clave para la tarea de cancelación automática de pedidos vencidos.
- El índice `(cliente_id, estado)` optimiza filtros de historial por estado (ej: "mis pedidos activos").

---

### `historial_estado_pedido`

Registro inmutable de cada transición de estado del pedido.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `pedido_id` | INT UNSIGNED | NOT NULL, FK → pedido.id | |
| `estado_anterior` | ENUM('RECIBIDO','EN_PREPARACION','EN_CAMINO','ENTREGADO','CANCELADO') | NULL | NULL si es el estado inicial |
| `estado_nuevo` | ENUM('RECIBIDO','EN_PREPARACION','EN_CAMINO','ENTREGADO','CANCELADO') | NOT NULL | |
| `actor_id` | INT UNSIGNED | NULL, FK → usuario.id | Usuario o sistema que realizó el cambio (NULL = sistema) |
| `fecha` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Índices**:
```sql
INDEX idx_historial_pedido (pedido_id)
```

**Notas**:
- Satisface NFR-007: trazabilidad completa de transiciones con fecha y actor responsable.
- `actor_id = NULL` significa que la transición fue automática (ej: pago confirmado → EN_PREPARACION).

---

### `item_pedido`

Ítems del pedido confirmado. Precios inmutables al momento de la confirmación.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `pedido_id` | INT UNSIGNED | NOT NULL, FK → pedido.id ON DELETE CASCADE | |
| `producto_id` | INT UNSIGNED | NOT NULL, FK → producto.id | Referencia al producto (para historial) |
| `nombre_producto` | VARCHAR(200) | NOT NULL | Copia del nombre al momento de la confirmación |
| `precio_unitario` | DECIMAL(10,2) | NOT NULL | Precio capturado al momento de la confirmación (inmutable) |
| `cantidad` | SMALLINT UNSIGNED | NOT NULL, DEFAULT 1 | |

**Índices**:
```sql
INDEX idx_item_pedido_pedido (pedido_id)
```

**Notas**:
- `precio_unitario` y `nombre_producto` son copias inmutables. Cambios posteriores en `item_menu` no los afectan.
- Satisface FR-013, NFR-006 y CE-004.

---

### `pago`

Registro del estado económico de cada pedido.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `pedido_id` | INT UNSIGNED | NOT NULL, UNIQUE, FK → pedido.id | Un pedido tiene exactamente un pago |
| `estado` | ENUM('PENDIENTE','APROBADO','RECHAZADO','REVERTIDO') | NOT NULL, DEFAULT 'PENDIENTE' | Estado del pago |
| `metodo` | ENUM('TARJETA','EFECTIVO') | NOT NULL | |
| `monto` | DECIMAL(10,2) | NOT NULL | Monto total del pago |
| `external_payment_id` | VARCHAR(100) | NULL | ID del pago en Mercado Pago (solo si TARJETA) |
| `fecha_actualizacion` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Índices**:
```sql
UNIQUE INDEX idx_pago_pedido (pedido_id)
INDEX idx_pago_estado (estado)
```

**Notas**:
- `external_payment_id` almacena el `payment_id` de Mercado Pago, necesario para operaciones de consulta y reversa.
- `REVERTIDO` es un estado adicional para registrar reversas de tarjeta (ej: pedido cancelado por timeout tras pago aprobado).

---

### `suscripcion_push`

Suscripciones Web Push activas de los usuarios.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | |
| `usuario_id` | INT UNSIGNED | NOT NULL, FK → usuario.id | |
| `endpoint` | VARCHAR(2048) | NOT NULL | URL del endpoint del servicio push |
| `p256dh` | VARCHAR(512) | NOT NULL | Clave pública del cliente para cifrado |
| `auth` | VARCHAR(128) | NOT NULL | Token de autenticación del cliente |
| `user_agent` | VARCHAR(500) | NULL | Identificación del navegador/dispositivo |
| `activa` | TINYINT(1) | NOT NULL, DEFAULT 1 | Si la suscripción sigue siendo válida |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Índices**:
```sql
INDEX idx_push_usuario (usuario_id)
```

**Notas**:
- Un usuario puede tener múltiples suscripciones activas (diferentes dispositivos/navegadores).
- Cuando un push falla con `410 Gone`, el backend marca la suscripción como `activa = 0`.

---

## Entidades JPA (referencia)

```java
// Este bloque es referencia de diseño. No es código fuente todavía.

@Entity
@Table(name = "usuario", indexes = {
    @Index(columnList = "rol")
})
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false)
    private String apellido;

    @Column(unique = true, length = 255, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String passwordHash;

    @Column(length = 20)
    private String telefono;

    @Column(length = 300)
    private String direccionEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol = RolUsuario.CLIENTE;

    @Column(nullable = false)
    private Boolean activo = true;

    // ... relaciones y timestamps
}

public enum RolUsuario { CLIENTE, ADMIN, REPARTIDOR }
public enum EstadoPedido { RECIBIDO, EN_PREPARACION, EN_CAMINO, ENTREGADO, CANCELADO }
public enum MetodoPago { TARJETA, EFECTIVO }
public enum EstadoPago { PENDIENTE, APROBADO, RECHAZADO, REVERTIDO }
```

---

## Reglas de integridad referencial

| FK | ON DELETE | ON UPDATE |
|----|-----------|-----------|
| `pedido.cliente_id → usuario.id` | RESTRICT | CASCADE |
| `pedido.repartidor_id → usuario.id` | SET NULL | CASCADE |
| `pedido.local_id → local.id` | RESTRICT | CASCADE |
| `item_pedido.pedido_id → pedido.id` | CASCADE | CASCADE |
| `item_carrito.carrito_id → carrito.id` | CASCADE | CASCADE |
| `item_menu.local_id → local.id` | RESTRICT | CASCADE |
| `item_menu.producto_id → producto.id` | RESTRICT | CASCADE |
| `pago.pedido_id → pedido.id` | CASCADE | CASCADE |
| `historial_estado_pedido.pedido_id → pedido.id` | CASCADE | CASCADE |
| `suscripcion_push.usuario_id → usuario.id` | CASCADE | CASCADE |
| `horario_local.local_id → local.id` | CASCADE | CASCADE |

---

## Datos iniciales (seed)

- 5 locales con sus direcciones, barrios y horarios de atención.
- 1 usuario admin predeterminado (contraseña cambiada antes de producción).
- Catálogo inicial de productos con precios e imágenes por local.
- Horarios de atención por local y día de la semana.

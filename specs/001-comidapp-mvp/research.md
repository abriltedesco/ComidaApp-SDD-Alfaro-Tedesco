# Research: ComidApp MVP — Decisiones Técnicas

**Feature**: `specs/001-comidapp-mvp/`  
**Date**: 2026-06-09  
**Status**: Completo — sin NEEDS CLARIFICATION pendientes

---

## Decisión 1: Pasarela de Pago — Mercado Pago

**Decision**: Mercado Pago como pasarela de pago externa para el MVP.

**Rationale**:
- Mercado Pago es el proveedor con mayor penetración en Argentina y LATAM, alineado con el mercado objetivo de la empresa.
- Ofrece SDK oficial para Java bien documentado y activamente mantenido.
- El flujo de Checkout Pro o Checkout API permite redirect o integración embebida sin que ComidApp toque datos de tarjeta en ningún momento (PCI-DSS completamente delegado).
- Webhook nativo para notificación asincrónica del resultado del pago (`payment.approved`, `payment.rejected`), lo que permite la transición automática RECIBIDO → EN_PREPARACION cuando el pago es aprobado (FR-017b).
- Soporta medios de pago locales (tarjeta de crédito/débito, Visa, Mastercard, American Express) sin configuración adicional.

**Integration flow**:
```
Cliente → POST /pedidos/confirmar
       → Backend crea Pedido (estado RECIBIDO, pago PENDIENTE)
       → Backend solicita preferencia de pago a MP API
       → Backend devuelve `init_point` (URL del checkout de MP)
       → Cliente redirige a MP
       → MP notifica resultado vía webhook a POST /pagos/webhook
       → Backend actualiza EstadoPago y, si APROBADO, avanza estado del pedido
```

**Flujo de reversa** (FR-032 — cancelación por timeout + pago ya aprobado):
- Se invoca `payments.cancel()` o `refunds.create()` del SDK de MP con el `payment_id` almacenado en la tabla `pagos`.
- La reversa se registra en `pagos.estado = REVERTIDO` (estado adicional solo para registro; el cliente ve `CANCELADO`).

**Alternatives considered**:
- **Stripe**: excelente DX global y SDK TypeScript superior, pero menor penetración local y proceso de onboarding más lento en Argentina.
- **PayPal**: sin soporte para tarjetas locales argentinas. Descartado.

**Secrets management**: `MP_ACCESS_TOKEN` y `MP_WEBHOOK_SECRET` se almacenan como variables de entorno. Nunca se commitean. En producción: Docker secrets o gestor de secretos del proveedor cloud.

---

## Decisión 2: Notificaciones Web Push — VAPID + web-push

**Decision**: Web Push estándar W3C implementado con la librería `webpush-java` (o `web-push-java`) y VAPID para autenticación del servidor.

**Rationale**:
- Web Push funciona con la aplicación cerrada o en segundo plano en todos los navegadores modernos (Chrome, Firefox, Edge, Safari 16.4+) mediante Service Worker.
- No requiere cuenta ni costo en servicio externo de terceros.
- VAPID (Voluntary Application Server Identification) garantiza que el servidor que envía las notificaciones es legítimo, sin API key de proveedor intermedio.
- La librería `webpush-java` es la implementación estándar VAPID en Java/Spring Boot.
- El cliente registra su suscripción (objeto `PushSubscription`) y el backend la almacena; al cambiar el estado del pedido, el backend invoca el servicio Web Push.

**Flow**:
```
1. Frontend: navigator.serviceWorker.register('/sw.js')
2. Frontend: registration.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: VAPID_PUBLIC_KEY })
3. Frontend: POST /notificaciones/suscribir → guarda suscripción en BD
4. Backend: al cambiar estado del pedido → webpush.sendNotification(suscripción, payload)
5. Service Worker: captura 'push' event → muestra notificación nativa del navegador
```

**Payload del mensaje**:
```json
{
  "titulo": "Tu pedido está en camino",
  "cuerpo": "Pedido #1234 — Estado: EN_CAMINO",
  "pedidoId": 1234,
  "nuevoEstado": "EN_CAMINO"
}
```

**Degradación elegante**: si el usuario no otorga permiso de notificaciones, el sistema funciona normalmente. La pantalla de seguimiento muestra el estado actualizado al próximo acceso.

**Alternatives considered**:
- **Firebase Cloud Messaging (FCM)**: requiere cuenta Google, dependencia de infra externa, más complejo. Innecesario para el alcance del MVP.
- **OneSignal**: SaaS externo, costo en volumen, vendor lock-in. Descartado.
- **Email/SMS**: costo operativo por mensaje. Descartado para notificaciones en tiempo real del pedido.

**Secrets**: par de claves VAPID generado una vez (`vapidPublicKey`, `vapidPrivateKey`) y almacenado como variables de entorno.

---

## Decisión 3: Estrategia de Índices MySQL

**Decision**: Índices explícitos en columnas de alta frecuencia de consulta, diseñados para las queries críticas del sistema.

**Rationale**: Con ~500 clientes activos y pedidos acumulando con el tiempo, sin índices las consultas de historial y seguimiento degradan a O(n). Los índices detallados a continuación garanteízan O(log n) para las operaciones más frecuentes.

### Índices definidos

| Tabla | Columna(s) | Tipo | Query que optimiza | FR asociado |
|-------|-----------|------|-------------------|-------------|
| `usuario` | `email` | UNIQUE INDEX | Login por email; verificación unicidad registro | FR-001, FR-002 |
| `pedido` | `cliente_id` | INDEX | Historial de pedidos del cliente (paginado) | FR-020 |
| `pedido` | `estado` | INDEX | Filtrado por estado; búsqueda de pedidos activos; cancelación automática | FR-017, FR-032 |
| `pedido` | `repartidor_id` | INDEX | Pedidos asignados a un repartidor | FR-029 |
| `pedido` | `local_id` | INDEX | Pedidos por local (analytics admin) | FR-027 |
| `pedido` | `(cliente_id, estado)` | COMPOSITE INDEX | Historial filtrado por estado + cliente | FR-020 |
| `pedido` | `(estado, fecha_creacion)` | COMPOSITE INDEX | Cancelación automática: WHERE estado='RECIBIDO' AND fecha_creacion < NOW()-1h | FR-032 |
| `item_menu` | `local_id` | INDEX | Menú por local (la query más frecuente del sistema) | FR-004, FR-006 |
| `item_menu` | `(local_id, disponible)` | COMPOSITE INDEX | Menú activo por local; filtro de disponibilidad en tiempo real | FR-006, FR-007c |
| `item_pedido` | `pedido_id` | INDEX | Detalle de un pedido | FR-020 |
| `suscripcion_push` | `usuario_id` | INDEX | Envío de notificación al usuario | FR-018 |
| `local` | `id` | PRIMARY KEY | Validación de horario de atención | FR-033 |

### Query crítica: cancelación automática (tarea programada cada 5 min)
```sql
SELECT id FROM pedido
WHERE estado = 'RECIBIDO'
  AND fecha_creacion < DATE_SUB(NOW(), INTERVAL 1 HOUR);
-- Cubierta por composite index (estado, fecha_creacion)
```

### Query crítica: menú activo de un local
```sql
SELECT p.*, im.precio, im.disponible
FROM item_menu im
JOIN producto p ON p.id = im.producto_id
WHERE im.local_id = ?
  AND im.disponible = true;
-- Cubierta por composite index (local_id, disponible)
```

### Query crítica: historial paginado del cliente
```sql
SELECT * FROM pedido
WHERE cliente_id = ?
ORDER BY fecha_creacion DESC
LIMIT 10 OFFSET ?;
-- Cubierta por INDEX (cliente_id) + ORDER BY sobre fecha_creacion (clustered)
```

**Alternatives considered**:
- Full-text search en nombres de productos: innecesario para el MVP; el volumen es muy pequeño.
- Redis para caché de menú: no justificado en el MVP con ~500 usuarios; se puede agregar en una iteración posterior si el menú es leído miles de veces por segundo.

---

## Decisión 4: ORM — JPA / Hibernate

**Decision**: JPA con Hibernate como ORM principal, integrado con Spring Data JPA.

**Rationale**:
- Estándar de la industria Java para persistencia de datos.
- Integración nativa con Spring Boot mediante Spring Data JPA.
- Entidades anotadas con `@Entity`, `@Table`, `@Column` — mapping declarativo.
- Repositorios generados automáticamente con `JpaRepository<T, ID>`.
- Soporte para migraciones SQL versionadas con Flyway o scripts manuales.
- `ddl-auto` configurado como `validate` en producción (P8 de la constitución).
- El acceso a datos se encapsula detrás de interfaces Repository en la capa de infraestructura, respetando la separación de capas (P3).

**Testing con JPA**:
- Para tests unitarios de dominio: no se usa JPA (las entidades de dominio son POJOs con lógica de negocio).
- Para tests de repositorio: se usa `@DataJpaTest` con H2 in-memory o MySQL Testcontainers.
- Para tests de integración de endpoints: `@SpringBootTest` + `MockMvc`.

**Alternatives considered**:
- **MyBatis**: control SQL granular pero más verboso, sin generación automática de schema.
- **JOOQ**: excelente type-safety SQL pero curva de aprendizaje mayor y licencia comercial para MySQL.
- **Spring Data JDBC**: más simple que JPA pero sin lazy loading ni caché de primer nivel.

---

## Decisión 5: Validación de Entrada — Jakarta Validation (Bean Validation)

**Decision**: Jakarta Validation (antes javax.validation) con Hibernate Validator para validación de DTOs en el backend.

**Rationale**:
- Estándar Java para validación declarativa mediante anotaciones (`@NotBlank`, `@Email`, `@Size`, `@Min`).
- Integración automática con Spring Boot: los DTOs anotados se validan automáticamente al recibir requests con `@Valid`.
- Mensajes de error descriptivos listos para devolver al cliente.
- Las reglas de formato (email, longitud mínima de contraseña) quedan en DTOs anotados en la capa API; las reglas de negocio quedan en el dominio.

---

## Decisión 6: Tarea programada para cancelación automática — @Scheduled (Spring)

**Decision**: `@Scheduled` de Spring Framework para ejecutar cada 5 minutos la tarea que cancela pedidos vencidos (FR-032).

**Rationale**:
- Sin dependencias externas de cola de mensajes (RabbitMQ, Redis) que añadirían complejidad innecesaria.
- `@Scheduled` es nativo de Spring Framework, corre dentro del mismo proceso Java.
- La query está cubierta por el índice compuesto `(estado, fecha_creacion)`.
- La granularidad de 5 minutos es suficiente: el timeout de cancelación es de 1 hora.
- Se habilita con `@EnableScheduling` en la configuración de Spring Boot.

**Job**: cada 5 minutos → `CancelarPedidoVencidoUseCase.ejecutar()` → consulta pedidos RECIBIDO con más de 1h → cancela → notifica cliente → reversa si pago tarjeta APROBADO.

---

## Decisión 7: Arquitectura Frontend — React Context para Carrito

**Decision**: `CarritoContext` (React Context API) para el estado del carrito en el frontend.

**Rationale**:
- El carrito es estado compartido entre `MenuPage`, `CarritoSidebar` y `ConfirmacionPage`.
- La complejidad del estado del carrito (restricción de un local, marca de productos desactivados, cálculo de total) no justifica Redux ni Zustand en el MVP.
- Context API + `useReducer` es suficiente y no añade dependencias.
- La verificación en tiempo real de disponibilidad de productos en el carrito se implementa como un polling cada 30 segundos desde `useCarrito.ts` al endpoint `GET /menu/:localId/disponibilidad`.

**Alternatives considered**:
- Zustand: más ergonómico que Context API, pero añade una dependencia. Se puede migrar en iteraciones futuras si la complejidad crece.
- Redux Toolkit: excesivo para el alcance del MVP.

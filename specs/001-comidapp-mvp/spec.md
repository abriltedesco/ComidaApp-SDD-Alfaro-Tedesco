# Feature Specification: ComidApp MVP — Pedidos y Entrega Online de Comida Rápida

**Feature Branch**: `001-comidapp-mvp`

**Created**: 2026-06-09

**Status**: Aprobado

**Input**: Plataforma web de pedidos y entrega de comida rápida para una empresa de hamburguesas con 5 locales físicos, estilo UberEats/PedidosYa.

---

## Historias de Usuario y Testing

<!--
  Prioridades:
    Alta  (P1) = Imprescindible para cualquier entrega funcional del MVP.
    Media (P2) = Importante; se requiere antes del lanzamiento completo.
    Baja  (P3) = Valiosa; puede diferirse a la primera iteración post-lanzamiento.
-->

### Historia de Usuario 1 — Registro de nuevo cliente (Prioridad: Alta)

Un visitante que nunca usó ComidApp quiere crear una cuenta ingresando su email,
contraseña y datos personales, para poder explorar menús y realizar pedidos.

**Por qué esta prioridad**: Sin registro no existe usuario que pueda operar la
plataforma. Es la puerta de entrada a todas las demás funcionalidades.

**Test independiente**: Una persona sin cuenta puede ir a la pantalla de registro,
completar el formulario con datos válidos y quedar registrada. Inmediatamente puede
acceder al menú sin pasos adicionales.

**Escenarios de aceptación**:

1. **Dado** un visitante en la pantalla de registro,
   **cuando** ingresa email único, contraseña válida, nombre, apellido y DNI,
   **entonces** se crea la cuenta y el usuario queda autenticado con acceso a la
   pantalla principal.

2. **Dado** un visitante que intenta registrarse con un email ya registrado,
   **cuando** envía el formulario,
   **entonces** se muestra "El email ya está registrado" y no se crea ninguna cuenta
   duplicada.

3. **Dado** un visitante que deja campos obligatorios vacíos,
   **cuando** intenta enviar el formulario,
   **entonces** cada campo requerido vacío muestra su mensaje de validación y el
   formulario no se procesa.

4. **Dado** un visitante que ingresa una contraseña con menos de 8 caracteres,
   **cuando** intenta registrarse,
   **entonces** se muestra "La contraseña debe tener al menos 8 caracteres" y el
   formulario no se procesa.

---

### Historia de Usuario 2 — Inicio y cierre de sesión (Prioridad: Alta)

Un cliente registrado quiere iniciar sesión con su email y contraseña para acceder
a la plataforma, y cerrar sesión cuando lo desee para proteger su cuenta en
dispositivos compartidos.

**Por qué esta prioridad**: La autenticación es un prerrequisito para toda
funcionalidad protegida del sistema.

**Test independiente**: Un usuario registrado puede iniciar sesión con credenciales
correctas, acceder al menú y su perfil, y luego cerrar sesión siendo redirigido
a la pantalla de login.

**Escenarios de aceptación**:

1. **Dado** un cliente registrado en la pantalla de login,
   **cuando** ingresa email y contraseña correctos,
   **entonces** obtiene acceso a la plataforma y es redirigido a la pantalla principal.

2. **Dado** un cliente que ingresa credenciales incorrectas,
   **cuando** intenta iniciar sesión,
   **entonces** se muestra "Credenciales incorrectas" y no se inicia ninguna sesión.

3. **Dado** un cliente con sesión activa,
   **cuando** pulsa "Cerrar sesión",
   **entonces** la sesión finaliza y es redirigido a la pantalla de login.

4. **Dado** un cliente cuya sesión expiró,
   **cuando** intenta acceder a cualquier pantalla protegida,
   **entonces** es redirigido automáticamente al login sin perder su intención de
   navegación (la URL de destino se mantiene para redirigir tras el nuevo login).

---

### Historia de Usuario 3 — Exploración del menú por local (Prioridad: Alta)

Un cliente con sesión activa quiere ver el menú de cualquiera de los 5 locales para
decidir qué pedir. Cada local debe mostrar únicamente sus productos disponibles, con
nombre, descripción, precio e imagen.

**Por qué esta prioridad**: El menú es el contenido principal de la plataforma.
Sin él no puede realizarse ningún pedido.

**Test independiente**: Un cliente puede seleccionar cualquiera de los 5 locales y
ver únicamente los productos disponibles de ese local, con nombre, descripción,
precio e imagen.

**Escenarios de aceptación**:

1. **Dado** un cliente en la pantalla principal,
   **cuando** selecciona uno de los 5 locales,
   **entonces** ve el listado de productos disponibles en ese local, cada uno con
   nombre, descripción, precio unitario e imagen.

2. **Dado** un cliente viendo el menú de un local,
   **cuando** el administrador desactivó la disponibilidad de un producto en ese local,
   **entonces** ese producto no aparece en el listado (o aparece con indicación clara
   de no disponible y sin opción de agregarlo al carrito).

3. **Dado** un cliente que cambia el local seleccionado,
   **cuando** elige un local diferente,
   **entonces** el listado se actualiza mostrando únicamente los productos de ese
   nuevo local.

4. **Dado** un cliente que no selecciona un local específico,
   **cuando** navega a la sección de productos,
   **entonces** puede ver el catálogo completo de todos los productos activos de
   todos los locales.

---

### Historia de Usuario 4 — Gestión del carrito de compras (Prioridad: Alta)

Un cliente que navega el menú de un local quiere agregar productos a un carrito,
modificar cantidades y eliminar ítems antes de confirmar el pedido. El carrito
debe mostrar el total actualizado en todo momento.

**Por qué esta prioridad**: El carrito es el mecanismo central mediante el cual
el cliente construye su pedido. Sin gestión del carrito no pueden confirmarse pedidos.

**Test independiente**: Un cliente puede agregar 2 productos, aumentar la cantidad
de uno y eliminar el otro; en cada paso el total se recalcula correctamente.

**Escenarios de aceptación**:

1. **Dado** un cliente que ve el menú de un local,
   **cuando** agrega un producto,
   **entonces** ese producto aparece en el carrito con cantidad 1 y el total se
   actualiza automáticamente.

2. **Dado** un cliente con un producto en el carrito,
   **cuando** incrementa la cantidad de ese producto,
   **entonces** la cantidad se actualiza y el total se recalcula correctamente.

3. **Dado** un cliente con varios productos en el carrito,
   **cuando** elimina un ítem,
   **entonces** ese producto desaparece del carrito y el total se actualiza.

4. **Dado** un cliente con el carrito vacío,
   **cuando** intenta avanzar a la pantalla de confirmación,
   **entonces** el sistema impide el avance y muestra "El carrito está vacío".

5. **Dado** un cliente con productos en el carrito,
   **cuando** abre el resumen,
   **entonces** ve: nombre del producto, precio unitario, cantidad, subtotal por
   ítem, costo de envío y total general.

6. **Dado** un cliente con productos de Local A en el carrito,
   **cuando** intenta agregar un producto de un Local B diferente,
   **entonces** el sistema muestra una advertencia indicando que el carrito ya
   contiene productos de otro local, y ofrece dos opciones: (a) limpiar el carrito
   y comenzar uno nuevo con el producto del nuevo local, o (b) cancelar y conservar
   el carrito actual.

7. **Dado** un cliente con un producto en el carrito que es desactivado por el
   administrador mientras el cliente navega,
   **cuando** el producto queda sin disponibilidad,
   **entonces** ese producto aparece en el carrito con precio $0 y un ícono de
   información en rojo; al hacer hover sobre el ícono el sistema informa "Este
   producto fue desactivado y no puede adquirirse". El botón de confirmar pedido
   queda deshabilitado hasta que el producto desactivado sea eliminado del carrito.

---

### Historia de Usuario 5 — Confirmación del pedido (Prioridad: Alta)

Un cliente con el carrito completo quiere confirmar su pedido seleccionando un
método de pago y revisando un resumen detallado antes de enviarlo. El sistema
debe verificar la disponibilidad de todos los productos en el momento exacto de
la confirmación.

**Por qué esta prioridad**: La confirmación del pedido es la transacción central
del sistema. Requiere el carrito (HU4) funcionando.

**Test independiente**: Un cliente con 2 productos en el carrito puede seleccionar
"Tarjeta", revisar el resumen completo, confirmar y recibir un número de pedido
con estado RECIBIDO.

**Escenarios de aceptación**:

1. **Dado** un cliente con carrito no vacío en la pantalla de confirmación,
   **cuando** selecciona un método de pago,
   **entonces** ve un resumen completo: productos con cantidades y precios unitarios,
   total, local, método de pago seleccionado y fecha/hora de la solicitud.

2. **Dado** un cliente que confirma un pedido con todos los productos disponibles,
   **cuando** el sistema verifica disponibilidad,
   **entonces** el pedido se registra con estado RECIBIDO y el cliente recibe
   confirmación con el número de pedido asignado.

3. **Dado** un cliente que confirma un pedido y al menos un producto se volvió
   no disponible entre el momento de agregar al carrito y la confirmación,
   **cuando** el sistema verifica disponibilidad,
   **entonces** el pedido es rechazado, el cliente es informado de qué producto(s)
   ya no están disponibles y puede actualizar el carrito.

4. **Dado** un cliente que no seleccionó método de pago,
   **cuando** intenta confirmar el pedido,
   **entonces** el sistema impide el avance y solicita seleccionar un método de pago.

5. **Dado** un cliente cuyo carrito contiene al menos un producto desactivado,
   **cuando** intenta avanzar a la confirmación,
   **entonces** el botón de confirmar permanece deshabilitado y los productos
   desactivados están marcados visualmente en rojo, con un mensaje indicando que
   deben eliminarse antes de poder confirmar el pedido.

6. **Dado** un pedido confirmado exitosamente,
   **entonces** el registro incluye: productos con cantidades y precios capturados
   al momento de la confirmación, precio total, local, método de pago, fecha/hora
   de creación y estado RECIBIDO.

---

### Historia de Usuario 6 — Pago con tarjeta de crédito o débito (Prioridad: Alta)

Un cliente que seleccionó pago con tarjeta quiere completar el pago de forma segura
a través de una pasarela de pago externa. El pedido solo debe proceder si el pago
es aprobado; si es rechazado, el cliente debe poder reintentar.

**Por qué esta prioridad**: El pago con tarjeta es uno de los dos métodos de pago
requeridos en el MVP.

**Test independiente**: Un cliente puede completar el flujo de pago con tarjeta
vía pasarela externa; si aprobado, el pedido queda confirmado con pago CONFIRMADO;
si rechazado, se informa al cliente con opción de reintentar.

**Escenarios de aceptación**:

1. **Dado** un cliente que selecciona tarjeta y confirma el pedido,
   **cuando** es redirigido a la pasarela de pago,
   **entonces** puede ingresar sus datos de tarjeta en la interfaz segura de la
   pasarela (fuera del entorno de ComidApp).

2. **Dado** un cliente que completa exitosamente el pago en la pasarela,
   **cuando** el sistema recibe la confirmación de pago,
   **entonces** el estado del pago del pedido cambia a CONFIRMADO y el pedido
   avanza en su flujo de preparación.

3. **Dado** un cliente cuyo pago es rechazado por la pasarela,
   **cuando** el sistema recibe la notificación de rechazo,
   **entonces** el estado del pago queda en RECHAZADO, el pedido no procede, y el
   cliente ve un mensaje claro con opción de reintentar con otra tarjeta.

4. **Dado** cualquier transacción con tarjeta,
   **entonces** ComidApp no almacena, procesa ni registra datos de tarjeta en
   ningún punto del sistema.

---

### Historia de Usuario 7 — Pago en efectivo con confirmación del repartidor (Prioridad: Alta)

Un cliente que selecciona pago en efectivo quiere que su pedido se confirme sin
pago previo. Al momento de la entrega, el repartidor confirma la recepción del
dinero, lo cual finaliza el ciclo económico del pedido.

**Por qué esta prioridad**: El pago en efectivo es el segundo método de pago
requerido en el MVP.

**Test independiente**: Un cliente selecciona efectivo y confirma el pedido (pago
queda PENDIENTE). Al entregar, el repartidor marca el cobro como recibido y el
pedido pasa a ENTREGADO con pago CONFIRMADO.

**Escenarios de aceptación**:

1. **Dado** un cliente que selecciona efectivo y confirma el pedido,
   **cuando** el pedido es registrado,
   **entonces** el estado del pago queda en PENDIENTE y el pedido avanza
   normalmente sin requerir ningún pago anticipado.

2. **Dado** un repartidor que entregó el pedido y cobró el efectivo,
   **cuando** confirma el cobro en su pantalla de pedidos,
   **entonces** el estado del pago cambia a CONFIRMADO y el pedido pasa al
   estado ENTREGADO.

3. **Dado** un pedido con pago en efectivo en estado PENDIENTE,
   **cuando** el repartidor no confirma el cobro,
   **entonces** el pedido no puede pasar a estado ENTREGADO.

---

### Historia de Usuario 8 — Seguimiento del estado del pedido (Prioridad: Alta)

Un cliente que realizó un pedido quiere consultar su estado actual en cualquier
momento para saber si está siendo preparado, en camino o ya fue entregado.
Los estados posibles son: RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO.

**Por qué esta prioridad**: El seguimiento del pedido es parte central de la
experiencia de entrega y es parte del MVP comprometido.

**Test independiente**: Un cliente puede acceder a la pantalla de su pedido activo
y ver el estado actualizado a medida que el pedido avanza en el proceso.

**Escenarios de aceptación**:

1. **Dado** un cliente con un pedido activo,
   **cuando** accede al detalle del pedido,
   **entonces** ve el estado actual claramente indicado (RECIBIDO / EN_PREPARACION
   / EN_CAMINO / ENTREGADO) con representación visual del progreso.

2. **Dado** un cliente que consulta su pedido activo tras un cambio de estado,
   **cuando** la pantalla se actualiza,
   **entonces** ve el estado más reciente reflejado.

3. **Dado** un cliente autenticado,
   **cuando** intenta acceder al detalle de un pedido que no le pertenece,
   **entonces** el sistema rechaza el acceso y muestra un error de autorización.

4. **Dado** un pedido ya entregado,
   **cuando** el cliente accede a su detalle,
   **entonces** ve el estado final ENTREGADO con la fecha y hora de entrega registrada.

---

### Historia de Usuario 9 — Perfil e historial de pedidos (Prioridad: Media)

Un cliente quiere ver y actualizar sus datos personales (dirección de entrega,
teléfono) y consultar el historial completo de todos sus pedidos con detalles de
cada uno.

**Por qué esta prioridad**: Importante para la retención de usuarios, pero no
bloquea el flujo principal de pedidos.

**Test independiente**: Un cliente puede editar su dirección, guardar los cambios
y ver en el historial todos sus pedidos anteriores con fecha, estado y total.

**Escenarios de aceptación**:

1. **Dado** un cliente en la pantalla de perfil,
   **cuando** accede a sus datos,
   **entonces** ve: nombre, apellido, email y dirección de entrega registrada.

2. **Dado** un cliente que modifica su dirección de entrega y guarda,
   **cuando** vuelve a acceder al perfil,
   **entonces** ve la nueva dirección guardada correctamente.

3. **Dado** un cliente en la sección de historial de pedidos,
   **cuando** visualiza la lista,
   **entonces** ve los pedidos ordenados por fecha descendente, mostrando para
   cada uno: fecha, local, total, método de pago y estado final.

4. **Dado** un cliente que selecciona un pedido del historial,
   **cuando** abre el detalle,
   **entonces** ve la lista completa de productos con cantidad, precio unitario
   al momento del pedido y subtotal por ítem.

---

### Historia de Usuario 10 — Notificaciones de cambio de estado del pedido (Prioridad: Media)

Un cliente con un pedido activo quiere recibir una notificación cuando el estado
de su pedido cambia, incluso si tiene la aplicación cerrada o en segundo plano,
para estar siempre informado del progreso sin tener que consultar manualmente la
pantalla de seguimiento.

**Decisión de Q1**: Las notificaciones llegan vía **Web Push del navegador**, tanto
con la aplicación activa como cuando está cerrada o en segundo plano. El usuario
debe otorgar permiso de notificaciones al registrarse o al realizar su primer pedido.

**Por qué esta prioridad**: El seguimiento del pedido es fundamental para la
experiencia de entrega; el cliente necesita saber el avance sin depender de abrir
la app repetidamente.

**Test independiente**: Un cliente que otorgó permisos de notificación recibe una
notificación push del navegador cuando el estado de su pedido activo cambia, tanto
con la app abierta como con la app cerrada.

**Escenarios de aceptación**:

1. **Dado** un cliente con pedido activo que otorgó permiso de notificaciones push,
   **cuando** el estado del pedido cambia (cualquier transición),
   **entonces** recibe una notificación push del navegador con el nuevo estado y el
   número de pedido dentro de los 30 segundos siguientes al cambio, independientemente
   de si la aplicación está abierta o cerrada.

2. **Dado** un cliente cuyo pedido pasa a estado ENTREGADO,
   **cuando** llega la notificación push,
   **entonces** la notificación incluye el nuevo estado, el número de pedido y
   confirmación de entrega realizada.

3. **Dado** un cliente que no otorgó permiso de notificaciones push,
   **cuando** el estado de su pedido cambia,
   **entonces** la aplicación muestra el nuevo estado en la pantalla de seguimiento
   al próximo acceso, sin enviar notificación push (degradación elegante sin bloquear
   el flujo del pedido).

4. **Dado** un cliente que abre la pantalla de su pedido activo en cualquier momento,
   **cuando** el estado cambió mientras estaba fuera de la app,
   **entonces** ve el estado actualizado correctamente (el push y la pantalla son
   consistentes).

---

### Historia de Usuario 11 — Repartidor: gestión de pedidos asignados (Prioridad: Media)

Un repartidor con sesión activa quiere ver únicamente sus pedidos asignados,
actualizar el estado de cada pedido a medida que avanza la entrega, y confirmar
el cobro en efectivo al finalizar.

**Decisión de Q2 — Regla híbrida de transición RECIBIDO → EN_PREPARACION**:
- **Pedidos con pago con tarjeta**: el sistema avanza automáticamente a EN_PREPARACION
  en el instante en que la pasarela confirma el pago. No requiere acción manual.
  No tiene sentido iniciar la preparación antes de saber que el pago fue aprobado.
- **Pedidos con pago en efectivo**: el repartidor gestiona todo el flujo de estados
  (RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO), ya que el cobro ocurre al
  final y no existe confirmación de pago anticipada.

**Por qué esta prioridad**: Necesario para el ciclo de vida completo del pedido,
aunque el pedido puede existir sin interacción del repartidor hasta la fase de entrega.

**Test independiente**: Un repartidor puede iniciar sesión, ver sus pedidos asignados,
avanzar un pedido en efectivo desde RECIBIDO hasta ENTREGADO, y confirmar el cobro.
Para un pedido con tarjeta, verifica que ya llegó en estado EN_PREPARACION sin su
intervención.

**Escenarios de aceptación**:

1. **Dado** un repartidor con sesión activa,
   **cuando** accede a su pantalla de pedidos,
   **entonces** ve únicamente los pedidos asignados a él con: estado actual, nombre
   del cliente, dirección de entrega y método de pago.

2. **Dado** un pedido con pago en tarjeta cuyo pago fue confirmado por la pasarela,
   **cuando** el sistema recibe la confirmación de pago,
   **entonces** el estado del pedido avanza automáticamente de RECIBIDO a
   EN_PREPARACION sin intervención del repartidor ni del administrador.

3. **Dado** un pedido con pago en efectivo recién creado (estado RECIBIDO),
   **cuando** el repartidor verifica que el local comenzó la preparación,
   **entonces** puede avanzar el estado a EN_PREPARACION manualmente.

4. **Dado** un repartidor con un pedido en EN_PREPARACION listo para retirar del local,
   **cuando** lo recoge,
   **entonces** puede avanzar el estado a EN_CAMINO.

5. **Dado** un repartidor con un pedido en EN_CAMINO,
   **cuando** completa la entrega,
   **entonces** puede avanzar el estado a ENTREGADO.

6. **Dado** un repartidor que completa la entrega de un pedido en efectivo,
   **cuando** avanza a ENTREGADO,
   **entonces** también confirma el cobro del efectivo, cambiando el estado del
   pago a CONFIRMADO en el mismo acto.

7. **Dado** un repartidor autenticado,
   **cuando** intenta ver o modificar pedidos asignados a otro repartidor,
   **entonces** el sistema rechaza el acceso con error de autorización.

---

### Historia de Usuario 12 — Administrador: gestión del catálogo por local (Prioridad: Media)

Un administrador quiere gestionar el catálogo de productos: activar/desactivar
disponibilidad de un producto para un local específico, actualizar precios, agregar
nuevos productos y eliminar productos del menú de un local.

**Por qué esta prioridad**: El control del catálogo es esencial para las operaciones
del negocio, aunque la configuración inicial puede realizarse previo al lanzamiento.

**Test independiente**: Un admin puede desactivar un producto en un local específico
y verificar que ese producto ya no aparece en el menú de ese local, sin afectar
su disponibilidad en otros locales donde estaba configurado.

**Escenarios de aceptación**:

1. **Dado** un administrador en el panel de productos,
   **cuando** desactiva un producto para un local específico,
   **entonces** ese producto deja de aparecer como disponible en el menú de ese
   local sin afectar a los demás locales.

2. **Dado** un administrador que actualiza el precio de un producto,
   **cuando** guarda el cambio,
   **entonces** los nuevos pedidos usan el precio actualizado; los pedidos ya
   confirmados conservan el precio original al momento de su confirmación.

3. **Dado** un administrador que elimina un producto del menú de un local,
   **cuando** confirma la eliminación,
   **entonces** el producto ya no aparece en ese menú y no puede ser agregado
   al carrito desde ese local.

4. **Dado** un administrador que agrega un nuevo producto,
   **cuando** completa nombre, descripción, precio e imagen y lo guarda,
   **entonces** el producto queda disponible en el/los local(es) configurado(s).

---

### Historia de Usuario 13 — Administrador: gestión de usuarios y repartidores (Prioridad: Baja)

Un administrador quiere registrar nuevos repartidores, ver la lista de todos los
clientes y repartidores registrados, y poder dar de baja usuarios cuando sea necesario.

**Por qué esta prioridad**: Herramienta operativa importante, pero no bloquea
el flujo del cliente ni el lanzamiento inicial del MVP.

**Test independiente**: Un admin puede registrar un repartidor que luego inicia sesión
correctamente. El admin también puede ver el listado completo de usuarios del sistema.

**Escenarios de aceptación**:

1. **Dado** un administrador que registra un nuevo repartidor con nombre, apellido,
   DNI y email,
   **cuando** completa el registro,
   **entonces** el repartidor recibe acceso al sistema y puede iniciar sesión.

2. **Dado** un administrador en la pantalla de usuarios,
   **cuando** accede al listado,
   **entonces** ve todos los clientes y repartidores registrados con: nombre,
   apellido, email y tipo de usuario.

3. **Dado** un administrador que elimina un usuario,
   **cuando** confirma la acción,
   **entonces** ese usuario ya no puede iniciar sesión y es removido del listado activo.

4. **Dado** cualquier usuario no-administrador,
   **cuando** intenta acceder al panel de gestión de usuarios,
   **entonces** el sistema rechaza el acceso.

---

### Casos Borde

- Un producto se desactiva **exactamente mientras** un cliente está en la pantalla de
  confirmación del pedido: el sistema verifica disponibilidad en el instante de la
  confirmación (no al agregar al carrito), rechaza el pedido e informa al cliente.

- **No hay repartidores disponibles** al confirmar un pedido: el sistema debe informar
  al cliente con un mensaje claro en lugar de crear un pedido sin repartidor asignado.

- La **pasarela de pago falla o no responde** durante la confirmación: el estado del
  pago queda en RECHAZADO, el pedido permanece creado sin avanzar, y el cliente puede
  reintentar el pago desde la pantalla del pedido. El pedido no queda en estado
  inconsistente.

- Un cliente intenta confirmar un **pedido con el carrito vacío**: el sistema impide el
  avance en la pantalla de carrito, antes de llegar a la confirmación.

- Un repartidor intenta avanzar el estado de un pedido **saltando fases** (p. ej. de
  RECIBIDO a ENTREGADO directamente): el sistema rechaza la transición y retorna
  un error indicando la secuencia válida.

- Un cliente intenta ver el detalle de un pedido usando un **ID que no le pertenece**:
  el sistema retorna error de autorización (no expone datos de otros usuarios).

- El administrador intenta registrar un repartidor con un **DNI ya existente** en el
  sistema: el sistema retorna un conflicto e informa al administrador.

- El precio de un producto se actualiza mientras hay **pedidos activos** que contienen
  ese producto: los pedidos ya confirmados conservan el precio capturado al momento
  de su creación.

- Un cliente tiene la aplicación activa en **dos dispositivos simultáneos**: los cambios
  de estado del pedido deben ser consistentes en ambos.

- Un cliente intenta agregar al carrito un producto de un **local diferente** al del
  carrito activo: el sistema muestra advertencia con las dos opciones (limpiar carrito
  o cancelar). No se mezclan productos de distintos locales en un mismo pedido.

- Un producto presente en el carrito activo es **desactivado en tiempo real** por el
  administrador: aparece con precio $0 e ícono rojo informativo; el botón de confirmar
  queda deshabilitado hasta que el cliente elimine el producto desactivado del carrito.

- Un pedido permanece en estado **RECIBIDO durante más de 1 hora** sin avanzar: el
  sistema lo cancela automáticamente y notifica al cliente vía Web Push. Si el pago
  era con tarjeta y ya estaba CONFIRMADO, el sistema inicia el proceso de reversa con
  la pasarela de pago.

- Un cliente intenta confirmar un pedido **fuera del horario de atención** del local
  seleccionado: el botón de confirmación está deshabilitado y el sistema muestra el
  horario de atención del local.

---

## Requisitos

### Requisitos Funcionales

- **FR-001**: El sistema DEBE permitir registrar un nuevo usuario con email único,
  contraseña (mínimo 8 caracteres), nombre, apellido y DNI.

- **FR-002**: El sistema DEBE autenticar a los usuarios con sesiones seguras de
  duración configurable; al expirar, redirigir al login.

- **FR-003**: El sistema DEBE permitir el cierre de sesión explícito en cualquier momento.

- **FR-004**: El sistema DEBE exponer el catálogo de productos diferenciado por cada
  uno de los 5 locales.

- **FR-005**: Cada producto DEBE contar con: nombre, descripción, precio unitario,
  imagen y estado de disponibilidad.

- **FR-006**: El sistema DEBE mostrar en el menú de un local únicamente los productos
  activos para ese local; los no disponibles no deben aparecer o deben estar claramente
  marcados como no disponibles.

- **FR-007**: El sistema DEBE permitir al cliente agregar productos al carrito desde
  el menú de un local, especificando cantidad.

- **FR-007b**: El carrito está restringido a un único local por sesión de compra. Al
  intentar agregar un producto de un local diferente al del carrito activo, el sistema
  DEBE mostrar una advertencia con dos opciones: (a) limpiar el carrito y comenzar uno
  nuevo con el nuevo local, o (b) cancelar y conservar el carrito actual.

- **FR-007c**: Si un producto presente en el carrito activo es desactivado por el
  administrador, el sistema DEBE reflejarlo en tiempo real: el producto aparece con
  precio $0 y un ícono de información en rojo. Al consultar el ícono, se informa
  "Este producto ya no está disponible". El botón de confirmar pedido queda
  deshabilitado mientras el carrito contenga productos desactivados.

- **FR-008**: El sistema DEBE permitir al cliente modificar la cantidad de cualquier
  producto en el carrito.

- **FR-009**: El sistema DEBE permitir al cliente eliminar productos individuales del
  carrito.

- **FR-010**: El sistema DEBE calcular y mostrar el total del carrito en tiempo real,
  incluyendo el costo de envío, actualizándose ante cada modificación.

- **FR-011**: El sistema DEBE verificar la disponibilidad de los productos del carrito
  en dos momentos:
  - **En tiempo real durante la navegación**: los productos desactivados se marcan
    automáticamente en el carrito (precio $0 + ícono rojo); verificación informativa
    y visual que no bloquea la sesión.
  - **En el instante de la confirmación**: validación final bloqueante; si algún
    producto no está disponible el pedido es rechazado y el cliente debe actualizar
    el carrito. Esta doble verificación evita vender productos desactivados y minimiza
    la necesidad de reversar cobros posteriores.

- **FR-012**: El sistema DEBE requerir la selección de un método de pago (TARJETA o
  EFECTIVO) como condición previa a la confirmación del pedido.

- **FR-013**: El pedido confirmado DEBE registrar: productos con cantidades y precios
  al momento de la confirmación, precio total, local, método de pago, fecha/hora de
  creación y estado RECIBIDO.

- **FR-014**: El sistema DEBE soportar pago con tarjeta mediante integración con una
  pasarela de pago externa, sin almacenar ni procesar datos de tarjeta en ninguna
  instancia del sistema.

- **FR-015**: El sistema DEBE soportar pago en efectivo contra entrega, registrando
  el estado del pago como PENDIENTE al crear el pedido.

- **FR-016**: El sistema DEBE permitir al repartidor confirmar el cobro en efectivo
  al completar la entrega, actualizando el estado del pago a CONFIRMADO.

- **FR-017**: El estado del pedido DEBE seguir la secuencia única:
  RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO.
  El sistema DEBE rechazar cualquier transición fuera de este orden.

- **FR-017b**: La responsabilidad de la transición RECIBIDO → EN_PREPARACION varía
  según el método de pago:
  - **Pago con tarjeta**: el sistema avanza automáticamente al estado EN_PREPARACION
    en cuanto la pasarela confirma el pago (CONFIRMADO). No requiere acción humana.
  - **Pago en efectivo**: el repartidor asignado avanza manualmente el estado a
    EN_PREPARACION cuando el local comienza la preparación.
  En ningún caso el administrador es responsable de esta transición.

- **FR-018**: El sistema DEBE notificar al cliente vía Web Push del navegador cada
  vez que el estado de su pedido cambie, tanto si la aplicación está activa como si
  está cerrada o en segundo plano. El cliente DEBE poder otorgar o rechazar el permiso
  de notificaciones. Si el permiso es rechazado, la pantalla de seguimiento debe
  seguir mostrando el estado actualizado al próximo acceso (degradación elegante).

- **FR-019**: El sistema DEBE mostrar al cliente el estado actual de su pedido en
  la pantalla de seguimiento, con indicación visual del progreso.

- **FR-020**: El sistema DEBE permitir al cliente consultar su historial de pedidos
  paginado (10 pedidos por página), ordenado por fecha descendente. El listado muestra
  por pedido: fecha, local, total, método de pago y estado final. Al seleccionar un
  pedido se accede al detalle completo con todos los ítems, cantidades y precios
  unitarios al momento de la confirmación.

- **FR-021**: El sistema DEBE permitir al cliente ver y editar sus datos de perfil
  (dirección de entrega, teléfono).

- **FR-022**: El sistema DEBE permitir al administrador activar y desactivar la
  disponibilidad de un producto para un local específico, de forma independiente
  a otros locales.

- **FR-023**: El sistema DEBE permitir al administrador actualizar el precio de un
  producto; los pedidos ya confirmados conservan el precio original.

- **FR-024**: El sistema DEBE permitir al administrador eliminar un producto del
  menú de un local.

- **FR-025**: El sistema DEBE permitir al administrador agregar nuevos productos
  al catálogo de un local.

- **FR-026**: El sistema DEBE permitir al administrador registrar nuevos repartidores
  con sus datos personales.

- **FR-027**: El sistema DEBE permitir al administrador visualizar la lista completa
  de usuarios registrados (clientes y repartidores) con sus datos básicos.

- **FR-028**: El sistema DEBE permitir al administrador eliminar usuarios del sistema.

- **FR-029**: El sistema DEBE mostrar al repartidor únicamente los pedidos asignados
  a él, sin exponer pedidos de otros repartidores.

- **FR-030**: El sistema DEBE permitir al repartidor avanzar el estado de sus pedidos
  en la secuencia válida (EN_PREPARACION → EN_CAMINO → ENTREGADO).

- **FR-031**: El acceso al detalle de un pedido DEBE estar restringido al cliente
  propietario, al repartidor asignado y al administrador.

- **FR-032**: El sistema DEBE cancelar automáticamente cualquier pedido que permanezca
  en estado RECIBIDO durante más de 1 hora consecutiva sin avanzar al siguiente estado.
  Al cancelarse, el sistema notifica al cliente vía Web Push (motivo: tiempo de espera
  excedido). Si el método de pago era tarjeta y el pago estaba CONFIRMADO, el sistema
  DEBE iniciar el proceso de reversa con la pasarela de pago.

- **FR-033**: El sistema DEBE bloquear la confirmación de nuevos pedidos cuando el
  local seleccionado está fuera de su horario de atención registrado. En ese caso,
  el botón de confirmar pedido estará deshabilitado y se mostrará el horario de
  atención vigente del local al cliente.

### Requisitos No Funcionales

- **NFR-001 — Rendimiento**: El contenido principal visible del menú de un local
  (LCP) debe cargarse en menos de 2,5 segundos en condiciones de red móvil estándar.
  Las consultas de disponibilidad de productos y estado de pedidos deben resolverse
  con búsquedas optimizadas en la base de datos, sin recorrer colecciones completas
  en memoria.

- **NFR-002 — Seguridad**: Todos los endpoints protegidos deben rechazar solicitudes
  sin sesión válida. La comunicación entre cliente y servidor debe realizarse
  exclusivamente por canal cifrado (HTTPS). ComidApp no almacena ni procesa datos de
  tarjeta; el cumplimiento de normativas de pago está delegado a la pasarela externa.

- **NFR-003 — Usabilidad**: La interfaz debe ser mobile-first y responsiva, funcional
  en smartphones, tablets y desktop. El flujo completo de "seleccionar local →
  agregar producto → confirmar pedido" debe completarse en 4 pasos o menos desde la
  pantalla principal.

- **NFR-004 — Disponibilidad**: El sistema debe estar operativo durante el horario de
  atención de los locales. El fallo de un componente opcional (como la pasarela de
  pago) no debe inhabilitar el resto de las funcionalidades del sistema.

- **NFR-005 — Escalabilidad**: La arquitectura debe soportar el crecimiento planificado
  desde los ~500 clientes actuales sin requerir rediseño estructural para escalar.

- **NFR-006 — Consistencia de datos**: El precio de cada producto en un pedido
  confirmado es inmutable; refleja el valor vigente al momento de la confirmación.

- **NFR-007 — Trazabilidad de estados**: Cada transición de estado de un pedido debe
  quedar registrada con la fecha/hora del cambio y el actor responsable.

- **NFR-008 — Separación de roles**: Un cliente no puede ejecutar acciones de
  repartidor ni de administrador. Un repartidor no puede acceder al panel de
  administración. Los roles son mutuamente excluyentes en cuanto a permisos de acción.

### Entidades Clave

- **Usuario**: persona registrada en el sistema. Subtipos: Cliente, Repartidor,
  Administrador. Atributos comunes: DNI, nombre, apellido, email, contraseña,
  teléfono, rol.

- **Local**: uno de los 5 establecimientos físicos. Atributos: nombre, dirección,
  barrio, tiempo estimado de entrega, horario de atención (franjas horarias de
  apertura y cierre por día de la semana, almacenadas como datos estáticos en el
  sistema). El horario es usado para bloquear la confirmación de pedidos fuera de
  los días y horas configurados.

- **Producto**: ítem del catálogo (hamburguesa u otro artículo). Atributos: nombre,
  descripción, precio base, imagen.

- **ItemDeMenu**: relación entre un Producto y un Local. Define si el producto está
  disponible en ese local y su precio específico para ese local (que puede diferir
  del precio base). Permite que el mismo producto tenga distinta disponibilidad y
  precio en cada local.

- **Carrito**: estructura transitoria que el cliente mantiene mientras navega. Contiene
  ítems de un único local (no se mezclan productos de locales distintos), con producto
  y cantidad. Calcula el total incluyendo el costo de envío. No persiste definitivamente
  hasta la confirmación. Los productos desactivados se marcan en tiempo real.

- **Pedido**: orden de compra confirmada. Contiene los ítems con cantidades y precios
  capturados al momento de la confirmación, el local, el cliente, el repartidor
  asignado, método de pago, fecha/hora de creación, estado del pedido y estado del pago.

- **ItemPedido**: ítem dentro de un pedido confirmado. Captura el precio unitario
  vigente al instante de la confirmación (inmutable).

- **Pago**: registro del estado económico del pedido. Estados posibles:
  PENDIENTE / CONFIRMADO / RECHAZADO. Vinculado a un método de pago (TARJETA o
  EFECTIVO).

---

## Criterios de Éxito

### Resultados Medibles

- **CE-001**: Un nuevo usuario puede completar el registro y realizar su primer pedido
  en menos de 5 minutos desde que abre la aplicación por primera vez.

- **CE-002**: El flujo completo de "seleccionar local → ver menú → agregar al carrito
  → confirmar pedido" se completa en 4 pasos o menos desde la pantalla principal.

- **CE-003**: El contenido principal visible del menú de cualquier local (LCP) carga
  en menos de 2,5 segundos en condiciones de red móvil estándar.

- **CE-004**: El 100% de los pedidos confirmados registran correctamente el precio de
  cada producto vigente al momento de la confirmación; los precios no mutan ante
  actualizaciones posteriores del administrador.

- **CE-005**: El sistema rechaza el 100% de los intentos de acceso a datos de pedidos
  de otros usuarios con respuesta de acceso denegado.

- **CE-006**: Las transiciones de estado del pedido solo pueden avanzar en la secuencia
  RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO; el sistema rechaza el 100%
  de las transiciones fuera de ese orden.

- **CE-007**: Un cliente con la aplicación activa ve el cambio de estado de su pedido
  reflejado en pantalla dentro de los 30 segundos siguientes al cambio.

- **CE-008**: Con ~500 clientes activos simultáneamente, las páginas de menú, estado
  de pedido e historial cargan en menos de 3 segundos por solicitud. El tiempo de
  respuesta no aumenta progresivamente a medida que crece el volumen de pedidos
  almacenados en la base de datos.

- **CE-009**: ComidApp no almacena ni registra datos de tarjeta en ninguna instancia
  del sistema, verificable mediante auditoría de base de datos y logs.

- **CE-010**: El 95% de los pedidos con pago en efectivo tienen su estado de pago
  actualizado a CONFIRMADO antes de que transcurran 5 minutos desde el estado ENTREGADO.

---

## Supuestos

- Los 5 locales están predefinidos en el sistema antes del lanzamiento; no cambian
  durante el MVP. Incorporar nuevos locales requiere una nueva iteración.

- Un mismo producto puede existir en varios locales con distinta disponibilidad y
  precio por local.

- La dirección de entrega del cliente se captura en el perfil del usuario; el sistema
  usa esa dirección para todos los pedidos sin solicitarla nuevamente en cada compra.

- El costo de envío es un valor fijo conocido al armar el carrito; no varía por
  distancia ni por local.

- El repartidor se asigna automáticamente por el sistema al confirmar el pedido
  (sin lógica de asignación manual en el MVP).

- El horario de atención de cada local está almacenado como datos estáticos en el
  sistema (días y franjas horarias de apertura y cierre por local). El sistema usa
  estos datos para bloquear la confirmación de pedidos fuera de horario. No se incluye
  gestión dinámica del calendario (feriados, cambios excepcionales de horario) en el MVP.

- Las notificaciones de estado se implementan vía **Web Push del navegador** y
  funcionan tanto con la aplicación abierta como cerrada o en segundo plano.
  Si el cliente no otorga permiso de notificaciones, la pantalla de seguimiento
  sigue mostrando el estado actualizado al próximo acceso (sin error, sin bloqueo).

- La transición RECIBIDO → EN_PREPARACION depende del método de pago: automática
  al confirmar el pago con tarjeta; manual por el repartidor en pedidos en efectivo.
  El administrador no interviene en la gestión del flujo de estados de pedidos.

- El administrador es único y global; no existe un admin por local.

- No existe un flujo de cancelación de pedido una vez confirmado en el MVP.

- El sistema de registro y login con email y contraseña es suficiente para el MVP;
  no se requiere autenticación con redes sociales ni SSO.

- La pasarela de pago retorna resultado de la transacción (aprobado/rechazado) por
  redirección o notificación; el sistema registra el resultado sin procesar datos
  de tarjeta.

- Un cliente puede tener múltiples pedidos activos simultáneamente si así lo desea.

- El administrador puede eliminar usuarios del sistema (clientes y repartidores),
  acción que invalida la sesión activa del usuario eliminado.

---

## Clarificaciones

### Sesión 2026-06-09

- Q: ¿El carrito puede contener productos de más de un local al mismo tiempo? → A: No. Un carrito solo puede contener productos de un único local. Si el cliente intenta agregar un producto de otro local, el sistema muestra una advertencia y ofrece limpiar el carrito para iniciar uno nuevo, o conservar el actual. Motivo: cada local tiene su propio repartidor y costo de envío.

- Q: ¿Qué se muestra cuando un producto se desactiva o queda sin stock mientras está en el carrito activo? → A: El producto permanece visible en el carrito con precio $0 y un ícono de información en rojo. Al hacer hover el sistema informa "Este producto fue desactivado y no puede adquirirse". El botón de confirmar queda deshabilitado hasta que el producto desactivado sea eliminado del carrito.

- Q: ¿Existe un tiempo límite para que el pedido avance desde RECIBIDO antes de cancelarse? → A: Sí, 1 hora. Si un pedido permanece en estado RECIBIDO más de 1 hora consecutiva sin avanzar, el sistema lo cancela automáticamente y notifica al cliente vía Web Push. Si el pago con tarjeta ya estaba confirmado, el sistema inicia la reversa.

- Q: ¿El historial de pedidos tiene paginación? ¿Cuántos por página? → A: Sí, el historial tiene paginación de 10 pedidos por página, ordenados por fecha descendente.

- Q: ¿Qué estado queda el pedido si la pasarela de pago falla durante la confirmación? → A: El estado del pago queda en RECHAZADO, el pedido permanece creado sin avanzar al flujo de preparación. El cliente puede reintentar el pago desde la pantalla del pedido.

- Q: ¿Se pueden confirmar pedidos fuera del horario de atención de los locales? → A: No. El sistema bloquea la confirmación fuera del horario de atención. El botón de confirmar está deshabilitado y se muestra el horario vigente del local. El horario se almacena como datos estáticos por local.

- Q: ¿La disponibilidad de productos se verifica en tiempo real en el carrito, o solo al confirmar? → A: En ambos momentos. En tiempo real durante la navegación (visual, informativo: precio $0 + ícono rojo para productos desactivados) y al confirmar (bloqueante). La doble verificación evita vender productos no disponibles y minimiza la necesidad de reversas de cobro.

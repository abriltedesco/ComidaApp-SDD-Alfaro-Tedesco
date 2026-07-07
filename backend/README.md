# ComidApp Backend — API REST

API REST para delivery de comida rápida. Spring Boot 4.0.5 + Java 21 + MySQL 8.

## Arrancar

```bash
cd backend
./gradlew bootRun --no-daemon
```

> La app se conecta a MySQL `comida_app` en localhost:3306 (user: root, pass: alumnoipm).
> Al arrancar, si la DB está vacía, carga datos de prueba automáticamente.

## Credenciales de Prueba

| Rol | Mail | Contraseña | DNI |
|-----|------|-----------|-----|
| **ADMIN** | admin@comidapp.ar | admin123 | 99999999 |
| **CLIENTE** | juan@test.com | admin123 | 12345678 |
| **CLIENTE** | laura@test.com | admin123 | 22334455 |
| **CLIENTE** | pedro@test.com | admin123 | 33445566 |
| **REPARTIDOR** | maria@test.com | admin123 | 87654321 |
| **REPARTIDOR** | carlos@test.com | admin123 | 11223344 |

## Endpoints

### 🔓 Públicos (sin autenticación)

```bash
# Health check
curl http://localhost:3001/api/v1/health

# Login (devuelve JWT)
curl 'http://localhost:3001/api/v1/auth/login?mail=juan@test.com&contrasenia=admin123'

# Registro cliente
curl -X POST http://localhost:3001/api/v1/auth/registro/cliente \
  -H "Content-Type: application/json" \
  -d '{"dni":55667788,"nombre":"Nuevo","apellido":"Usuario","mail":"nuevo@test.com","contrasenia":"clave123","telefono":351111222,"dirEntrega":"Calle 1","ciudad":"Cordoba"}'
```

### 🔒 Autenticados (requieren Header: `Authorization: Bearer <token>`)

#### Menú y Productos
```bash
# Listar locales
curl http://localhost:3001/api/v1/locales -H "Authorization: Bearer $TOKEN"

# Menú de un local (con filtro opcional por categoría)
curl 'http://localhost:3001/api/v1/locales/1/menu' -H "Authorization: Bearer $TOKEN"
curl 'http://localhost:3001/api/v1/locales/1/menu?categoria=hamburguesas' -H "Authorization: Bearer $TOKEN"

# Buscar productos (por nombre y/o categoría)
curl 'http://localhost:3001/api/v1/productos/buscar?nombre=doble' -H "Authorization: Bearer $TOKEN"
curl 'http://localhost:3001/api/v1/productos/buscar?categoria=bebidas' -H "Authorization: Bearer $TOKEN"
```

#### Carrito
```bash
# Ver carrito
curl http://localhost:3001/api/v1/carrito -H "Authorization: Bearer $TOKEN"

# Agregar item
curl -X POST http://localhost:3001/api/v1/carrito/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productoId":1,"cantidad":2}'

# Modificar cantidad
curl -X PUT http://localhost:3001/api/v1/carrito/items/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cantidad":3}'

# Eliminar item
curl -X DELETE http://localhost:3001/api/v1/carrito/items/1 -H "Authorization: Bearer $TOKEN"

# Vaciar carrito
curl -X DELETE http://localhost:3001/api/v1/carrito -H "Authorization: Bearer $TOKEN"
```

#### Pedidos
```bash
# Confirmar pedido (desde carrito)
curl -X POST http://localhost:3001/api/v1/pedidos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"metodoPago":"EFECTIVO"}'

# Mis pedidos
curl http://localhost:3001/api/v1/pedidos/mis-pedidos -H "Authorization: Bearer $TOKEN"

# Pedidos del repartidor (requiere rol REPARTIDOR)
curl http://localhost:3001/api/v1/pedidos/repartidor -H "Authorization: Bearer $TOKEN_REP"

# Actualizar estado (requiere ADMIN o REPARTIDOR)
curl -X PUT http://localhost:3001/api/v1/pedidos/1/estado \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"CONFIRMADO"}'
```

#### Perfil
```bash
# Ver perfil
curl http://localhost:3001/api/v1/perfil -H "Authorization: Bearer $TOKEN"

# Actualizar perfil
curl -X PUT http://localhost:3001/api/v1/perfil \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"NuevoNombre","telefono":351999888}'

# Cambiar contraseña
curl -X PUT http://localhost:3001/api/v1/perfil/password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"passwordActual":"admin123","passwordNueva":"nuevaclave"}'
```

### 🔑 Admin (requiere rol ADMIN)

```bash
# Estadísticas
curl http://localhost:3001/api/v1/admin/estadisticas -H "Authorization: Bearer $TOKEN_ADMIN"

# Listar productos (admin)
curl http://localhost:3001/api/v1/admin/productos -H "Authorization: Bearer $TOKEN_ADMIN"

# Crear producto
curl -X POST http://localhost:3001/api/v1/admin/productos \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Nuevo Burger","precioUnitario":12000,"categoria":"hamburguesas","localId":1}'

# Cambiar disponibilidad
curl -X PUT http://localhost:3001/api/v1/admin/productos/1/disponibilidad \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"disponible":false}'

# Pedidos con filtro por estado
curl 'http://localhost:3001/api/v1/admin/pedidos?estado=PENDIENTE' -H "Authorization: Bearer $TOKEN_ADMIN"

# Listar usuarios
curl http://localhost:3001/api/v1/admin/usuarios -H "Authorization: Bearer $TOKEN_ADMIN"

# Registrar repartidor
curl -X POST http://localhost:3001/api/v1/admin/repartidores \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"dni":77889900,"nombre":"Nuevo","apellido":"Repartidor","mail":"nuevo.rep@test.com","contrasenia":"clave123","telefono":351000999}'
```

## Categorías de Productos

| Categoría | Ejemplos | Rango precio |
|-----------|----------|-------------|
| hamburguesas | Simples (150g), Dobles (300g), Triples (450g), Combos | $10.500 – $27.000 |
| acompañamientos | Papas fritas, Aros de cebolla, Nuggets, Bastones muzza | $5.500 – $12.000 |
| bebidas | Gaseosas, Agua, Cerveza, Jugo natural | $2.500 – $5.500 |

## Estados de Pedido

```
PENDIENTE → CONFIRMADO → EN_PREPARACION → EN_CAMINO → ENTREGADO
    ↓            ↓              ↓
 CANCELADO   CANCELADO     CANCELADO
```

## Swagger

Con el servidor corriendo: http://localhost:3001/swagger-ui.html

# ComidApp — Quickstart de Desarrollo Local

Guía paso a paso para levantar el entorno completo desde cero.

---

## Requisitos previos

| Herramienta | Versión mínima | Verificar |
|-------------|---------------|-----------|
| Java JDK | 17+ | `java -version` |
| Gradle | 8+ (o usar wrapper `./gradlew`) | `gradle -v` |
| Node.js | 20 LTS (solo frontend) | `node -v` |
| npm | 10+ (solo frontend) | `npm -v` |
| MySQL | 8.0 | `mysql --version` |
| Docker (opcional) | 24+ | `docker -v` |
| Git | cualquiera | `git --version` |

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/<tu-org>/2026-MTN-TP5--ComidApp-SDD.git
cd 2026-MTN-TP5--ComidApp-SDD
```

---

## 2. Estructura esperada del proyecto

```
.
├── backend/          ← Node.js + Express + Prisma (a crear)
├── frontend/         ← React + Vite + TypeScript
└── specs/            ← documentación SDD
```

---

## 3. Variables de entorno

### Backend — `backend/src/main/resources/application.properties`

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/comidapp?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=cambia-esto-por-un-secreto-seguro-de-al-menos-32-chars
jwt.expiration=604800000

# Mercado Pago
mp.access-token=TEST-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
mp.webhook-secret=tu-webhook-secret-de-mp
mp.success-url=http://localhost:5173/pago/exito
mp.failure-url=http://localhost:5173/pago/fallo
mp.pending-url=http://localhost:5173/pago/pendiente

# Web Push VAPID
vapid.subject=mailto:admin@comidapp.ar
vapid.public-key=
vapid.private-key=

# Servidor
server.port=3001
```

### Frontend — `frontend/.env`

```env
VITE_API_URL=http://localhost:3001/api/v1
VITE_VAPID_PUBLIC_KEY=""  # misma clave que vapid.public-key del backend
```

---

## 4. Base de datos

### Opción A — MySQL local

```bash
# Crear la base de datos
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS comidapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Opción B — Docker Compose (recomendado)

```bash
cd backend
docker compose up -d db
```

El `docker-compose.yml` del backend debe exponer MySQL en el puerto 3306.

---

## 5. Generar claves VAPID

Ejecutar una sola vez y copiar los valores a `application.properties`:

```bash
cd backend
./gradlew generateVapid
```

Esto imprime `vapid.public-key` y `vapid.private-key`.
Copiar ambos valores en `application.properties` **y** el público en `frontend/.env`.

---

## 6. Backend

```bash
cd backend

# Compilar el proyecto
./gradlew build

# Ejecutar migraciones SQL (Flyway, si está configurado)
# o aplicar el script SQL manualmente:
mysql -u root -p comidapp < src/main/resources/db/migration/V1__init.sql

# Iniciar en modo desarrollo
./gradlew bootRun
```

El servidor estará disponible en `http://localhost:3001`.

Verificar:

```bash
curl http://localhost:3001/api/v1/locales
# Espera: [] o array de locales si el seed cargó datos
```

---

## 7. Frontend

```bash
cd frontend
npm install
npm run dev
```

La app estará disponible en `http://localhost:5173`.

---

## 8. Ejecutar tests

### Backend

```bash
cd backend

# Tests unitarios + integración
./gradlew test

# Con reporte de cobertura (JaCoCo)
./gradlew jacocoTestReport
```

### Frontend

```bash
cd frontend
npm test
```

---

## 9. Flujos de verificación end-to-end

### Flujo 1 — Registro y login

```bash
# Registrar usuario
curl -X POST http://localhost:3001/api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","apellido":"López","email":"ana@test.com","password":"password123"}'

# Login
curl -X POST http://localhost:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@test.com","password":"password123"}'
# Guardar el token del response
```

### Flujo 2 — Ver menú y hacer pedido

```bash
TOKEN="<token-del-paso-anterior>"

# Ver locales
curl -H "Authorization: Bearer $TOKEN" http://localhost:3001/api/v1/locales

# Ver menú del local 1
curl -H "Authorization: Bearer $TOKEN" http://localhost:3001/api/v1/menu/1

# Agregar item al carrito
curl -X POST http://localhost:3001/api/v1/carrito/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"itemMenuId":1,"cantidad":2}'

# Confirmar pedido en efectivo
curl -X POST http://localhost:3001/api/v1/pedidos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"metodoPago":"EFECTIVO","direccionEntrega":"Av. Corrientes 1234"}'
```

### Flujo 3 — Admin: desactivar producto

```bash
ADMIN_TOKEN="<token-de-admin>"

curl -X PATCH http://localhost:3001/api/v1/admin/locales/1/productos/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"disponible":false}'
```

---

## 10. Solución de problemas comunes

| Síntoma | Causa probable | Solución |
|---------|---------------|----------|
| `P1001: Can't reach database` | MySQL no está corriendo | `sudo service mysql start` o `docker compose up -d db` |
| `JWT must be provided` | Falta el header Authorization | Incluir `Authorization: Bearer <token>` |
| `VAPID keys not configured` | Variables vacías en application.properties | Ejecutar `./gradlew generateVapid` y actualizar `application.properties` |
| Puerto 3001 ocupado | Otro proceso usa el puerto | Cambiar `server.port=` en `application.properties` |
| `Schema validation: missing table` | Migraciones no aplicadas | Aplicar el SQL inicial: `mysql -u root -p comidapp < V1__init.sql` |

---

## 11. Próximos pasos

Una vez que el entorno esté corriendo, consultar:

- `specs/001-comidapp-mvp/plan.md` — arquitectura y estructura de carpetas
- `specs/001-comidapp-mvp/contracts/openapi.yaml` — contrato completo de la API
- `specs/001-comidapp-mvp/data-model.md` — esquema de base de datos
- `specs/001-comidapp-mvp/spec.md` — historias de usuario y requerimientos

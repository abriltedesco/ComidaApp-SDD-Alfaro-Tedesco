# 🍔 ComidApp — Plataforma de Pedidos y Entrega de Comida Rápida

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite)

ComidApp es una plataforma web full-stack de pedidos y entrega de comida rápida para una empresa de hamburguesas con múltiples locales físicos. El sistema permite a los clientes explorar menús por local, gestionar carritos de compra, confirmar pedidos con diferentes métodos de pago, y hacer seguimiento en tiempo real del estado de sus pedidos con notificaciones web push.

---

## 📋 ¿Qué hace la aplicación?

ComidApp es un sistema completo de delivery que cubre el flujo end-to-end de un pedido de comida:

### 👤 Para Clientes
- **Registro y autenticación** con JWT (JSON Web Tokens)
- **Exploración de menús** por local con filtros por categoría
- **Gestión de carrito** con validaciones de disponibilidad y local único
- **Confirmación de pedidos** con pago por tarjeta (Mercado Pago) o efectivo
- **Seguimiento en tiempo real** del estado del pedido
- **Notificaciones push** cuando el pedido cambia de estado
- **Historial de pedidos** con opción de volver a pedir
- **Valoraciones y reseñas** de productos consumidos

### 👨‍💼 Para Administradores
- **Gestión de locales** (creación, edición, horarios de atención)
- **Administración de menús** (productos, precios, disponibilidad)
- **Panel de métricas** con KPIs de negocio
- **Visualización de pedidos** en tiempo real por local
- **Gestión de repartidores** y asignación de pedidos

### 🛵 Para Repartidores
- **Listado de pedidos asignados** en tiempo real
- **Actualización de estado** de entregas
- **Confirmación de pago** para pedidos en efectivo

### 💼 Funcionalidades del Sistema
- **Catálogo de productos**: Hamburguesas (simples, dobles, triples, combos, veggie), acompañamientos (papas, aros de cebolla, nuggets, bastones de mozzarella), bebidas (gaseosas, agua, cerveza, jugos naturales)
- **Control de horarios**: Los pedidos solo se aceptan en horario de atención del local
- **Validación de disponibilidad**: Verifica stock antes de confirmar pedidos
- **Múltiples métodos de pago**: Tarjeta con pasarela Mercado Pago o efectivo contra entrega
- **Sistema de estados**: Flujo completo desde RECIBIDO → EN_PREPARACION → EN_CAMINO → ENTREGADO
- **Timeout automático**: Cancelación automática si no se avanza desde RECIBIDO en 1 hora
- **Restricciones de negocio**: Un carrito solo puede contener productos de un mismo local

---

## 🎯 Desarrollo con SDD (Specification-Driven Development)

Este proyecto fue desarrollado siguiendo la metodología **SDD (Specification-Driven Development)**, que pone las especificaciones al centro del proceso de desarrollo.

### ¿Qué es SDD?

SDD es una metodología de desarrollo que establece:
1. **La especificación manda**: Todo el código debe trazarse a requisitos funcionales documentados
2. **Diseño antes de código**: Se define primero el modelo de datos, contratos de API y arquitectura
3. **Calidad desde el inicio**: Tests y validaciones planificadas desde la especificación
4. **Documentación viva**: La especificación evoluciona con el proyecto

### Aplicación de SDD en ComidApp

#### 📁 Estructura de Especificaciones

```
specs/001-comidapp-mvp/
├── spec.md              # 13 historias de usuario con criterios de aceptación
├── plan.md              # Plan de implementación con arquitectura y fases
├── data-model.md        # Modelo de datos con tablas, columnas, PKs, FKs e índices
├── research.md          # Decisiones técnicas fundamentadas
├── contracts/
│   └── openapi.yaml     # Contratos REST de todos los endpoints
└── checklists/
    └── requirements.md  # Checklist de calidad (aprobado 16/16)
```

#### 🏗️ Principios Aplicados

1. **Dominio OO Real**: Las entidades encapsulan lógica de negocio (`Pedido.puedeAvanzar()`, `Carrito.calcularTotal()`, `Local.estaAbierto()`)
2. **Separación de Capas**: Arquitectura en 4 capas (Domain, Application, Infrastructure, API)
3. **Código Limpio**: Sin clases anémicas, métodos < 40 líneas, validaciones en servicios
4. **Enums para Tipos Cerrados**: `EstadoPedido`, `MetodoPago`, `EstadoPago`, `RolUsuario`
5. **Tests para Reglas Críticas**: Cobertura obligatoria en carrito, disponibilidad, transiciones de estado
6. **Índices MySQL**: Optimización desde diseño en columnas de alta consulta
7. **Mobile-First**: CSS mobile-first, lazy loading, code splitting con Vite

#### 📊 Trazabilidad Completa

Cada funcionalidad implementada está trazada a:
- **Historias de Usuario** (HU-01 a HU-13 en `spec.md`)
- **Requisitos Funcionales** (FR-001 a FR-033 en `spec.md`)
- **Endpoints documentados** en `contracts/openapi.yaml`
- **Modelo de datos** en `data-model.md`

#### ✅ Checklist de Calidad

El proyecto cumple con 16/16 criterios de calidad SDD:
- ✅ Historias de usuario independientes y testeables
- ✅ Arquitectura en capas con responsabilidades claras
- ✅ Modelo de datos normalizado con índices
- ✅ Contratos API versionados y documentados
- ✅ Tests unitarios y de integración
- ✅ Seguridad con JWT y roles
- ✅ Manejo de errores con excepciones tipadas
- ✅ Performance optimizado (LCP < 2.5s)

---

## 🏗️ Arquitectura

### Backend (Spring Boot 4.0.5 + Java 21)

```
src/main/java/com/comidapp/
├── domain/                    # Capa de Dominio
│   ├── entities/              # Entidades JPA con lógica de negocio
│   │   ├── Pedido.java
│   │   ├── Carrito.java
│   │   ├── Local.java
│   │   ├── ItemMenu.java
│   │   └── Usuario.java
│   ├── enums/                 # Tipos cerrados del dominio
│   │   ├── EstadoPedido.java
│   │   ├── MetodoPago.java
│   │   ├── EstadoPago.java
│   │   └── RolUsuario.java
│   └── exceptions/            # Excepciones de dominio
│
├── application/               # Capa de Aplicación (Casos de Uso)
│   ├── pedidos/
│   │   ├── ConfirmarPedidoUseCase.java
│   │   └── GestionarEstadoPedidoUseCase.java
│   ├── carrito/
│   │   └── GestionarCarritoUseCase.java
│   ├── pagos/
│   │   └── ProcesarPagoUseCase.java
│   └── notificaciones/
│       └── EnviarNotificacionUseCase.java
│
├── infrastructure/            # Capa de Infraestructura
│   ├── persistence/           # Repositorios JPA
│   ├── security/              # JWT, filtros, configuración
│   ├── payment/               # Integración Mercado Pago
│   └── notifications/         # Web Push (VAPID)
│
└── api/                       # Capa de API (Controllers)
    ├── controllers/           # REST Controllers
    ├── dto/                   # Data Transfer Objects
    └── config/                # Configuración Web, CORS, Swagger
```

### Frontend (React 19 + Vite 5)

```
2026-MTN-TP3-Front-Alfaro-Tedesco/
├── src/
│   ├── components/            # Componentes reutilizables
│   ├── pages/                 # Páginas de la aplicación
│   ├── hooks/                 # Custom hooks de React
│   ├── services/              # Servicios de API
│   ├── context/               # Context API para estado global
│   └── utils/                 # Utilidades y helpers
└── public/                    # Assets estáticos
```

### Base de Datos (MySQL 8.0)

El esquema está normalizado con las siguientes tablas principales:
- `usuario` (clientes, repartidores, admin)
- `local` (sucursales con horarios)
- `item_menu` (productos del catálogo)
- `carrito` + `item_carrito` (carritos activos)
- `pedido` + `item_pedido` (pedidos confirmados)
- `pago` (transacciones de pago)
- `notificacion` (notificaciones push)
- `resena` (valoraciones de productos)

**Índices optimizados** en:
- `pedido.cliente_id`, `pedido.estado`, `pedido.local_id`
- `usuario.email` (único)
- `item_menu.local_id`, `item_menu.categoria`
- `carrito.cliente_id` (único)

---

## 🚀 Cómo Correr la Aplicación

### Prerrequisitos

Asegúrate de tener instalado:
- **Java 21** (OpenJDK o Oracle JDK)
- **MySQL 8.0** o superior
- **Node.js 18+** y **npm** (para el frontend)
- **Git** (para clonar el repositorio)

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/Instituto-Politecnico-Modelo/2026-MTN-TP5--ComidApp-SDD.git
cd 2026-MTN-TP5--ComidApp-SDD
```

### Paso 2: Configurar la Base de Datos

#### 2.1 Crear la Base de Datos

```bash
mysql -u root -p
```

Dentro de MySQL:

```sql
CREATE DATABASE comida_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'comidapp_user'@'localhost' IDENTIFIED BY 'alumnoipm';
GRANT ALL PRIVILEGES ON comida_app.* TO 'comidapp_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 2.2 (Opcional) Cargar Datos de Prueba

Si quieres usar el script SQL con datos iniciales:

```bash
mysql -u root -p comida_app < scripts/comidapp_workbench.sql
```

> **Nota**: La aplicación también carga datos de prueba automáticamente al arrancar con el perfil `dev`.

### Paso 3: Configurar el Backend

#### 3.1 Editar Credenciales de Base de Datos (si es necesario)

Edita `backend/src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/comida_app
spring.datasource.username=comidapp_user
spring.datasource.password=alumnoipm
```

#### 3.2 Configurar Variables de Entorno para Mercado Pago

Crea un archivo `.env` en la raíz del proyecto `backend/` (o configura como variables de entorno):

```bash
MERCADOPAGO_ACCESS_TOKEN=tu_access_token_de_mercado_pago
```

> **Nota**: Para desarrollo, puedes usar credenciales de prueba de [Mercado Pago Developers](https://www.mercadopago.com.ar/developers).

### Paso 4: Arrancar el Backend

```bash
cd backend
./gradlew bootRun --no-daemon
```

> **Windows**: Usa `gradlew.bat` en lugar de `./gradlew`

El backend arrancará en `http://localhost:3001`

**Verificar que funciona:**

```bash
curl http://localhost:3001/api/v1/health
```

Deberías ver:
```json
{"status": "UP"}
```

### Paso 5: Arrancar el Frontend (Opcional)

Si tienes el frontend en el proyecto:

```bash
cd 2026-MTN-TP3-Front-Alfaro-Tedesco
npm install
npm run dev
```

El frontend arrancará en `http://localhost:5173`

### Paso 6: Acceder a la Documentación de la API

Con el backend corriendo, abre en tu navegador:

**Swagger UI (Documentación Interactiva)**:  
[http://localhost:3001/swagger-ui.html](http://localhost:3001/swagger-ui.html)

**OpenAPI JSON**:  
[http://localhost:3001/v3/api-docs](http://localhost:3001/v3/api-docs)

---

## 🔐 Credenciales de Prueba

Una vez que el backend arranque, se cargarán automáticamente estos usuarios de prueba:

| Rol | Email | Contraseña | DNI |
|-----|-------|-----------|-----|
| **ADMIN** | admin@comidapp.ar | admin123 | 99999999 |
| **CLIENTE** | juan@test.com | admin123 | 12345678 |
| **CLIENTE** | laura@test.com | admin123 | 22334455 |
| **CLIENTE** | pedro@test.com | admin123 | 33445566 |
| **REPARTIDOR** | maria@test.com | admin123 | 87654321 |
| **REPARTIDOR** | carlos@test.com | admin123 | 11223344 |

---

## 🧪 Ejecutar Tests

### Tests del Backend

```bash
cd backend

# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con reporte de cobertura
./gradlew test jacocoTestReport

# Ver reporte de cobertura
# Se genera en: build/reports/jacoco/test/html/index.html
```

### Tests del Frontend

```bash
cd 2026-MTN-TP3-Front-Alfaro-Tedesco

# Ejecutar tests
npm test

# Ejecutar tests con cobertura
npm run test:coverage
```

---

## 📡 Ejemplos de Uso de la API

### Login

```bash
curl -X POST 'http://localhost:3001/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "mail": "juan@test.com",
    "contrasenia": "admin123"
  }'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 2,
    "nombre": "Juan",
    "apellido": "Pérez",
    "mail": "juan@test.com",
    "rol": "CLIENTE"
  }
}
```

### Listar Locales

```bash
curl 'http://localhost:3001/api/v1/locales' \
  -H 'Authorization: Bearer TU_TOKEN_AQUI'
```

### Ver Menú de un Local

```bash
curl 'http://localhost:3001/api/v1/locales/1/menu' \
  -H 'Authorization: Bearer TU_TOKEN_AQUI'
```

### Agregar Item al Carrito

```bash
curl -X POST 'http://localhost:3001/api/v1/carrito/items' \
  -H 'Authorization: Bearer TU_TOKEN_AQUI' \
  -H 'Content-Type: application/json' \
  -d '{
    "itemMenuId": 1,
    "cantidad": 2
  }'
```

### Confirmar Pedido

```bash
curl -X POST 'http://localhost:3001/api/v1/pedidos/confirmar' \
  -H 'Authorization: Bearer TU_TOKEN_AQUI' \
  -H 'Content-Type: application/json' \
  -d '{
    "metodoPago": "TARJETA",
    "dirEntrega": "Av. Colón 1234",
    "notas": "Sin cebolla en la hamburguesa"
  }'
```

---

## 📚 Documentación Adicional

- **Especificación Completa**: [`specs/001-comidapp-mvp/spec.md`](specs/001-comidapp-mvp/spec.md)
- **Plan de Implementación**: [`specs/001-comidapp-mvp/plan.md`](specs/001-comidapp-mvp/plan.md)
- **Modelo de Datos**: [`specs/001-comidapp-mvp/data-model.md`](specs/001-comidapp-mvp/data-model.md)
- **Contratos API**: [`specs/001-comidapp-mvp/contracts/openapi.yaml`](specs/001-comidapp-mvp/contracts/openapi.yaml)
- **Especificaciones de Datos**: [`documentacion/especificaciones-datos.md`](documentacion/especificaciones-datos.md)
- **Progreso del Backend**: [`documentacion/progreso-backend.md`](documentacion/progreso-backend.md)
- **README del Backend**: [`backend/README.md`](backend/README.md)

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 21** - Lenguaje de programación
- **Spring Boot 4.0.5** - Framework principal
- **Spring Security** - Autenticación y autorización con JWT
- **Spring Data JPA** - ORM y persistencia
- **MySQL 8.0** - Base de datos relacional
- **Flyway** - Migraciones de base de datos
- **Mercado Pago SDK** - Pasarela de pagos
- **Web Push (VAPID)** - Notificaciones push
- **Springdoc OpenAPI** - Documentación de API
- **JUnit 5 + Mockito** - Testing

### Frontend
- **React 19** - Librería de UI
- **TypeScript 5** - Lenguaje tipado
- **Vite 5** - Build tool y dev server
- **React Router** - Enrutamiento
- **Axios** - Cliente HTTP
- **Context API** - Gestión de estado

### DevOps & Tools
- **Gradle** - Build automation
- **Git** - Control de versiones
- **JaCoCo** - Cobertura de código
- **Swagger UI** - Documentación interactiva de API

---

## 📊 Estructura del Proyecto

```
2026-MTN-TP5--ComidApp-SDD/
├── README.md                           # Este archivo
├── backend/                            # Aplicación Spring Boot
│   ├── build.gradle                    # Configuración Gradle
│   ├── src/main/java/com/comidapp/     # Código fuente Java
│   ├── src/main/resources/             # Recursos (properties, SQL migrations)
│   └── src/test/                       # Tests unitarios e integración
├── 2026-MTN-TP3-Front-Alfaro-Tedesco/  # Aplicación React (frontend)
├── documentacion/                      # Documentación del proyecto
│   ├── especificaciones-datos.md
│   └── progreso-backend.md
├── specs/001-comidapp-mvp/             # Especificaciones SDD
│   ├── spec.md                         # Historias de usuario
│   ├── plan.md                         # Plan de implementación
│   ├── data-model.md                   # Modelo de datos
│   ├── research.md                     # Decisiones técnicas
│   ├── contracts/openapi.yaml          # Contratos REST
│   └── checklists/requirements.md      # Checklist de calidad
└── scripts/                            # Scripts SQL
    └── comidapp_workbench.sql
```

---

## 🐛 Troubleshooting

### Error: "Access denied for user"

Verifica las credenciales de MySQL en `application-dev.properties` y asegúrate de que el usuario tenga permisos:

```sql
GRANT ALL PRIVILEGES ON comida_app.* TO 'comidapp_user'@'localhost';
FLUSH PRIVILEGES;
```

### Error: "Port 3001 already in use"

Cambia el puerto en `backend/src/main/resources/application.properties`:

```properties
server.port=3002
```

### Error: "Table doesn't exist"

Asegúrate de que Flyway ejecutó las migraciones. Verifica los logs al arrancar. Si es necesario, elimina la base de datos y vuélvela a crear:

```sql
DROP DATABASE comida_app;
CREATE DATABASE comida_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### El frontend no se conecta al backend

Verifica la URL del backend en la configuración del frontend (archivo de configuración o variable de entorno).

---

## 👥 Autores

- **Alfaro** - Frontend y diseño UX
- **Tedesco** - Backend y arquitectura

**Materia**: Metodologías y Tecnologías de Desarrollo de Software  
**Institución**: Instituto Politécnico Modelo  
**Año**: 2026

---

## 📄 Licencia

Este proyecto es un trabajo académico desarrollado para la materia de Metodologías y Tecnologías de Desarrollo de Software.

---

## 🙏 Agradecimientos

- A los docentes de la materia por la guía en la aplicación de SDD
- A la comunidad de Spring Boot y React por la documentación y recursos
- A Mercado Pago por las APIs de integración de pagos

---

## 📞 Contacto

Para consultas sobre el proyecto:
- **Repository**: [2026-MTN-TP5--ComidApp-SDD](https://github.com/Instituto-Politecnico-Modelo/2026-MTN-TP5--ComidApp-SDD)
- **Documentación**: Ver carpeta `specs/` y `documentacion/`

---

**¡Buen provecho con ComidApp! 🍔🍟🥤**
-- ============================================================================
-- ComidApp MVP — Migración inicial
-- Motor: MySQL 8.0
-- Fecha: 2026-06-19
-- Nota: Esquema compatible con JPA entities en com.comidapp.domain.entities
-- ============================================================================

CREATE DATABASE IF NOT EXISTS comidapp
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE comidapp;

-- ────────────────────────────────────────────────────────────────────────────
-- 1. usuario (SINGLE_TABLE: Cliente, Admin, Repartidor)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuario (
    dni          INT          NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    apellido     VARCHAR(100) NOT NULL,
    mail         VARCHAR(255) NOT NULL,
    contrasenia  VARCHAR(255) NOT NULL,
    telefono     INT          NULL,
    tipo         VARCHAR(31)  NOT NULL COMMENT 'Discriminador: CLIENTE | ADMIN | REPARTIDOR',
    -- Campos de Cliente
    dir_entrega  VARCHAR(300) NULL,
    ciudad       VARCHAR(100) NULL,
    -- Campos de Repartidor
    disponible   TINYINT(1)   NULL,
    PRIMARY KEY (dni),
    UNIQUE INDEX idx_usuario_mail (mail),
    INDEX idx_usuario_tipo (tipo)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 2. local
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `local` (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100) NOT NULL,
    direccion VARCHAR(300) NOT NULL,
    telefono  VARCHAR(20)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 3. horario_local
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS horario_local (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    local_id      BIGINT      NOT NULL,
    dia_semana    VARCHAR(20) NOT NULL COMMENT 'MONDAY, TUESDAY, ..., SUNDAY',
    hora_apertura TIME        NOT NULL,
    hora_cierre   TIME        NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_horario_local_id (local_id),
    CONSTRAINT fk_horario_local
        FOREIGN KEY (local_id) REFERENCES `local` (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 4. producto
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS producto (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(200) NOT NULL,
    descripcion     TEXT         NULL,
    precio_unitario DOUBLE       NOT NULL,
    categoria       VARCHAR(100) NOT NULL,
    imagen_url      VARCHAR(500) NULL,
    disponible      TINYINT(1)   NOT NULL DEFAULT 1,
    local_id        BIGINT       NULL,
    PRIMARY KEY (id),
    INDEX idx_producto_local (local_id),
    INDEX idx_producto_categoria (categoria),
    CONSTRAINT fk_producto_local
        FOREIGN KEY (local_id) REFERENCES `local` (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 5. carrito (un carrito por cliente, un solo local)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS carrito (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    cliente_dni INT    NOT NULL,
    local_id    BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_carrito_cliente (cliente_dni),
    INDEX idx_carrito_local (local_id),
    CONSTRAINT fk_carrito_cliente
        FOREIGN KEY (cliente_dni) REFERENCES usuario (dni)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_carrito_local
        FOREIGN KEY (local_id) REFERENCES `local` (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 6. item_carrito
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS item_carrito (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    carrito_id  BIGINT   NOT NULL,
    producto_id BIGINT   NOT NULL,
    cantidad    INT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_item_carrito_carrito (carrito_id),
    UNIQUE INDEX idx_item_carrito_carrito_producto (carrito_id, producto_id),
    CONSTRAINT fk_item_carrito_carrito
        FOREIGN KEY (carrito_id) REFERENCES carrito (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_carrito_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 7. pedido
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pedido (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    cliente_dni   INT           NOT NULL,
    local_id      BIGINT        NOT NULL,
    repartidor_dni INT          NULL,
    estado        VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    metodo_pago   VARCHAR(20)   NOT NULL,
    estado_pago   VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    precio_total  DOUBLE        NOT NULL,
    dir_entrega   VARCHAR(300)  NULL,
    fecha_hora    DATETIME      NULL,
    PRIMARY KEY (id),
    INDEX idx_pedido_cliente (cliente_dni),
    INDEX idx_pedido_repartidor (repartidor_dni),
    INDEX idx_pedido_estado (estado),
    INDEX idx_pedido_local (local_id),
    INDEX idx_pedido_cliente_estado (cliente_dni, estado),
    INDEX idx_pedido_estado_fecha (estado, fecha_hora),
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_dni) REFERENCES usuario (dni)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pedido_repartidor
        FOREIGN KEY (repartidor_dni) REFERENCES usuario (dni)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_pedido_local
        FOREIGN KEY (local_id) REFERENCES `local` (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 8. item_pedido
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS item_pedido (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id       BIGINT       NOT NULL,
    producto_id     BIGINT       NOT NULL,
    cantidad        INT          NOT NULL DEFAULT 1,
    precio_unitario DOUBLE       NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_item_pedido_pedido (pedido_id),
    CONSTRAINT fk_item_pedido_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_pedido_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 9. pago
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pago (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id        BIGINT       NOT NULL,
    metodo_pago      VARCHAR(20)  NOT NULL,
    estado           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    monto            DOUBLE       NOT NULL,
    mp_preference_id VARCHAR(100) NULL,
    mp_payment_id    VARCHAR(100) NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_pago_pedido (pedido_id),
    INDEX idx_pago_estado (estado),
    CONSTRAINT fk_pago_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 10. suscripcion_push
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS suscripcion_push (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    usuario_dni INT           NOT NULL,
    endpoint    VARCHAR(500)  NOT NULL,
    p256dh      VARCHAR(500)  NOT NULL,
    auth        VARCHAR(500)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_push_usuario (usuario_dni),
    CONSTRAINT fk_push_usuario
        FOREIGN KEY (usuario_dni) REFERENCES usuario (dni)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- Datos iniciales (seed)
-- ============================================================================

-- Admin por defecto
INSERT INTO usuario (dni, nombre, apellido, mail, contrasenia, telefono, tipo)
VALUES (99999999, 'Admin', 'ComidApp', 'admin@comidapp.ar',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
        0, 'ADMIN')
ON DUPLICATE KEY UPDATE nombre = nombre;

-- 5 Locales
INSERT INTO `local` (id, nombre, direccion, telefono) VALUES
    (1, 'ComidApp Centro',     'Av. San Martín 100, Centro',      '0351-4001001'),
    (2, 'ComidApp Norte',      'Av. Rafael Núñez 4500, Cerro',    '0351-4001002'),
    (3, 'ComidApp Sur',        'Av. Sabattini 3200, Barrio SEP',  '0351-4001003'),
    (4, 'ComidApp Oeste',      'Av. Colón 5800, Villa Cabrera',   '0351-4001004'),
    (5, 'ComidApp Nueva Córdoba', 'Bv. Illia 200, Nueva Córdoba', '0351-4001005')
ON DUPLICATE KEY UPDATE nombre = nombre;

-- Horarios (Lunes a Sábado 11:00-23:00, Domingo 12:00-22:00)
INSERT INTO horario_local (local_id, dia_semana, hora_apertura, hora_cierre)
SELECT l.id, d.dia, d.apertura, d.cierre
FROM `local` l
CROSS JOIN (
    SELECT 'MONDAY'    AS dia, '11:00:00' AS apertura, '23:00:00' AS cierre UNION ALL
    SELECT 'TUESDAY',         '11:00:00',              '23:00:00'          UNION ALL
    SELECT 'WEDNESDAY',       '11:00:00',              '23:00:00'          UNION ALL
    SELECT 'THURSDAY',        '11:00:00',              '23:00:00'          UNION ALL
    SELECT 'FRIDAY',          '11:00:00',              '23:00:00'          UNION ALL
    SELECT 'SATURDAY',        '11:00:00',              '23:00:00'          UNION ALL
    SELECT 'SUNDAY',          '12:00:00',              '22:00:00'
) d
WHERE NOT EXISTS (
    SELECT 1 FROM horario_local h WHERE h.local_id = l.id AND h.dia_semana = d.dia
);

-- Productos iniciales (mismos para todos los locales)
INSERT INTO producto (nombre, descripcion, precio_unitario, categoria, imagen_url, disponible, local_id)
SELECT p.nombre, p.descripcion, p.precio, p.categoria, p.imagen, 1, l.id
FROM (
    SELECT 'Hamburguesa Clásica'    AS nombre, 'Pan, carne 150g, lechuga, tomate, queso' AS descripcion, 3500.00 AS precio, 'hamburguesas' AS categoria, NULL AS imagen UNION ALL
    SELECT 'Hamburguesa Doble',      'Pan, doble carne 300g, queso cheddar, bacon',        5200.00,        'hamburguesas',   NULL           UNION ALL
    SELECT 'Hamburguesa Veggie',     'Pan, medallón de lentejas, rúcula, tomate',          3800.00,        'hamburguesas',   NULL           UNION ALL
    SELECT 'Papas Fritas Grandes',   'Porción 400g con salsa a elección',                  2000.00,        'acompañamientos', NULL          UNION ALL
    SELECT 'Aros de Cebolla',        'Porción 300g con dip BBQ',                           2200.00,        'acompañamientos', NULL          UNION ALL
    SELECT 'Coca-Cola 500ml',        'Gaseosa línea Coca-Cola',                            1500.00,        'bebidas',        NULL           UNION ALL
    SELECT 'Agua Mineral 500ml',     'Sin gas',                                            1000.00,        'bebidas',        NULL           UNION ALL
    SELECT 'Cerveza Artesanal 473ml','IPA local',                                          2500.00,        'bebidas',        NULL
) p
CROSS JOIN `local` l
WHERE NOT EXISTS (
    SELECT 1 FROM producto pr WHERE pr.nombre = p.nombre AND pr.local_id = l.id
);

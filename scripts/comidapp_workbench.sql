-- ============================================================================
-- ComidApp — Script completo para MySQL Workbench
-- Fuente: DevDataLoader.java (backend/src/main/java/.../config/DevDataLoader.java)
-- Base de datos: comida_app  (igual que application-dev.properties)
-- Motor: MySQL 8.0
-- Contrasenia de todos los usuarios: admin123
-- ============================================================================

DROP DATABASE IF EXISTS comida_app;

CREATE DATABASE comida_app
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE comida_app;

-- ────────────────────────────────────────────────────────────────────────────
-- 1. usuario  (SINGLE_TABLE: Admin / Cliente / Repartidor)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE usuario (
    dni          INT          NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    apellido     VARCHAR(100) NOT NULL,
    mail         VARCHAR(255) NOT NULL,
    contrasenia  VARCHAR(255) NOT NULL,
    telefono     INT          NULL,
    tipo         VARCHAR(31)  NOT NULL  COMMENT 'ADMIN | CLIENTE | REPARTIDOR',
    dir_entrega  VARCHAR(300) NULL,
    ciudad       VARCHAR(100) NULL,
    disponible   TINYINT(1)   NULL,
    PRIMARY KEY (dni),
    UNIQUE INDEX idx_usuario_mail (mail),
    INDEX idx_usuario_tipo (tipo)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 2. local
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE `local` (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100) NOT NULL,
    direccion VARCHAR(300) NOT NULL,
    telefono  VARCHAR(20)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 3. horario_local  (DiaSemana: LUNES..DOMINGO)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE horario_local (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    local_id      BIGINT      NOT NULL,
    dia_semana    VARCHAR(20) NOT NULL  COMMENT 'LUNES | MARTES | MIERCOLES | JUEVES | VIERNES | SABADO | DOMINGO',
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
CREATE TABLE producto (
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
-- 5. carrito
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE carrito (
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
CREATE TABLE item_carrito (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    carrito_id  BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad    INT    NOT NULL DEFAULT 1,
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
CREATE TABLE pedido (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    cliente_dni    INT          NOT NULL,
    local_id       BIGINT       NOT NULL,
    repartidor_dni INT          NULL,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                     COMMENT 'PENDIENTE|CONFIRMADO|EN_PREPARACION|EN_CAMINO|ENTREGADO|CANCELADO',
    metodo_pago    VARCHAR(20)  NOT NULL  COMMENT 'EFECTIVO | TARJETA',
    estado_pago    VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                     COMMENT 'PENDIENTE | APROBADO | RECHAZADO',
    precio_total   DOUBLE       NOT NULL,
    dir_entrega    VARCHAR(300) NULL,
    fecha_hora     DATETIME     NULL,
    PRIMARY KEY (id),
    INDEX idx_pedido_cliente (cliente_dni),
    INDEX idx_pedido_repartidor (repartidor_dni),
    INDEX idx_pedido_estado (estado),
    INDEX idx_pedido_local (local_id),
    INDEX idx_pedido_cliente_estado (cliente_dni, estado),
    INDEX idx_pedido_estado_fecha (estado, fecha_hora),
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_dni)    REFERENCES usuario (dni) ON DELETE RESTRICT  ON UPDATE CASCADE,
    CONSTRAINT fk_pedido_repartidor
        FOREIGN KEY (repartidor_dni) REFERENCES usuario (dni) ON DELETE SET NULL  ON UPDATE CASCADE,
    CONSTRAINT fk_pedido_local
        FOREIGN KEY (local_id)       REFERENCES `local` (id)  ON DELETE RESTRICT  ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 8. item_pedido
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE item_pedido (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL,
    cantidad        INT    NOT NULL DEFAULT 1,
    precio_unitario DOUBLE NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_item_pedido_pedido (pedido_id),
    CONSTRAINT fk_item_pedido_pedido
        FOREIGN KEY (pedido_id)   REFERENCES pedido  (id) ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_item_pedido_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 9. pago
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE pago (
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
        FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 10. suscripcion_push
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE suscripcion_push (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_dni INT          NOT NULL,
    endpoint    VARCHAR(500) NOT NULL,
    p256dh      VARCHAR(500) NOT NULL,
    auth        VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_push_usuario (usuario_dni),
    CONSTRAINT fk_push_usuario
        FOREIGN KEY (usuario_dni) REFERENCES usuario (dni)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 11. resena  (creada por Hibernate via @Entity, no incluida en V1__init.sql)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE resena (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    producto_id BIGINT      NOT NULL,
    cliente_dni INT         NOT NULL,
    puntuacion  INT         NOT NULL,
    comentario  VARCHAR(40) NULL,
    fecha_hora  DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_resena_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_resena_cliente  FOREIGN KEY (cliente_dni) REFERENCES usuario  (dni)
) ENGINE=InnoDB;


-- ============================================================================
-- DATOS: Usuarios (6)
-- Contrasenia: admin123
-- Hash: $2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm
-- ============================================================================

INSERT INTO usuario (dni, nombre, apellido, mail, contrasenia, telefono, tipo) VALUES
(99999999, 'Admin', 'ComidApp', 'admin@comidapp.ar',
 '$2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm', 0, 'ADMIN');

INSERT INTO usuario (dni, nombre, apellido, mail, contrasenia, telefono, tipo, dir_entrega, ciudad) VALUES
(12345678, 'Juan',  'Perez',    'juan@test.com',   '$2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm', 351555123, 'CLIENTE', 'Av. Colon 123',         'Cordoba'),
(22334455, 'Laura', 'Martinez', 'laura@test.com',  '$2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm', 351555666, 'CLIENTE', 'Bv. San Juan 800',      'Cordoba'),
(33445566, 'Pedro', 'Gonzalez', 'pedro@test.com',  '$2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm', 351555777, 'CLIENTE', 'Calle Dean Funes 200',  'Cordoba');

INSERT INTO usuario (dni, nombre, apellido, mail, contrasenia, telefono, tipo, disponible) VALUES
(87654321, 'Maria',  'Lopez',  'maria@test.com',  '$2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm', 351555987, 'REPARTIDOR', 1),
(11223344, 'Carlos', 'Garcia', 'carlos@test.com', '$2a$10$5UXm0I8RILP1qcuEaFB0t.qX8/L5CobAR4cW1lzoAA/IllarPOTdm', 351555432, 'REPARTIDOR', 1);


-- ============================================================================
-- DATOS: Locales (5)
-- ============================================================================
INSERT INTO `local` (id, nombre, direccion, telefono) VALUES
(1, 'Alfesco Burgers Centro',  'Av. San Martin 100, Centro',          '0351-4001001'),
(2, 'Alfesco Burgers Norte',   'Av. Rafael Nunez 4500, Cerro',        '0351-4001002'),
(3, 'Alfesco Burgers Sur',     'Av. Sabattini 3200, Barrio SEP',      '0351-4001003'),
(4, 'Alfesco Burgers Florida', 'Florida 800, CABA',                   '011-4001004'),
(5, 'Alfesco Burgers Palermo', 'Honduras 5100, Palermo, CABA',        '011-4001005');


-- ============================================================================
-- DATOS: Horarios
-- Centro     : Lun-Sab 08:00-23:00, Dom 12:00-22:00
-- Norte/Sur  : Lun-Sab 11:00-23:00, Dom 12:00-22:00
-- Flor/Paler : Lun-Sab 11:00-02:00, Dom 12:00-22:00
-- ============================================================================

-- Local 1 - Centro
INSERT INTO horario_local (local_id, dia_semana, hora_apertura, hora_cierre) VALUES
(1,'LUNES','08:00:00','23:00:00'),(1,'MARTES','08:00:00','23:00:00'),
(1,'MIERCOLES','08:00:00','23:00:00'),(1,'JUEVES','08:00:00','23:00:00'),
(1,'VIERNES','08:00:00','23:00:00'),(1,'SABADO','08:00:00','23:00:00'),
(1,'DOMINGO','12:00:00','22:00:00');

-- Local 2 - Norte
INSERT INTO horario_local (local_id, dia_semana, hora_apertura, hora_cierre) VALUES
(2,'LUNES','11:00:00','23:00:00'),(2,'MARTES','11:00:00','23:00:00'),
(2,'MIERCOLES','11:00:00','23:00:00'),(2,'JUEVES','11:00:00','23:00:00'),
(2,'VIERNES','11:00:00','23:00:00'),(2,'SABADO','11:00:00','23:00:00'),
(2,'DOMINGO','12:00:00','22:00:00');

-- Local 3 - Sur
INSERT INTO horario_local (local_id, dia_semana, hora_apertura, hora_cierre) VALUES
(3,'LUNES','11:00:00','23:00:00'),(3,'MARTES','11:00:00','23:00:00'),
(3,'MIERCOLES','11:00:00','23:00:00'),(3,'JUEVES','11:00:00','23:00:00'),
(3,'VIERNES','11:00:00','23:00:00'),(3,'SABADO','11:00:00','23:00:00'),
(3,'DOMINGO','12:00:00','22:00:00');

-- Local 4 - Florida (nocturno)
INSERT INTO horario_local (local_id, dia_semana, hora_apertura, hora_cierre) VALUES
(4,'LUNES','11:00:00','02:00:00'),(4,'MARTES','11:00:00','02:00:00'),
(4,'MIERCOLES','11:00:00','02:00:00'),(4,'JUEVES','11:00:00','02:00:00'),
(4,'VIERNES','11:00:00','02:00:00'),(4,'SABADO','11:00:00','02:00:00'),
(4,'DOMINGO','12:00:00','22:00:00');

-- Local 5 - Palermo (nocturno)
INSERT INTO horario_local (local_id, dia_semana, hora_apertura, hora_cierre) VALUES
(5,'LUNES','11:00:00','02:00:00'),(5,'MARTES','11:00:00','02:00:00'),
(5,'MIERCOLES','11:00:00','02:00:00'),(5,'JUEVES','11:00:00','02:00:00'),
(5,'VIERNES','11:00:00','02:00:00'),(5,'SABADO','11:00:00','02:00:00'),
(5,'DOMINGO','12:00:00','22:00:00');


-- ============================================================================
-- DATOS: Productos
-- 32 productos base × 5 locales = 160 (IDs 1-160, local 1 = IDs 1-32)
-- 2 combos × 5 locales = 10 (IDs 161-170)
-- Orden de insercion: todos los productos del local 1, luego local 2, etc.
-- ============================================================================

-- ── 32 productos base para TODOS los locales (CROSS JOIN) ───────────────────
INSERT INTO producto (nombre, descripcion, precio_unitario, categoria, disponible, local_id)
SELECT p.nombre, p.descripcion, p.precio, p.categoria, 1, l.id
FROM (
  -- Hamburguesas simples (150g)
  SELECT  1 AS ord, 'Hamburguesa Clasica'      AS nombre, 'Pan brioche, carne 150g, lechuga, tomate, queso tybo, mayonesa casera'                    AS descripcion, 10500.00 AS precio, 'hamburguesas' AS categoria UNION ALL
  SELECT  2, 'Hamburguesa Criolla',     'Pan de campo, carne 150g, huevo frito, chimichurri, lechuga, tomate',                                         11000.00, 'hamburguesas' UNION ALL
  SELECT  3, 'Hamburguesa BBQ',         'Pan con semillas, carne 150g, bacon, cheddar, salsa BBQ ahumada, cebolla caramelizada',                       12000.00, 'hamburguesas' UNION ALL
  SELECT  4, 'Hamburguesa Veggie',      'Pan integral, medallon de lentejas y quinoa, rucula, tomate seco, palta',                                     11500.00, 'hamburguesas' UNION ALL
  SELECT  5, 'Hamburguesa Picante',     'Pan brioche, carne 150g, jalapeños, pepper jack, salsa picante, cebolla morada',                              11500.00, 'hamburguesas' UNION ALL
  -- Hamburguesas dobles (300g)
  SELECT  6, 'Doble Cheddar',           'Pan brioche, doble carne 300g, doble cheddar, bacon crocante, salsa especial',                                14500.00, 'hamburguesas' UNION ALL
  SELECT  7, 'Doble Criolla',           'Pan de campo, doble carne 300g, huevo, provolone, chimichurri',                                               15000.00, 'hamburguesas' UNION ALL
  SELECT  8, 'Doble BBQ Bacon',         'Pan con semillas, doble carne 300g, triple bacon, cheddar, BBQ ahumada',                                      15500.00, 'hamburguesas' UNION ALL
  SELECT  9, 'Doble Completa',          'Pan brioche, doble carne 300g, jamon, queso, huevo, lechuga, tomate, mayonesa',                               15000.00, 'hamburguesas' UNION ALL
  SELECT 10, 'Doble Smash',             'Pan brioche, 2 smash patties crocantes, american cheese, pepinillos, mostaza',                                14000.00, 'hamburguesas' UNION ALL
  -- Hamburguesas triples (450g)
  SELECT 11, 'Triple Inferno',          'Pan brioche, triple carne 450g, triple cheddar, bacon, jalapeños, salsa inferno',                             19500.00, 'hamburguesas' UNION ALL
  SELECT 12, 'Triple ComidApp',         'Pan brioche, triple carne 450g, cheddar, provolone, bacon, huevo, salsa de la casa',                          20000.00, 'hamburguesas' UNION ALL
  SELECT 13, 'Triple Bacon Monster',    'Pan con semillas, triple carne 450g, quintuple bacon, cheddar, BBQ',                                          21000.00, 'hamburguesas' UNION ALL
  SELECT 14, 'Triple Clasica XL',       'Pan brioche XL, triple carne 450g, lechuga, tomate, cebolla, queso, mayo',                                    18500.00, 'hamburguesas' UNION ALL
  -- Acompañamientos
  SELECT 15, 'Papas Fritas Medianas',   'Porcion 300g con salsa a eleccion (mayo, ketchup, BBQ)',                                                       5500.00, 'acompañamientos' UNION ALL
  SELECT 16, 'Papas Fritas Grandes',    'Porcion 500g con salsa a eleccion',                                                                            7000.00, 'acompañamientos' UNION ALL
  SELECT 17, 'Papas con Cheddar y Bacon','Porcion 400g con cheddar fundido y bacon crocante',                                                           8500.00, 'acompañamientos' UNION ALL
  SELECT 18, 'Aros de Cebolla',         'Porcion 250g rebozados con dip barbacoa',                                                                      6500.00, 'acompañamientos' UNION ALL
  SELECT 19, 'Nuggets x6',              '6 nuggets de pollo con dip a eleccion',                                                                        7000.00, 'acompañamientos' UNION ALL
  SELECT 20, 'Nuggets x12',             '12 nuggets de pollo con 2 dips a eleccion',                                                                   12000.00, 'acompañamientos' UNION ALL
  SELECT 21, 'Bastones de Mozzarella x6','6 bastones de muzza con salsa marinara',                                                                      7500.00, 'acompañamientos' UNION ALL
  SELECT 22, 'Ensalada Cesar',          'Lechuga, croutones, parmesano, aderezo cesar',                                                                 6000.00, 'acompañamientos' UNION ALL
  -- Bebidas
  SELECT 23, 'Coca-Cola 500ml',         'Gaseosa Coca-Cola linea regular 500ml',                                                                        3500.00, 'bebidas' UNION ALL
  SELECT 24, 'Coca-Cola Zero 500ml',    'Gaseosa Coca-Cola Zero 500ml',                                                                                 3500.00, 'bebidas' UNION ALL
  SELECT 25, 'Sprite 500ml',            'Gaseosa Sprite 500ml',                                                                                         3500.00, 'bebidas' UNION ALL
  SELECT 26, 'Fanta 500ml',             'Gaseosa Fanta naranja 500ml',                                                                                  3500.00, 'bebidas' UNION ALL
  SELECT 27, 'Agua Mineral 500ml',      'Agua mineral sin gas Villavicencio 500ml',                                                                     2500.00, 'bebidas' UNION ALL
  SELECT 28, 'Agua Saborizada 500ml',   'Agua saborizada Levite pomelo rosado 500ml',                                                                   3000.00, 'bebidas' UNION ALL
  SELECT 29, 'Cerveza Quilmes 473ml',   'Lata Quilmes Cristal 473ml',                                                                                   4500.00, 'bebidas' UNION ALL
  SELECT 30, 'Cerveza Andes 473ml',     'Lata Andes Origen rubia 473ml',                                                                                4500.00, 'bebidas' UNION ALL
  SELECT 31, 'Cerveza Patagonia 473ml', 'Lata Patagonia Amber Lager 473ml',                                                                             5500.00, 'bebidas' UNION ALL
  SELECT 32, 'Jugo Natural 500ml',      'Jugo de naranja exprimido del dia',                                                                            4000.00, 'bebidas'
) p
JOIN `local` l ON l.id >= 1
ORDER BY l.id, p.ord;

-- ── Combos exclusivos por local (IDs 161-170) ────────────────────────────────
INSERT INTO producto (nombre, descripcion, precio_unitario, categoria, disponible, local_id) VALUES
('Combo Centro Clasico',    'Hamburguesa Clasica + Papas medianas + Coca-Cola 500ml',                        16000.00, 'combos', 1, 1),
('Combo Centro Doble',      'Doble Cheddar + Papas grandes + Cerveza Quilmes 473ml',                         22000.00, 'combos', 1, 1),
('Combo Norte BBQ',         'Hamburguesa BBQ + Aros de cebolla + Sprite 500ml',                              18500.00, 'combos', 1, 2),
('Combo Norte Triple',      'Triple ComidApp + Papas con cheddar + Cerveza Patagonia 473ml',                 28000.00, 'combos', 1, 2),
('Combo Sur Criolla',       'Hamburguesa Criolla + Nuggets x6 + Jugo Natural 500ml',                         19000.00, 'combos', 1, 3),
('Combo Sur Smash',         'Doble Smash + Papas medianas + Coca-Cola Zero 500ml',                           20000.00, 'combos', 1, 3),
('Combo Florida Premium',   'Triple Bacon Monster + Papas con cheddar + Cerveza Patagonia 473ml',            30000.00, 'combos', 1, 4),
('Combo Florida Nocturno',  'Doble BBQ Bacon + Bastones de Mozzarella x6 + Cerveza Andes 473ml',             24000.00, 'combos', 1, 4),
('Combo Palermo Veggie',    'Hamburguesa Veggie + Ensalada Cesar + Agua Saborizada 500ml',                   20000.00, 'combos', 1, 5),
('Combo Palermo Inferno',   'Triple Inferno + Nuggets x12 + Cerveza Quilmes 473ml',                          32000.00, 'combos', 1, 5);


-- ============================================================================
-- DATOS: Pedidos (4)
-- Fechas relativas al 2026-06-27 (fecha de generacion del script)
--   p1: ayer 20:30   → ENTREGADO
--   p2: hoy -20min   → EN_PREPARACION
--   p3: hoy -5min    → PENDIENTE (sin repartidor)
--   p4: anteayer 13h → CANCELADO (sin repartidor)
-- Precios calculados con productos del local 1 (IDs 1-32):
--   p1 = 2xHamb.Clasica(10500) + 1xPapas Med(5500) + 2xCoca-Cola(3500) = 33500
--   p2 = 1xDoble Cheddar(14500) + 1xPapas Cheddar y Bacon(8500) + 1xCerveza Quilmes(4500) = 27500
--   p3 = 1xTriple Inferno(19500) + 1xNuggets x6(7000) + 1xSprite(3500) = 30000
--   p4 = 1xHamb.Clasica(10500) = 10500
-- ============================================================================
INSERT INTO pedido (id, cliente_dni, local_id, repartidor_dni, estado, metodo_pago, estado_pago, precio_total, dir_entrega, fecha_hora) VALUES
(1, 12345678, 1, 87654321, 'ENTREGADO',     'EFECTIVO', 'APROBADO',  33500.00, 'Av. Colon 123', '2026-06-26 20:30:00'),
(2, 12345678, 1, 87654321, 'EN_PREPARACION','TARJETA',  'APROBADO',  27500.00, 'Av. Colon 123', '2026-06-27 12:00:00'),
(3, 12345678, 1, NULL,     'PENDIENTE',     'EFECTIVO', 'PENDIENTE', 30000.00, 'Av. Colon 123', '2026-06-27 12:15:00'),
(4, 12345678, 1, NULL,     'CANCELADO',     'EFECTIVO', 'PENDIENTE', 10500.00, 'Av. Colon 123', '2026-06-25 13:00:00');


-- ============================================================================
-- DATOS: Items de pedido
-- Productos del local 1: indice 0-based del DevDataLoader → ID = indice + 1
--   prod[0]  = ID 1  = Hamburguesa Clasica       (10500)
--   prod[5]  = ID 6  = Doble Cheddar             (14500)
--   prod[10] = ID 11 = Triple Inferno            (19500)
--   prod[14] = ID 15 = Papas Fritas Medianas      (5500)
--   prod[16] = ID 17 = Papas con Cheddar y Bacon  (8500)
--   prod[18] = ID 19 = Nuggets x6                 (7000)
--   prod[22] = ID 23 = Coca-Cola 500ml            (3500)
--   prod[24] = ID 25 = Sprite 500ml               (3500)
--   prod[28] = ID 29 = Cerveza Quilmes 473ml      (4500)
-- ============================================================================
INSERT INTO item_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES
-- Pedido 1: 2x Hamburguesa Clasica + 1x Papas Medianas + 2x Coca-Cola
(1,  1, 2, 10500.00),
(1, 15, 1,  5500.00),
(1, 23, 2,  3500.00),
-- Pedido 2: 1x Doble Cheddar + 1x Papas con Cheddar y Bacon + 1x Cerveza Quilmes
(2,  6, 1, 14500.00),
(2, 17, 1,  8500.00),
(2, 29, 1,  4500.00),
-- Pedido 3: 1x Triple Inferno + 1x Nuggets x6 + 1x Sprite
(3, 11, 1, 19500.00),
(3, 19, 1,  7000.00),
(3, 25, 1,  3500.00),
-- Pedido 4: 1x Hamburguesa Clasica
(4,  1, 1, 10500.00);


-- ============================================================================
-- DATOS: Pagos (solo p1 y p2; p3=PENDIENTE y p4=CANCELADO no tienen pago)
-- ============================================================================
INSERT INTO pago (pedido_id, metodo_pago, estado, monto, mp_payment_id) VALUES
(1, 'EFECTIVO', 'APROBADO', 33500.00, NULL),
(2, 'TARJETA',  'APROBADO', 27500.00, 'MP-DEV-001');


-- ============================================================================
-- DATOS: Carrito de juan@test.com (local 1)
--   prod[7]  = ID 8  = Doble BBQ Bacon      (15500) x1
--   prod[15] = ID 16 = Papas Fritas Grandes  (7000)  x1
--   prod[22] = ID 23 = Coca-Cola 500ml       (3500)  x2
-- ============================================================================
INSERT INTO carrito (id, cliente_dni, local_id) VALUES (1, 12345678, 1);

INSERT INTO item_carrito (carrito_id, producto_id, cantidad) VALUES
(1,  8, 1),
(1, 16, 1),
(1, 23, 2);


-- ============================================================================
-- DATOS: Resenas (96 = 32 productos x 3 clientes)
-- Generadas por DevDataLoader.cargarResenas() con:
--   ratings[i] = {puntuacion_c1, puntuacion_c2, puntuacion_c3}
--   comentario = comentarios[(i+j) % 10]
--   comentarios = {"Muy rico!","Excelente","Buenisimo","Recomendado","Me encanto",
--                  "Siempre pido este","Nunca falla","El mejor","Riquisimo","Muy bueno"}
-- fecha_hora fija: 2026-06-27 00:00:00
-- ============================================================================
INSERT INTO resena (producto_id, cliente_dni, puntuacion, comentario, fecha_hora) VALUES
-- prod 1 (Hamburguesa Clasica)
(1,12345678,5,'Muy rico!','2026-06-27 00:00:00'),
(1,22334455,4,'Excelente','2026-06-27 00:00:00'),
(1,33445566,5,'Buenisimo','2026-06-27 00:00:00'),
-- prod 2 (Hamburguesa Criolla)
(2,12345678,4,'Excelente','2026-06-27 00:00:00'),
(2,22334455,5,'Buenisimo','2026-06-27 00:00:00'),
(2,33445566,4,'Recomendado','2026-06-27 00:00:00'),
-- prod 3 (Hamburguesa BBQ)
(3,12345678,5,'Buenisimo','2026-06-27 00:00:00'),
(3,22334455,5,'Recomendado','2026-06-27 00:00:00'),
(3,33445566,5,'Me encanto','2026-06-27 00:00:00'),
-- prod 4 (Hamburguesa Veggie)
(4,12345678,4,'Recomendado','2026-06-27 00:00:00'),
(4,22334455,4,'Me encanto','2026-06-27 00:00:00'),
(4,33445566,3,'Siempre pido este','2026-06-27 00:00:00'),
-- prod 5 (Hamburguesa Picante)
(5,12345678,5,'Me encanto','2026-06-27 00:00:00'),
(5,22334455,4,'Siempre pido este','2026-06-27 00:00:00'),
(5,33445566,4,'Nunca falla','2026-06-27 00:00:00'),
-- prod 6 (Doble Cheddar)
(6,12345678,4,'Siempre pido este','2026-06-27 00:00:00'),
(6,22334455,5,'Nunca falla','2026-06-27 00:00:00'),
(6,33445566,5,'El mejor','2026-06-27 00:00:00'),
-- prod 7 (Doble Criolla)
(7,12345678,5,'Nunca falla','2026-06-27 00:00:00'),
(7,22334455,4,'El mejor','2026-06-27 00:00:00'),
(7,33445566,4,'Riquisimo','2026-06-27 00:00:00'),
-- prod 8 (Doble BBQ Bacon)
(8,12345678,4,'El mejor','2026-06-27 00:00:00'),
(8,22334455,5,'Riquisimo','2026-06-27 00:00:00'),
(8,33445566,5,'Muy bueno','2026-06-27 00:00:00'),
-- prod 9 (Doble Completa)
(9,12345678,5,'Riquisimo','2026-06-27 00:00:00'),
(9,22334455,5,'Muy bueno','2026-06-27 00:00:00'),
(9,33445566,4,'Muy rico!','2026-06-27 00:00:00'),
-- prod 10 (Doble Smash)
(10,12345678,4,'Muy bueno','2026-06-27 00:00:00'),
(10,22334455,4,'Muy rico!','2026-06-27 00:00:00'),
(10,33445566,5,'Excelente','2026-06-27 00:00:00'),
-- prod 11 (Triple Inferno)
(11,12345678,5,'Muy rico!','2026-06-27 00:00:00'),
(11,22334455,5,'Excelente','2026-06-27 00:00:00'),
(11,33445566,5,'Buenisimo','2026-06-27 00:00:00'),
-- prod 12 (Triple ComidApp)
(12,12345678,4,'Excelente','2026-06-27 00:00:00'),
(12,22334455,4,'Buenisimo','2026-06-27 00:00:00'),
(12,33445566,4,'Recomendado','2026-06-27 00:00:00'),
-- prod 13 (Triple Bacon Monster)
(13,12345678,5,'Buenisimo','2026-06-27 00:00:00'),
(13,22334455,5,'Recomendado','2026-06-27 00:00:00'),
(13,33445566,5,'Me encanto','2026-06-27 00:00:00'),
-- prod 14 (Triple Clasica XL)
(14,12345678,4,'Recomendado','2026-06-27 00:00:00'),
(14,22334455,5,'Me encanto','2026-06-27 00:00:00'),
(14,33445566,4,'Siempre pido este','2026-06-27 00:00:00'),
-- prod 15 (Papas Fritas Medianas)
(15,12345678,5,'Me encanto','2026-06-27 00:00:00'),
(15,22334455,4,'Siempre pido este','2026-06-27 00:00:00'),
(15,33445566,5,'Nunca falla','2026-06-27 00:00:00'),
-- prod 16 (Papas Fritas Grandes)
(16,12345678,4,'Siempre pido este','2026-06-27 00:00:00'),
(16,22334455,4,'Nunca falla','2026-06-27 00:00:00'),
(16,33445566,4,'El mejor','2026-06-27 00:00:00'),
-- prod 17 (Papas con Cheddar y Bacon)
(17,12345678,5,'Nunca falla','2026-06-27 00:00:00'),
(17,22334455,5,'El mejor','2026-06-27 00:00:00'),
(17,33445566,4,'Riquisimo','2026-06-27 00:00:00'),
-- prod 18 (Aros de Cebolla)
(18,12345678,4,'El mejor','2026-06-27 00:00:00'),
(18,22334455,4,'Riquisimo','2026-06-27 00:00:00'),
(18,33445566,5,'Muy bueno','2026-06-27 00:00:00'),
-- prod 19 (Nuggets x6)
(19,12345678,5,'Riquisimo','2026-06-27 00:00:00'),
(19,22334455,5,'Muy bueno','2026-06-27 00:00:00'),
(19,33445566,5,'Muy rico!','2026-06-27 00:00:00'),
-- prod 20 (Nuggets x12)
(20,12345678,4,'Muy bueno','2026-06-27 00:00:00'),
(20,22334455,5,'Muy rico!','2026-06-27 00:00:00'),
(20,33445566,4,'Excelente','2026-06-27 00:00:00'),
-- prod 21 (Bastones de Mozzarella x6)
(21,12345678,5,'Muy rico!','2026-06-27 00:00:00'),
(21,22334455,4,'Excelente','2026-06-27 00:00:00'),
(21,33445566,5,'Buenisimo','2026-06-27 00:00:00'),
-- prod 22 (Ensalada Cesar)
(22,12345678,4,'Excelente','2026-06-27 00:00:00'),
(22,22334455,5,'Buenisimo','2026-06-27 00:00:00'),
(22,33445566,4,'Recomendado','2026-06-27 00:00:00'),
-- prod 23 (Coca-Cola 500ml)
(23,12345678,5,'Buenisimo','2026-06-27 00:00:00'),
(23,22334455,4,'Recomendado','2026-06-27 00:00:00'),
(23,33445566,4,'Me encanto','2026-06-27 00:00:00'),
-- prod 24 (Coca-Cola Zero 500ml)
(24,12345678,4,'Recomendado','2026-06-27 00:00:00'),
(24,22334455,5,'Me encanto','2026-06-27 00:00:00'),
(24,33445566,5,'Siempre pido este','2026-06-27 00:00:00'),
-- prod 25 (Sprite 500ml)
(25,12345678,5,'Me encanto','2026-06-27 00:00:00'),
(25,22334455,5,'Siempre pido este','2026-06-27 00:00:00'),
(25,33445566,5,'Nunca falla','2026-06-27 00:00:00'),
-- prod 26 (Fanta 500ml)
(26,12345678,4,'Siempre pido este','2026-06-27 00:00:00'),
(26,22334455,4,'Nunca falla','2026-06-27 00:00:00'),
(26,33445566,4,'El mejor','2026-06-27 00:00:00'),
-- prod 27 (Agua Mineral 500ml)
(27,12345678,5,'Nunca falla','2026-06-27 00:00:00'),
(27,22334455,5,'El mejor','2026-06-27 00:00:00'),
(27,33445566,5,'Riquisimo','2026-06-27 00:00:00'),
-- prod 28 (Agua Saborizada 500ml)
(28,12345678,4,'El mejor','2026-06-27 00:00:00'),
(28,22334455,4,'Riquisimo','2026-06-27 00:00:00'),
(28,33445566,5,'Muy bueno','2026-06-27 00:00:00'),
-- prod 29 (Cerveza Quilmes 473ml)
(29,12345678,5,'Riquisimo','2026-06-27 00:00:00'),
(29,22334455,5,'Muy bueno','2026-06-27 00:00:00'),
(29,33445566,4,'Muy rico!','2026-06-27 00:00:00'),
-- prod 30 (Cerveza Andes 473ml)
(30,12345678,4,'Muy bueno','2026-06-27 00:00:00'),
(30,22334455,5,'Muy rico!','2026-06-27 00:00:00'),
(30,33445566,4,'Excelente','2026-06-27 00:00:00'),
-- prod 31 (Cerveza Patagonia 473ml)
(31,12345678,5,'Muy rico!','2026-06-27 00:00:00'),
(31,22334455,4,'Excelente','2026-06-27 00:00:00'),
(31,33445566,5,'Buenisimo','2026-06-27 00:00:00'),
-- prod 32 (Jugo Natural 500ml)
(32,12345678,4,'Excelente','2026-06-27 00:00:00'),
(32,22334455,5,'Buenisimo','2026-06-27 00:00:00'),
(32,33445566,5,'Recomendado','2026-06-27 00:00:00');


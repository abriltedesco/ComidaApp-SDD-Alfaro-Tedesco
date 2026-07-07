# Especificaciones de Datos — ComidApp

## Reglas Obligatorias del Catálogo de Productos

Todos los productos del sistema **DEBEN** ser exclusivamente de las siguientes categorías:

### 1. Hamburguesas (`categoria: "hamburguesas"`)

| Tipo | Descripción | Rango de precio (ARS 2026) |
|------|-------------|---------------------------|
| **Simple** | 1 medallón de carne 150g | $10.500 – $12.000 |
| **Doble** | 2 medallones, total 300g | $14.000 – $15.500 |
| **Triple** | 3 medallones, total 450g | $18.500 – $21.000 |
| **Combos** | Hamburguesa + papas + bebida | $16.000 – $27.000 |
| **Veggie** | Medallón de lentejas/quinoa | $11.000 – $12.000 |

**Ingredientes válidos:** pan brioche/de campo/con semillas/integral, carne vacuna, cheddar, provolone, tybo, pepper jack, bacon, huevo frito, lechuga, tomate, rúcula, palta, cebolla caramelizada/morada, jalapeños, chimichurri, mayonesa casera, salsa BBQ, salsa especial, salsa picante, mostaza, pepinillos, jamón.

### 2. Acompañamientos (`categoria: "acompañamientos"`)

| Producto | Rango de precio (ARS 2026) |
|----------|---------------------------|
| Papas fritas (medianas/grandes) | $5.500 – $7.000 |
| Papas con cheddar y bacon | $8.000 – $9.000 |
| Aros de cebolla | $6.000 – $7.000 |
| Nuggets (x6 / x12) | $7.000 – $12.000 |
| Bastones de mozzarella | $7.000 – $8.000 |
| Ensalada | $5.500 – $6.500 |

### 3. Bebidas (`categoria: "bebidas"`)

| Producto | Rango de precio (ARS 2026) |
|----------|---------------------------|
| Gaseosas 500ml (Coca-Cola, Sprite, Fanta) | $3.500 |
| Agua mineral 500ml | $2.500 |
| Agua saborizada 500ml | $3.000 |
| Cerveza lata 473ml (Quilmes, Andes) | $4.500 |
| Cerveza premium lata 473ml (Patagonia) | $5.500 |
| Jugo natural 500ml | $4.000 |

---

## Datos de Prueba (perfil `dev`)

El `DevDataLoader` carga automáticamente al arrancar con perfil `dev`:

| Tabla | Cantidad | Detalle |
|-------|----------|---------|
| `usuario` | 4 | 1 admin, 1 cliente, 2 repartidores |
| `local` | 3 | Centro, Norte, Sur (Córdoba) |
| `horario_local` | 21 | 7 días × 3 locales |
| `producto` | 105 | 35 productos × 3 locales |
| `pedido` | 4 | Estados: ENTREGADO, EN_PREPARACION, PENDIENTE, CANCELADO |
| `item_pedido` | 10 | Items distribuidos en los 4 pedidos |
| `pago` | 2 | 1 EFECTIVO aprobado, 1 TARJETA aprobado |
| `carrito` | 1 | Cliente juan@test.com con 3 items |
| `item_carrito` | 3 | Doble Criolla + Papas Grandes + 2x Coca-Cola |

### Credenciales de prueba (password: `admin123`)

| Rol | Mail | DNI |
|-----|------|-----|
| ADMIN | admin@comidapp.ar | 99999999 |
| CLIENTE | juan@test.com | 12345678 |
| REPARTIDOR | maria@test.com | 87654321 |
| REPARTIDOR | carlos@test.com | 11223344 |

---

## Restricciones de Negocio

1. **NO** se agregan productos que no sean hamburguesas, acompañamientos o bebidas.
2. Los precios deben reflejar valores reales de Argentina 2026 (inflación considerada).
3. Las hamburguesas SIEMPRE deben indicar tipo (simple/doble/triple) e ingredientes.
4. Un local tiene los mismos 35 productos base.
5. Los horarios son Lun-Sáb 11:00-23:00, Dom 12:00-22:00.

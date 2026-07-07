package com.comidapp.infrastructure.config;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.comidapp.domain.entities.Admin;
import com.comidapp.domain.entities.Carrito;
import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.HorarioLocal;
import com.comidapp.domain.entities.ItemPedido;
import com.comidapp.domain.entities.Local;
import com.comidapp.domain.entities.Pago;
import com.comidapp.domain.entities.Pedido;
import com.comidapp.domain.entities.Producto;
import com.comidapp.domain.entities.Repartidor;
import com.comidapp.domain.enums.DiaSemana;
import com.comidapp.domain.enums.EstadoPago;
import com.comidapp.domain.enums.EstadoPedido;
import com.comidapp.domain.enums.MetodoPago;
import com.comidapp.infrastructure.persistence.CarritoRepository;
import com.comidapp.infrastructure.persistence.LocalRepository;
import com.comidapp.infrastructure.persistence.PagoRepository;
import com.comidapp.infrastructure.persistence.PedidoRepository;
import com.comidapp.infrastructure.persistence.ProductoRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;

/**
 * Carga datos iniciales en la base de datos MySQL (comida_app).
 * Solo inserta si la tabla usuario está vacía (idempotente).
 * Password de todos los usuarios de prueba: admin123
 */
@Component
public class DevDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataLoader.class);

    private final UsuarioRepository usuarioRepository;
    private final LocalRepository localRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final CarritoRepository carritoRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataLoader(UsuarioRepository usuarioRepository,
                         LocalRepository localRepository,
                         ProductoRepository productoRepository,
                         PedidoRepository pedidoRepository,
                         PagoRepository pagoRepository,
                         CarritoRepository carritoRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.localRepository = localRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.carritoRepository = carritoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Datos de prueba ya cargados, saltando...");
            return;
        }

        log.info("Cargando datos de prueba para perfil DEV...");
        cargarUsuarios();
        cargarLocalesConHorarios();
        cargarProductos();
        cargarPedidosYPagos();
        cargarCarritos();
        log.info("Datos de prueba cargados exitosamente.");
    }

    private void cargarUsuarios() {
        String hash = passwordEncoder.encode("admin123");

        // Admin
        Admin admin = new Admin();
        admin.setDni(99999999);
        admin.setNombre("Admin");
        admin.setApellido("ComidApp");
        admin.setMail("admin@comidapp.ar");
        admin.setContrasenia(hash);
        admin.setTelefono(0);
        usuarioRepository.save(admin);

        // Cliente de prueba
        Cliente cliente = new Cliente();
        cliente.setDni(12345678);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setMail("juan@test.com");
        cliente.setContrasenia(hash);
        cliente.setTelefono(351555123);
        cliente.setDirEntrega("Av. Colon 123");
        cliente.setCiudad("Cordoba");
        usuarioRepository.save(cliente);

        // Cliente 2
        Cliente cliente2 = new Cliente();
        cliente2.setDni(22334455);
        cliente2.setNombre("Laura");
        cliente2.setApellido("Martinez");
        cliente2.setMail("laura@test.com");
        cliente2.setContrasenia(hash);
        cliente2.setTelefono(351555666);
        cliente2.setDirEntrega("Bv. San Juan 800");
        cliente2.setCiudad("Cordoba");
        usuarioRepository.save(cliente2);

        // Cliente 3
        Cliente cliente3 = new Cliente();
        cliente3.setDni(33445566);
        cliente3.setNombre("Pedro");
        cliente3.setApellido("Gonzalez");
        cliente3.setMail("pedro@test.com");
        cliente3.setContrasenia(hash);
        cliente3.setTelefono(351555777);
        cliente3.setDirEntrega("Calle Dean Funes 200");
        cliente3.setCiudad("Cordoba");
        usuarioRepository.save(cliente3);

        // Repartidor 1
        Repartidor rep1 = new Repartidor();
        rep1.setDni(87654321);
        rep1.setNombre("Maria");
        rep1.setApellido("Lopez");
        rep1.setMail("maria@test.com");
        rep1.setContrasenia(hash);
        rep1.setTelefono(351555987);
        rep1.setDisponible(true);
        usuarioRepository.save(rep1);

        // Repartidor 2
        Repartidor rep2 = new Repartidor();
        rep2.setDni(11223344);
        rep2.setNombre("Carlos");
        rep2.setApellido("Garcia");
        rep2.setMail("carlos@test.com");
        rep2.setContrasenia(hash);
        rep2.setTelefono(351555432);
        rep2.setDisponible(true);
        usuarioRepository.save(rep2);

        log.info("  → 6 usuarios creados (admin, 3 clientes, 2 repartidores)");
    }

    private void cargarLocalesConHorarios() {
        String[][] localesData = {
            {"Alfesco Burgers Centro", "Av. San Martin 100, Centro", "0351-4001001"},
            {"Alfesco Burgers Norte", "Av. Rafael Nunez 4500, Cerro", "0351-4001002"},
            {"Alfesco Burgers Sur", "Av. Sabattini 3200, Barrio SEP", "0351-4001003"},
            {"Alfesco Burgers Florida", "Florida 800, CABA", "011-4001004"},
            {"Alfesco Burgers Palermo", "Honduras 5100, Palermo, CABA", "011-4001005"}
        };

        for (String[] data : localesData) {
            Local local = new Local();
            local.setNombre(data[0]);
            local.setDireccion(data[1]);
            local.setTelefono(data[2]);

            boolean esFlorida = data[0].contains("Florida");
            boolean esPalermo = data[0].contains("Palermo");
            boolean esNocturno = esFlorida || esPalermo;
            boolean esCentro = data[0].contains("Centro");

            // Horarios: Centro 08:00-23:00, Florida 11:00-02:00, Palermo 11:00-03:00, Resto 11:00-23:00, Dom 12:00-22:00
            for (DayOfWeek dia : DayOfWeek.values()) {
                HorarioLocal h = new HorarioLocal();
                h.setLocal(local);
                h.setDiaSemana(DiaSemana.fromDayOfWeek(dia));
                if (dia == DayOfWeek.SUNDAY) {
                    h.setHoraApertura(LocalTime.of(12, 0));
                    h.setHoraCierre(LocalTime.of(22, 0));
                } else if (esPalermo) {
                    h.setHoraApertura(LocalTime.of(11, 0));
                    h.setHoraCierre(LocalTime.of(3, 0));
                } else if (esFlorida) {
                    h.setHoraApertura(LocalTime.of(11, 0));
                    h.setHoraCierre(LocalTime.of(2, 0));
                } else if (esCentro) {
                    h.setHoraApertura(LocalTime.of(8, 0));
                    h.setHoraCierre(LocalTime.of(23, 0));
                } else {
                    h.setHoraApertura(LocalTime.of(11, 0));
                    h.setHoraCierre(LocalTime.of(23, 0));
                }
                local.getHorarios().add(h);
            }

            localRepository.save(local);
        }

        log.info("  → 5 locales con horarios creados");
    }

    private void cargarProductos() {
        List<Local> locales = localRepository.findAll();

        // ═══════════════════════════════════════════════════════════════════════
        // CATÁLOGO ComidApp — Precios Argentina Junio 2026
        // Categorías: hamburguesas | acompañamientos | bebidas
        // ═══════════════════════════════════════════════════════════════════════

        String[][] productosBase = {
            // ── HAMBURGUESAS SIMPLES (1 medallón 150g) ────────────────────────
            {"Hamburguesa Clásica", "Pan brioche, carne 150g, lechuga, tomate, queso tybo, mayonesa casera", "10500.00", "hamburguesas"},
            {"Hamburguesa Criolla", "Pan de campo, carne 150g, huevo frito, chimichurri, lechuga, tomate", "11000.00", "hamburguesas"},
            {"Hamburguesa BBQ", "Pan con semillas, carne 150g, bacon, cheddar, salsa BBQ ahumada, cebolla caramelizada", "12000.00", "hamburguesas"},
            {"Hamburguesa Veggie", "Pan integral, medallon de lentejas y quinoa, rucula, tomate seco, palta", "11500.00", "hamburguesas"},
            {"Hamburguesa Picante", "Pan brioche, carne 150g, jalapeños, pepper jack, salsa picante, cebolla morada", "11500.00", "hamburguesas"},

            // ── HAMBURGUESAS DOBLES (2 medallones 300g total) ─────────────────
            {"Doble Cheddar", "Pan brioche, doble carne 300g, doble cheddar, bacon crocante, salsa especial", "14500.00", "hamburguesas"},
            {"Doble Criolla", "Pan de campo, doble carne 300g, huevo, provolone, chimichurri", "15000.00", "hamburguesas"},
            {"Doble BBQ Bacon", "Pan con semillas, doble carne 300g, triple bacon, cheddar, BBQ ahumada", "15500.00", "hamburguesas"},
            {"Doble Completa", "Pan brioche, doble carne 300g, jamón, queso, huevo, lechuga, tomate, mayonesa", "15000.00", "hamburguesas"},
            {"Doble Smash", "Pan brioche, 2 smash patties crocantes, american cheese, pepinillos, mostaza", "14000.00", "hamburguesas"},

            // ── HAMBURGUESAS TRIPLES (3 medallones 450g total) ────────────────
            {"Triple Inferno", "Pan brioche, triple carne 450g, triple cheddar, bacon, jalapeños, salsa inferno", "19500.00", "hamburguesas"},
            {"Triple ComidApp", "Pan brioche, triple carne 450g, cheddar, provolone, bacon, huevo, salsa de la casa", "20000.00", "hamburguesas"},
            {"Triple Bacon Monster", "Pan con semillas, triple carne 450g, quíntuple bacon, cheddar, BBQ", "21000.00", "hamburguesas"},
            {"Triple Clásica XL", "Pan brioche XL, triple carne 450g, lechuga, tomate, cebolla, queso, mayo", "18500.00", "hamburguesas"},

            // ── COMBOS ────────────────────────────────────────────────────────
            // Los combos exclusivos se crean por local más abajo

            // ── ACOMPAÑAMIENTOS ───────────────────────────────────────────────
            {"Papas Fritas Medianas", "Porción 300g con salsa a elección (mayo, ketchup, BBQ)", "5500.00", "acompañamientos"},
            {"Papas Fritas Grandes", "Porción 500g con salsa a elección", "7000.00", "acompañamientos"},
            {"Papas con Cheddar y Bacon", "Porción 400g con cheddar fundido y bacon crocante", "8500.00", "acompañamientos"},
            {"Aros de Cebolla", "Porción 250g rebozados con dip barbacoa", "6500.00", "acompañamientos"},
            {"Nuggets x6", "6 nuggets de pollo con dip a elección", "7000.00", "acompañamientos"},
            {"Nuggets x12", "12 nuggets de pollo con 2 dips a elección", "12000.00", "acompañamientos"},
            {"Bastones de Mozzarella x6", "6 bastones de muzza con salsa marinara", "7500.00", "acompañamientos"},
            {"Ensalada César", "Lechuga, croutones, parmesano, aderezo césar", "6000.00", "acompañamientos"},

            // ── BEBIDAS ───────────────────────────────────────────────────────
            {"Coca-Cola 500ml", "Gaseosa Coca-Cola línea regular 500ml", "3500.00", "bebidas"},
            {"Coca-Cola Zero 500ml", "Gaseosa Coca-Cola Zero 500ml", "3500.00", "bebidas"},
            {"Sprite 500ml", "Gaseosa Sprite 500ml", "3500.00", "bebidas"},
            {"Fanta 500ml", "Gaseosa Fanta naranja 500ml", "3500.00", "bebidas"},
            {"Agua Mineral 500ml", "Agua mineral sin gas Villavicencio 500ml", "2500.00", "bebidas"},
            {"Agua Saborizada 500ml", "Agua saborizada Levité pomelo rosado 500ml", "3000.00", "bebidas"},
            {"Cerveza Quilmes 473ml", "Lata Quilmes Cristal 473ml", "4500.00", "bebidas"},
            {"Cerveza Andes 473ml", "Lata Andes Origen rubia 473ml", "4500.00", "bebidas"},
            {"Cerveza Patagonia 473ml", "Lata Patagonia Amber Lager 473ml", "5500.00", "bebidas"},
            {"Jugo Natural 500ml", "Jugo de naranja exprimido del día", "4000.00", "bebidas"},
        };

        for (Local local : locales) {
            for (String[] data : productosBase) {
                Producto p = new Producto();
                p.setNombre(data[0]);
                p.setDescripcion(data[1]);
                p.setPrecioUnitario(Double.parseDouble(data[2]));
                p.setCategoria(data[3]);
                p.setDisponible(true);
                p.setLocal(local);
                productoRepository.save(p);
            }
        }

        // ── COMBOS EXCLUSIVOS POR LOCAL ──────────────────────────────────────
        String[][][] combosPorLocal = {
            { // Centro
                {"Combo Centro Clásico", "Hamburguesa Clásica + Papas medianas + Coca-Cola 500ml", "16000.00", "combos"},
                {"Combo Centro Doble", "Doble Cheddar + Papas grandes + Cerveza Quilmes 473ml", "22000.00", "combos"},
            },
            { // Norte
                {"Combo Norte BBQ", "Hamburguesa BBQ + Aros de cebolla + Sprite 500ml", "18500.00", "combos"},
                {"Combo Norte Triple", "Triple ComidApp + Papas con cheddar + Cerveza Patagonia 473ml", "28000.00", "combos"},
            },
            { // Sur
                {"Combo Sur Criolla", "Hamburguesa Criolla + Nuggets x6 + Jugo Natural 500ml", "19000.00", "combos"},
                {"Combo Sur Smash", "Doble Smash + Papas medianas + Coca-Cola Zero 500ml", "20000.00", "combos"},
            },
            { // Florida
                {"Combo Florida Premium", "Triple Bacon Monster + Papas con cheddar + Cerveza Patagonia 473ml", "30000.00", "combos"},
                {"Combo Florida Nocturno", "Doble BBQ Bacon + Bastones de Mozzarella x6 + Cerveza Andes 473ml", "24000.00", "combos"},
            },
            { // Palermo
                {"Combo Palermo Veggie", "Hamburguesa Veggie + Ensalada César + Agua Saborizada 500ml", "20000.00", "combos"},
                {"Combo Palermo Inferno", "Triple Inferno + Nuggets x12 + Cerveza Quilmes 473ml", "32000.00", "combos"},
            }
        };

        for (int i = 0; i < locales.size() && i < combosPorLocal.length; i++) {
            Local local = locales.get(i);
            for (String[] data : combosPorLocal[i]) {
                Producto p = new Producto();
                p.setNombre(data[0]);
                p.setDescripcion(data[1]);
                p.setPrecioUnitario(Double.parseDouble(data[2]));
                p.setCategoria(data[3]);
                p.setDisponible(true);
                p.setLocal(local);
                productoRepository.save(p);
            }
        }

        log.info("  → {} productos base + 10 combos exclusivos creados", locales.size() * productosBase.length);
    }

    private void cargarPedidosYPagos() {
        // Obtener usuario cliente (juan@test.com / DNI 12345678)
        Cliente cliente = (Cliente) usuarioRepository.findByMail("juan@test.com")
                .orElseThrow();
        Repartidor rep = (Repartidor) usuarioRepository.findByMail("maria@test.com")
                .orElseThrow();
        Local local = localRepository.findAll().get(0);
        List<Producto> productos = productoRepository.findByLocalId(local.getId());

        // Pedido 1: ENTREGADO (ayer)
        Pedido p1 = new Pedido();
        p1.setCliente(cliente);
        p1.setLocal(local);
        p1.setRepartidor(rep);
        p1.setMetodoPago(MetodoPago.EFECTIVO);
        p1.setEstado(EstadoPedido.ENTREGADO);
        p1.setEstadoPago(EstadoPago.APROBADO);
        p1.setFechaHora(LocalDateTime.now().minusDays(1).withHour(20).withMinute(30));
        p1.setDirEntrega("Av. Colon 123");
        p1.setPrecioTotal(0.0);

        ItemPedido i1 = new ItemPedido(p1, productos.get(0), 2); // 2x Hamburguesa Clásica
        ItemPedido i2 = new ItemPedido(p1, productos.get(14), 1); // Papas medianas
        ItemPedido i3 = new ItemPedido(p1, productos.get(22), 2); // 2x Coca-Cola
        p1.getItems().add(i1);
        p1.getItems().add(i2);
        p1.getItems().add(i3);
        p1.calcularTotal();
        pedidoRepository.save(p1);

        Pago pago1 = new Pago();
        pago1.setPedido(p1);
        pago1.setMetodoPago(MetodoPago.EFECTIVO);
        pago1.setEstado(EstadoPago.APROBADO);
        pago1.setMonto(p1.getPrecioTotal());
        pagoRepository.save(pago1);

        // Pedido 2: EN_PREPARACION (hoy)
        Pedido p2 = new Pedido();
        p2.setCliente(cliente);
        p2.setLocal(local);
        p2.setRepartidor(rep);
        p2.setMetodoPago(MetodoPago.TARJETA);
        p2.setEstado(EstadoPedido.EN_PREPARACION);
        p2.setEstadoPago(EstadoPago.APROBADO);
        p2.setFechaHora(LocalDateTime.now().minusMinutes(20));
        p2.setDirEntrega("Av. Colon 123");
        p2.setPrecioTotal(0.0);

        ItemPedido i4 = new ItemPedido(p2, productos.get(5), 1); // Doble Cheddar
        ItemPedido i5 = new ItemPedido(p2, productos.get(16), 1); // Papas con cheddar
        ItemPedido i6 = new ItemPedido(p2, productos.get(28), 1); // Cerveza Quilmes
        p2.getItems().add(i4);
        p2.getItems().add(i5);
        p2.getItems().add(i6);
        p2.calcularTotal();
        pedidoRepository.save(p2);

        Pago pago2 = new Pago();
        pago2.setPedido(p2);
        pago2.setMetodoPago(MetodoPago.TARJETA);
        pago2.setEstado(EstadoPago.APROBADO);
        pago2.setMonto(p2.getPrecioTotal());
        pago2.setMpPaymentId("MP-DEV-001");
        pagoRepository.save(pago2);

        // Pedido 3: PENDIENTE (hace 5 min)
        Pedido p3 = new Pedido();
        p3.setCliente(cliente);
        p3.setLocal(local);
        p3.setMetodoPago(MetodoPago.EFECTIVO);
        p3.setEstado(EstadoPedido.PENDIENTE);
        p3.setEstadoPago(EstadoPago.PENDIENTE);
        p3.setFechaHora(LocalDateTime.now().minusMinutes(5));
        p3.setDirEntrega("Av. Colon 123");
        p3.setPrecioTotal(0.0);

        ItemPedido i7 = new ItemPedido(p3, productos.get(10), 1); // Triple Inferno
        ItemPedido i8 = new ItemPedido(p3, productos.get(18), 1); // Nuggets x6
        ItemPedido i9 = new ItemPedido(p3, productos.get(24), 1); // Sprite
        p3.getItems().add(i7);
        p3.getItems().add(i8);
        p3.getItems().add(i9);
        p3.calcularTotal();
        pedidoRepository.save(p3);

        // Pedido 4: CANCELADO (antesdeayer)
        Pedido p4 = new Pedido();
        p4.setCliente(cliente);
        p4.setLocal(local);
        p4.setMetodoPago(MetodoPago.EFECTIVO);
        p4.setEstado(EstadoPedido.CANCELADO);
        p4.setEstadoPago(EstadoPago.PENDIENTE);
        p4.setFechaHora(LocalDateTime.now().minusDays(2).withHour(13).withMinute(0));
        p4.setDirEntrega("Av. Colon 123");
        p4.setPrecioTotal(0.0);

        ItemPedido i10 = new ItemPedido(p4, productos.get(0), 1); // Hamburguesa Clásica
        p4.getItems().add(i10);
        p4.calcularTotal();
        pedidoRepository.save(p4);

        log.info("  → 4 pedidos creados (ENTREGADO, EN_PREPARACION, PENDIENTE, CANCELADO)");
    }

    private void cargarCarritos() {
        // Carrito con items para el cliente de prueba
        Cliente cliente = (Cliente) usuarioRepository.findByMail("juan@test.com")
                .orElseThrow();
        Local local = localRepository.findAll().get(0);
        List<Producto> productos = productoRepository.findByLocalId(local.getId());

        Carrito carrito = new Carrito();
        carrito.setCliente(cliente);
        // No setear local: agregarItem() lo setea automáticamente del primer producto
        carrito.agregarItem(productos.get(7), 1);  // Doble BBQ Bacon
        carrito.agregarItem(productos.get(15), 1); // Papas Grandes
        carrito.agregarItem(productos.get(22), 2); // 2x Coca-Cola
        carritoRepository.save(carrito);

        log.info("  → 1 carrito con 3 items creado");
    }

}

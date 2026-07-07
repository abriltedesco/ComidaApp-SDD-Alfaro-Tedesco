package com.comidapp.api.controllers;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.comidapp.domain.entities.Admin;
import com.comidapp.domain.entities.Cliente;
import com.comidapp.domain.entities.HorarioLocal;
import com.comidapp.domain.entities.Local;
import com.comidapp.domain.entities.Producto;
import com.comidapp.domain.entities.Repartidor;
import com.comidapp.domain.enums.DiaSemana;
import com.comidapp.domain.enums.RolUsuario;
import com.comidapp.infrastructure.persistence.LocalRepository;
import com.comidapp.infrastructure.persistence.ProductoRepository;
import com.comidapp.infrastructure.persistence.UsuarioRepository;
import com.comidapp.infrastructure.security.JwtService;

/**
 * Tests de integración completos — cubre Auth, Menu, Carrito, Pedidos, Admin, Perfil, Seguridad.
 * Usa H2 en memoria con perfil test. Un solo contexto Spring compartido.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Integración API - Tests completos")
class ApiIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LocalRepository localRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private MockMvc mockMvc;
    private static String tokenCliente;
    private static String tokenAdmin;
    private static String tokenRepartidor;
    private static Long productoId;
    private static Long localId;
    private static boolean dataLoaded = false;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        if (!dataLoaded) {
            // Admin
            Admin admin = new Admin();
            admin.setDni(99999999);
            admin.setNombre("Admin");
            admin.setApellido("Test");
            admin.setMail("admin@integration.com");
            admin.setContrasenia(passwordEncoder.encode("admin123"));
            admin.setTelefono(351000001);
            usuarioRepository.save(admin);

            // Cliente
            Cliente cliente = new Cliente();
            cliente.setDni(11111111);
            cliente.setNombre("Juan");
            cliente.setApellido("TestCliente");
            cliente.setMail("cliente@integration.com");
            cliente.setContrasenia(passwordEncoder.encode("pass123"));
            cliente.setTelefono(351000002);
            cliente.setDirEntrega("Calle Test 1");
            cliente.setCiudad("Cordoba");
            usuarioRepository.save(cliente);

            // Repartidor
            Repartidor rep = new Repartidor();
            rep.setDni(22222222);
            rep.setNombre("Maria");
            rep.setApellido("TestRep");
            rep.setMail("rep@integration.com");
            rep.setContrasenia(passwordEncoder.encode("rep123"));
            rep.setTelefono(351000003);
            rep.setDisponible(true);
            usuarioRepository.save(rep);

            // Local con horarios todos los días
            Local local = new Local();
            local.setNombre("Local Integration");
            local.setDireccion("Av. Test 100");
            local.setTelefono("351-0000");
            for (DiaSemana dia : DiaSemana.values()) {
                HorarioLocal h = new HorarioLocal();
                h.setDiaSemana(dia);
                h.setHoraApertura(LocalTime.of(0, 0));
                h.setHoraCierre(LocalTime.of(23, 59));
                h.setLocal(local);
                local.getHorarios().add(h);
            }
            localRepository.save(local);
            localId = local.getId();

            // Producto
            Producto p = new Producto();
            p.setNombre("Hamburguesa Integration");
            p.setDescripcion("Desc");
            p.setPrecioUnitario(10500.0);
            p.setCategoria("hamburguesas");
            p.setDisponible(true);
            p.setLocal(local);
            productoRepository.save(p);
            productoId = p.getId();

            // Tokens
            tokenAdmin = jwtService.generarToken("admin@integration.com", RolUsuario.ADMIN, 99999999);
            tokenCliente = jwtService.generarToken("cliente@integration.com", RolUsuario.CLIENTE, 11111111);
            tokenRepartidor = jwtService.generarToken("rep@integration.com", RolUsuario.REPARTIDOR, 22222222);

            dataLoaded = true;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // AUTH
    // ════════════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("Auth: Login GET con credenciales válidas devuelve token")
    void loginExitoso() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login")
                        .param("mail", "cliente@integration.com")
                        .param("contrasenia", "pass123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test @Order(2)
    @DisplayName("Auth: Login con credenciales inválidas devuelve 401")
    void loginInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login")
                        .param("mail", "cliente@integration.com")
                        .param("contrasenia", "wrongpass"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(3)
    @DisplayName("Auth: Registro cliente devuelve 201")
    void registroCliente() throws Exception {
        String json = """
                {"dni":88888888,"nombre":"Nuevo","apellido":"Cliente",
                "mail":"nuevo@integration.com","contrasenia":"clave123",
                "telefono":351888888,"dirEntrega":"Calle 9","ciudad":"Cba"}
                """;
        mockMvc.perform(post("/api/v1/auth/registro/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    // ════════════════════════════════════════════════════════════════
    // MENU / LOCALES
    // ════════════════════════════════════════════════════════════════

    @Test @Order(10)
    @DisplayName("Menu: GET /locales devuelve locales con horarios en español")
    void listarLocales() throws Exception {
        mockMvc.perform(get("/api/v1/locales")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Local Integration"))
                .andExpect(jsonPath("$[0].horarios[0].diaSemana").exists());
    }

    @Test @Order(11)
    @DisplayName("Menu: GET /locales/{id}/menu no expone id ni localId")
    void menuSinIds() throws Exception {
        mockMvc.perform(get("/api/v1/locales/" + localId + "/menu")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hamburguesa Integration"))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].localId").doesNotExist());
    }

    // ════════════════════════════════════════════════════════════════
    // CARRITO
    // ════════════════════════════════════════════════════════════════

    @Test @Order(20)
    @DisplayName("Carrito: POST /carrito/items agrega item")
    void agregarItemCarrito() throws Exception {
        String json = "{\"productoId\":" + productoId + ",\"cantidad\":2}";
        mockMvc.perform(post("/api/v1/carrito/items")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    // ════════════════════════════════════════════════════════════════
    // PEDIDOS
    // ════════════════════════════════════════════════════════════════

    @Test @Order(30)
    @DisplayName("Pedidos: POST /pedidos confirma pedido desde carrito")
    void confirmarPedido() throws Exception {
        // Asegurar que hay algo en el carrito
        String itemJson = "{\"productoId\":" + productoId + ",\"cantidad\":1}";
        mockMvc.perform(post("/api/v1/carrito/items")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson));

        String json = "{\"metodoPago\":\"EFECTIVO\"}";
        mockMvc.perform(post("/api/v1/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.metodoPago").value("EFECTIVO"));
    }

    @Test @Order(31)
    @DisplayName("Pedidos: GET /pedidos/mis-pedidos devuelve historial")
    void misPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/mis-pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ════════════════════════════════════════════════════════════════
    // PAGOS
    // ════════════════════════════════════════════════════════════════

    @Test @Order(40)
    @DisplayName("Pagos: webhook MP es público (404 por pago inexistente)")
    void webhookPublico() throws Exception {
        String json = "{\"id\":\"99999\",\"status\":\"approved\"}";
        mockMvc.perform(post("/api/v1/pagos/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN
    // ════════════════════════════════════════════════════════════════

    @Test @Order(50)
    @DisplayName("Admin: GET /admin/productos lista productos")
    void adminListaProductos() throws Exception {
        mockMvc.perform(get("/api/v1/admin/productos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(51)
    @DisplayName("Admin: POST /admin/productos crea producto")
    void adminCreaProducto() throws Exception {
        String json = "{\"nombre\":\"NewBurger\",\"precioUnitario\":15000,\"categoria\":\"hamburguesas\",\"localId\":" + localId + "}";
        mockMvc.perform(post("/api/v1/admin/productos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("NewBurger"));
    }

    @Test @Order(52)
    @DisplayName("Admin: GET /admin/usuarios lista usuarios")
    void adminListaUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ════════════════════════════════════════════════════════════════
    // PERFIL
    // ════════════════════════════════════════════════════════════════

    @Test @Order(60)
    @DisplayName("Perfil: GET /perfil devuelve datos del usuario")
    void obtenerPerfil() throws Exception {
        mockMvc.perform(get("/api/v1/perfil")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.tipo").value("CLIENTE"));
    }

    @Test @Order(61)
    @DisplayName("Perfil: PUT /perfil actualiza datos")
    void actualizarPerfil() throws Exception {
        String json = "{\"nombre\":\"JuanModificado\"}";
        mockMvc.perform(put("/api/v1/perfil")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("JuanModificado"));
    }

    // ════════════════════════════════════════════════════════════════
    // NOTIFICACIONES
    // ════════════════════════════════════════════════════════════════

    @Test @Order(70)
    @DisplayName("Notificaciones: POST /notificaciones/suscribir registra push")
    void suscribirPush() throws Exception {
        String json = "{\"endpoint\":\"https://push.test.com/abc\",\"p256dh\":\"key123\",\"auth\":\"auth123\"}";
        mockMvc.perform(post("/api/v1/notificaciones/suscribir")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    // ════════════════════════════════════════════════════════════════
    // SEGURIDAD - Accesos no autorizados
    // ════════════════════════════════════════════════════════════════

    @Test @Order(80)
    @DisplayName("Seguridad: Sin token devuelve 401")
    void sinTokenEs401() throws Exception {
        mockMvc.perform(get("/api/v1/locales"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/carrito"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/perfil"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(81)
    @DisplayName("Seguridad: Token inválido devuelve 401")
    void tokenInvalidoEs401() throws Exception {
        mockMvc.perform(get("/api/v1/locales")
                        .header("Authorization", "Bearer token.invalido.xyz"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(82)
    @DisplayName("Seguridad: Cliente NO accede a admin")
    void clienteNoAccedeAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/productos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }

    @Test @Order(83)
    @DisplayName("Seguridad: Repartidor NO accede a admin")
    void repartidorNoAccedeAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios")
                        .header("Authorization", "Bearer " + tokenRepartidor))
                .andExpect(status().isForbidden());
    }

    @Test @Order(84)
    @DisplayName("Seguridad: Cliente NO accede a pedidos repartidor")
    void clienteNoAccedePedidosRep() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/repartidor")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
    }
}

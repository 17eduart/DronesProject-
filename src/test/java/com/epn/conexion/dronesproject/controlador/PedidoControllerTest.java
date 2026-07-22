package com.epn.conexion.dronesproject.controlador;

import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronCarga;
import com.epn.conexion.dronesproject.modelo.Pedido;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.seguridad.JwtAuthFilter;
import com.epn.conexion.dronesproject.seguridad.JwtService;
import com.epn.conexion.dronesproject.seguridad.ManejadorErroresSeguridad;
import com.epn.conexion.dronesproject.seguridad.SecurityConfig;
import com.epn.conexion.dronesproject.seguridad.UsuarioDetailsService;
import com.epn.conexion.dronesproject.servicio.PedidoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de regresion de la propiedad de los pedidos.
 *
 * Protege dos garantias distintas:
 *  - un CLIENTE solo ve lo suyo (403 si pide el pedido de otro);
 *  - un ADMINISTRADOR ve cualquiera.
 *
 * Igual que SeguridadDronTest, usa un JwtService real y simula de donde salen
 * las authorities y la capa de negocio, asi que no necesita MySQL.
 */
@WebMvcTest(PedidoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, ManejadorErroresSeguridad.class, JwtService.class})
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PedidoServicio pedidoServicio;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Pedido pedidoDeAna;

    @BeforeEach
    void prepararEscenario() {
        when(userDetailsService.loadUserByUsername("ana"))
                .thenReturn(usuarioCon("ana", Usuario.ROL_CLIENTE));
        when(userDetailsService.loadUserByUsername("beto"))
                .thenReturn(usuarioCon("beto", Usuario.ROL_CLIENTE));
        when(userDetailsService.loadUserByUsername("admin1"))
                .thenReturn(usuarioCon("admin1", Usuario.ROL_ADMINISTRADOR));

        Usuario ana = new Usuario("ana", "hash-simulado", Usuario.ROL_CLIENTE);
        Dron dron = new DronCarga("CG01", "Titan", 100.0, 29.0, 300.0);
        pedidoDeAna = new Pedido(ana, dron, 20.0, 8.0, 100.0,
                dron.calcularCosto(20.0, 8.0, 100.0), LocalDateTime.now());
        pedidoDeAna.setId(7L);
    }

    private User usuarioCon(String username, String rol) {
        return new User(username, "hash-simulado",
                List.of(new SimpleGrantedAuthority(UsuarioDetailsService.PREFIJO_ROL + rol)));
    }

    private String bearer(String username, String rol) {
        return "Bearer " + jwtService.generarToken(username, rol);
    }

    // ---------- Propiedad del pedido ----------

    @Test
    @DisplayName("Un CLIENTE ve su propio pedido")
    void cliente_ve_su_propio_pedido() throws Exception {
        when(pedidoServicio.buscarPorId(7L)).thenReturn(Optional.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos/7")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ana", Usuario.ROL_CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.costoTotal").value(29.60));
    }

    @Test
    @DisplayName("Un CLIENTE NO puede ver el pedido de otro CLIENTE: 403")
    void cliente_no_ve_pedido_ajeno() throws Exception {
        when(pedidoServicio.buscarPorId(7L)).thenReturn(Optional.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos/7")
                        .header(HttpHeaders.AUTHORIZATION, bearer("beto", Usuario.ROL_CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.Status").value(403))
                .andExpect(jsonPath("$.Error").value("Forbidden"));
    }

    @Test
    @DisplayName("Un ADMINISTRADOR si puede ver el pedido de cualquier CLIENTE")
    void administrador_ve_pedido_ajeno() throws Exception {
        when(pedidoServicio.buscarPorId(7L)).thenReturn(Optional.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos/7")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin1", Usuario.ROL_ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    @DisplayName("La factura de un pedido ajeno tambien es 403 para un CLIENTE")
    void factura_ajena_es_403() throws Exception {
        when(pedidoServicio.buscarPorId(7L)).thenReturn(Optional.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos/7/factura")
                        .header(HttpHeaders.AUTHORIZATION, bearer("beto", Usuario.ROL_CLIENTE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un pedido inexistente da 404, no 403, aunque el id no sea del cliente")
    void pedido_inexistente_es_404() throws Exception {
        when(pedidoServicio.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pedidos/99")
                        .header(HttpHeaders.AUTHORIZATION, bearer("beto", Usuario.ROL_CLIENTE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.Status").value(404));
    }

    // ---------- Listado segun rol ----------

    @Test
    @DisplayName("GET /pedidos de un CLIENTE consulta solo los suyos")
    void listado_de_cliente_filtra_por_usuario() throws Exception {
        when(pedidoServicio.listarPorUsuario("ana")).thenReturn(List.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ana", Usuario.ROL_CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana"));

        verify(pedidoServicio).listarPorUsuario("ana");
        verify(pedidoServicio, never()).listarTodo();
    }

    @Test
    @DisplayName("GET /pedidos de un ADMINISTRADOR consulta todos")
    void listado_de_admin_trae_todo() throws Exception {
        when(pedidoServicio.listarTodo()).thenReturn(List.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin1", Usuario.ROL_ADMINISTRADOR)))
                .andExpect(status().isOk());

        verify(pedidoServicio).listarTodo();
        verify(pedidoServicio, never()).listarPorUsuario(anyString());
    }

    // ---------- Creacion ----------

    @Test
    @DisplayName("POST /pedidos usa el username del token, no el del body")
    void crear_usa_el_username_del_token() throws Exception {
        when(pedidoServicio.crear(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(pedidoDeAna);

        // El body intenta colar "username":"admin1"; debe ignorarse.
        String body = """
                {"codigoDron":"CG01","distanciaSolicitada":20.0,"pesoSolicitado":8.0,
                 "horasSolicitadas":100.0,"username":"admin1","costoTotal":0.01}
                """;

        mockMvc.perform(post("/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ana", Usuario.ROL_CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costoTotal").value(29.60));

        verify(pedidoServicio).crear(eq("ana"), eq("CG01"), eq(20.0), eq(8.0), eq(100.0));
    }

    @Test
    @DisplayName("Exceder la capacidad del dron responde 400 con el mensaje de la validacion")
    void capacidad_excedida_es_400() throws Exception {
        when(pedidoServicio.crear(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new IllegalArgumentException(
                        "El peso solicitado (50.0 kg) supera la capacidad del modelo Titan, cuyo maximo es 29.0 kg"));

        String body = """
                {"codigoDron":"CG01","distanciaSolicitada":20.0,"pesoSolicitado":50.0,"horasSolicitadas":100.0}
                """;

        mockMvc.perform(post("/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ana", Usuario.ROL_CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Status").value(400))
                .andExpect(jsonPath("$.Mensaje").value(
                        org.hamcrest.Matchers.containsString("supera la capacidad")));
    }

    @Test
    @DisplayName("POST /pedidos sin token es 401")
    void crear_sin_token_es_401() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoDron\":\"CG01\"}"))
                .andExpect(status().isUnauthorized());

        verify(pedidoServicio, never()).crear(any(), any(), any(), any(), any());
    }

    // ---------- Factura ----------

    @Test
    @DisplayName("La factura llega desglosada y sus componentes suman el total")
    void factura_desglosada() throws Exception {
        when(pedidoServicio.buscarPorId(7L)).thenReturn(Optional.of(pedidoDeAna));

        mockMvc.perform(get("/pedidos/7/factura")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ana", Usuario.ROL_CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroFactura").value("FAC-000007"))
                .andExpect(jsonPath("$.nombreCliente").value("ana"))
                .andExpect(jsonPath("$.detalleDron").value("Titan (CARGA)"))
                // 6.0 + (20 * 0.70) + (8 * 1.20) = 29.60
                .andExpect(jsonPath("$.costoBase").value(6.0))
                .andExpect(jsonPath("$.componenteDistancia").value(14.0))
                .andExpect(jsonPath("$.componentePeso").value(9.6))
                .andExpect(jsonPath("$.total").value(29.60));
    }
}

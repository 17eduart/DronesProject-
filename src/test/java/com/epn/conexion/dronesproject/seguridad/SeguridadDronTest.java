package com.epn.conexion.dronesproject.seguridad;

import com.epn.conexion.dronesproject.controlador.DroneController;
import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronLiviano;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.servicio.DronServicio;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de regresion de la autorizacion por rol sobre /dron.
 *
 * Usa un JwtService real (no mock) para que los tokens sean tokens de verdad,
 * firmados con el secreto de application.properties. Lo unico simulado es de
 * donde salen las authorities (UserDetailsService) y la capa de negocio
 * (DronServicio), asi no hace falta MySQL.
 *
 * Importante: el rol efectivo lo decide el UserDetailsService, no el claim del
 * token. Por eso el mock devuelve la authority y el token solo identifica al
 * usuario.
 */
@WebMvcTest(DroneController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, ManejadorErroresSeguridad.class, JwtService.class})
class SeguridadDronTest {

    private static final String BODY_DRON = """
            {"codigo":"LV99","modelo":"Maverick","distancia_km":10.0,
             "peso_maximo":3.0,"horas_vuelo":110.0}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private DronServicio dronServicio;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void prepararUsuarios() {
        when(userDetailsService.loadUserByUsername("cliente1"))
                .thenReturn(usuarioCon("cliente1", Usuario.ROL_CLIENTE));
        when(userDetailsService.loadUserByUsername("admin1"))
                .thenReturn(usuarioCon("admin1", Usuario.ROL_ADMINISTRADOR));
    }

    private User usuarioCon(String username, String rol) {
        return new User(username, "hash-simulado",
                List.of(new SimpleGrantedAuthority(UsuarioDetailsService.PREFIJO_ROL + rol)));
    }

    private String bearer(String username, String rol) {
        return "Bearer " + jwtService.generarToken(username, rol);
    }

    @Test
    @DisplayName("GET /dron sin token responde 401 con el formato JSON de la app")
    void get_sin_token_es_401() throws Exception {
        mockMvc.perform(get("/dron"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.Status").value(401))
                .andExpect(jsonPath("$.Error").value("Unauthorized"))
                .andExpect(jsonPath("$.Mensaje").exists());

        verify(dronServicio, never()).listarTodo();
    }

    @Test
    @DisplayName("GET /dron con token de CLIENTE responde 200")
    void get_con_token_de_cliente_es_200() throws Exception {
        when(dronServicio.listarTodo()).thenReturn(List.of());

        mockMvc.perform(get("/dron")
                        .header(HttpHeaders.AUTHORIZATION, bearer("cliente1", Usuario.ROL_CLIENTE)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /dron/{tipo} con token de CLIENTE responde 403")
    void post_como_cliente_es_403() throws Exception {
        mockMvc.perform(post("/dron/LIVIANO")
                        .header(HttpHeaders.AUTHORIZATION, bearer("cliente1", Usuario.ROL_CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DRON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.Status").value(403))
                .andExpect(jsonPath("$.Error").value("Forbidden"));

        // Lo que de verdad importa: el CLIENTE no llego a tocar el catalogo.
        verify(dronServicio, never())
                .insertar(anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("POST /dron/{tipo} con token de ADMINISTRADOR responde 200")
    void post_como_administrador_es_200() throws Exception {
        Dron creado = new DronLiviano("LV99", "Maverick", 10.0, 3.0, 110.0);
        when(dronServicio.insertar(anyString(), anyString(), anyString(),
                anyDouble(), anyDouble(), anyDouble())).thenReturn(creado);

        mockMvc.perform(post("/dron/LIVIANO")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin1", Usuario.ROL_ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DRON))
                .andExpect(status().isOk());

        verify(dronServicio).insertar(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Un token con firma invalida no autentica: 401")
    void token_con_firma_falsa_es_401() throws Exception {
        // Mismo formato, firma alterada en el ultimo caracter.
        String tokenValido = jwtService.generarToken("admin1", Usuario.ROL_ADMINISTRADOR);
        String tokenAlterado = tokenValido.substring(0, tokenValido.length() - 1)
                + (tokenValido.endsWith("A") ? "B" : "A");

        mockMvc.perform(get("/dron").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAlterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El claim rol del token no otorga permisos por si solo")
    void el_claim_rol_no_manda_sobre_la_base() throws Exception {
        // Token que dice ADMINISTRADOR, pero el usuario en "la base" es CLIENTE.
        // Debe mandar la base: 403. Si alguien cambia el filtro para leer las
        // authorities del claim en vez del UserDetailsService, esto pasa a 200.
        mockMvc.perform(post("/dron/LIVIANO")
                        .header(HttpHeaders.AUTHORIZATION, bearer("cliente1", Usuario.ROL_ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DRON))
                .andExpect(status().isForbidden());
    }
}

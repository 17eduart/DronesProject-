package com.epn.conexion.dronesproject.controlador;

import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRequest;
import com.epn.conexion.dronesproject.servicio.UsuarioServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de regresion del endpoint publico POST /registro.
 *
 * Protege el fix de escalacion de privilegios: el registro publico debe crear
 * SIEMPRE usuarios con rol CLIENTE, ignorando cualquier "rol" que mande el
 * cliente en el body. Si alguien vuelve a leer el rol del request (por ejemplo
 * reemplazando UsuarioRequest por la entidad Usuario), estas pruebas fallan.
 *
 * Usa el slice @WebMvcTest: levanta solo la capa web, sin JPA ni MySQL.
 */
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioServicio usuarioServicio;

    @Test
    @DisplayName("POST /registro ignora el rol del body y registra siempre como CLIENTE")
    void registro_ignora_el_rol_enviado_por_el_cliente() throws Exception {
        when(usuarioServicio.registrar(anyString(), anyString(), anyString()))
                .thenAnswer(invocacion -> new Usuario(
                        invocacion.getArgument(0),
                        "hash-simulado",
                        invocacion.getArgument(2)));

        String bodyMalicioso = """
                {"username":"atacante","password":"secreto123","rol":"ADMINISTRADOR"}
                """;

        // El campo extra "rol" no debe romper la deserializacion: se ignora.
        mockMvc.perform(post("/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMalicioso))
                .andExpect(status().isCreated());

        // Lo importante no es el codigo HTTP, sino que rol llego al servicio.
        ArgumentCaptor<String> rolCapturado = ArgumentCaptor.forClass(String.class);
        verify(usuarioServicio).registrar(eq("atacante"), eq("secreto123"), rolCapturado.capture());

        assertEquals(Usuario.ROL_CLIENTE, rolCapturado.getValue(),
                "El registro publico no debe propagar el rol enviado por el cliente");

        // Verificacion redundante a proposito: deja explicito que nunca se crea un ADMINISTRADOR.
        verify(usuarioServicio, never())
                .registrar(anyString(), anyString(), eq(Usuario.ROL_ADMINISTRADOR));
    }

    @Test
    @DisplayName("POST /registro con username y password validos responde 201 Created")
    void registro_valido_responde_201() throws Exception {
        when(usuarioServicio.registrar(anyString(), anyString(), anyString()))
                .thenReturn(new Usuario("cliente1", "hash-simulado", Usuario.ROL_CLIENTE));

        String body = """
                {"username":"cliente1","password":"secreto123"}
                """;

        mockMvc.perform(post("/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(usuarioServicio).registrar("cliente1", "secreto123", Usuario.ROL_CLIENTE);
    }

    @Test
    @DisplayName("La entidad Usuario nunca se usa como body del registro")
    void el_endpoint_no_acepta_la_entidad_como_body() {
        // Guarda de diseno: si alguien revierte el DTO y vuelve a poner
        // @RequestBody Usuario, el tipo del parametro cambia y esto falla.
        Method registrar = Arrays.stream(UsuarioController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("registrar"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No existe el metodo registrar en UsuarioController"));

        assertEquals(1, registrar.getParameterCount(),
                "El registro debe recibir un unico DTO como body");
        assertEquals(UsuarioRequest.class, registrar.getParameterTypes()[0],
                "El body de /registro debe ser un DTO sin campo rol, no la entidad Usuario");
    }
}

package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integracion de registro y login (antes estaba como CommandLineRunner
 * en la clase Test). @Transactional hace rollback al terminar cada prueba.
 */
@SpringBootTest
@Transactional
class UsuarioServicioTest {

    @Autowired
    private UsuarioServicio usuarioServicio;

    /**
     * Username distinto en cada ejecucion. La tabla usuarios es compartida y
     * puede traer filas de corridas anteriores (por ejemplo el "eduart" que
     * insertaba el viejo CommandLineRunner en cada arranque); con un nombre
     * fijo el test fallaria por "El usuario ya existe" sin que el codigo de
     * produccion tenga nada malo.
     */
    private String username;

    @BeforeEach
    void generarUsername() {
        username = "test-" + UUID.randomUUID();
    }

    @Test
    @DisplayName("El registro guarda la contrasena hasheada, nunca en texto plano")
    void registrar_hashea_la_contrasena() {
        Usuario creado = usuarioServicio.registrar(username, "prueba1", Usuario.ROL_CLIENTE);

        assertEquals(username, creado.getUsername());
        assertNotEquals("prueba1", creado.getPassword());
    }

    @Test
    @DisplayName("No se puede registrar dos veces el mismo username")
    void registrar_username_duplicado_falla() {
        usuarioServicio.registrar(username, "prueba1", Usuario.ROL_CLIENTE);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioServicio.registrar(username, "otra", Usuario.ROL_CLIENTE));
    }

    @Test
    @DisplayName("Login correcto e incorrecto")
    void login_valida_credenciales() {
        usuarioServicio.registrar(username, "prueba1", Usuario.ROL_CLIENTE);

        assertTrue(usuarioServicio.login(username, "prueba1"));
        assertFalse(usuarioServicio.login(username, "malo"));
        assertFalse(usuarioServicio.login("no-existe-" + UUID.randomUUID(), "prueba1"));
    }
}

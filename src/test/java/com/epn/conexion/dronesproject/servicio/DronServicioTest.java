package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronCarga;
import com.epn.conexion.dronesproject.modelo.DronEmergencia;
import com.epn.conexion.dronesproject.modelo.DronLiviano;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integracion del CRUD de drones (antes estaba como CommandLineRunner
 * en la clase Test). Necesita la base de datos configurada en
 * application.properties; @Transactional hace rollback al terminar cada prueba,
 * asi que no deja basura en la tabla dron.
 */
@SpringBootTest
@Transactional
class DronServicioTest {

    @Autowired
    private DronServicio dronServicio;

    @Test
    @DisplayName("Insertar, buscar, actualizar y eliminar drones")
    void crud_completo() {
        dronServicio.insertar("LIVIANO", "TLV01", "Maverick", 10.0, 3.0, 110.0);
        dronServicio.insertar("CARGA", "TCG01", "Titan", 20.0, 8.0, 200.0);
        dronServicio.insertar("EMERGENCIA", "TEM01", "Rescue", 20.0, 8.0, 150.0);

        List<Dron> todos = dronServicio.listarTodo();
        assertTrue(todos.size() >= 3);

        Optional<Dron> liviano = dronServicio.buscarCodigo("TLV01");
        assertTrue(liviano.isPresent());
        assertEquals("Maverick", liviano.get().getModelo());

        dronServicio.actualizar("LIVIANO", "TLV01", "Maverick-Editado", 12.0, 3.5, 100.0);
        assertEquals("Maverick-Editado",
                dronServicio.buscarCodigo("TLV01").orElseThrow().getModelo());

        dronServicio.eliminar("TCG01");
        assertFalse(dronServicio.buscarCodigo("TCG01").isPresent());
    }

    @Test
    @DisplayName("El discriminador persiste la subclase correcta (polimorfismo)")
    void guarda_la_subclase_segun_el_tipo() {
        dronServicio.insertar("LIVIANO", "TLV02", "Maverick", 10.0, 3.0, 110.0);
        dronServicio.insertar("CARGA", "TCG02", "Titan", 20.0, 8.0, 200.0);
        dronServicio.insertar("EMERGENCIA", "TEM02", "Rescue", 20.0, 8.0, 150.0);

        assertInstanceOf(DronLiviano.class, dronServicio.buscarCodigo("TLV02").orElseThrow());
        assertInstanceOf(DronCarga.class, dronServicio.buscarCodigo("TCG02").orElseThrow());
        assertInstanceOf(DronEmergencia.class, dronServicio.buscarCodigo("TEM02").orElseThrow());
    }

    @Test
    @DisplayName("Un tipo desconocido lanza IllegalArgumentException")
    void tipo_invalido_falla() {
        assertThrows(IllegalArgumentException.class,
                () -> dronServicio.insertar("VOLADOR", "TX1", "x", 1.0, 1.0, 1.0));
    }

    @Test
    @DisplayName("Las validaciones de la subclase se aplican tambien al insertar")
    void peso_invalido_falla() {
        assertThrows(IllegalArgumentException.class,
                () -> dronServicio.insertar("LIVIANO", "TX2", "x", 1.0, 10.0, 1.0));
    }
}

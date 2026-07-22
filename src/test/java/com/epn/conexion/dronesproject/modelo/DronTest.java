package com.epn.conexion.dronesproject.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de negocio de la jerarquia Dron.
 * No necesitan Spring ni base de datos: son objetos puros.
 */
class DronTest {

    @Test
    @DisplayName("El costo de cada tipo de dron se calcula con su propia formula")
    void calcular_costo_por_tipo() {
        Dron liviano = new DronLiviano("LV01", "Maverick", 10.0, 3.0, 110.0);
        Dron carga = new DronCarga("CG01", "Titan", 20.0, 8.0, 200.0);
        Dron emergencia = new DronEmergencia("EM01", "Rescue", 20.0, 8.0, 150.0);

        // 0.50 + (10 * 0.50)
        assertEquals(5.50, liviano.calcular_costo(), 0.0001);
        // 6.0 + (20 * 0.70) + (8 * 1.20)
        assertEquals(29.60, carga.calcular_costo(), 0.0001);
        // 15.0 + (20 * 1.00) + (8 * 1.50)
        assertEquals(47.00, emergencia.calcular_costo(), 0.0001);
    }

    @Test
    @DisplayName("El dron de emergencia es el mas caro a igualdad de distancia y peso")
    void emergencia_es_el_tipo_mas_caro() {
        Dron carga = new DronCarga("CG02", "Titan", 20.0, 8.0, 200.0);
        Dron emergencia = new DronEmergencia("EM02", "Rescue", 20.0, 8.0, 150.0);

        assertTrue(emergencia.calcular_costo() > carga.calcular_costo());
    }

    @Test
    @DisplayName("DronLiviano rechaza pesos mayores a 5 kg y vuelos de mas de 120 minutos")
    void liviano_valida_limites() {
        assertThrows(IllegalArgumentException.class,
                () -> new DronLiviano("X1", "x", 1.0, 10.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DronLiviano("X2", "x", 1.0, 1.0, 200.0));
    }

    @Test
    @DisplayName("DronCarga rechaza pesos de 30 kg o mas y vuelos de mas de 300 minutos")
    void carga_valida_limites() {
        assertThrows(IllegalArgumentException.class,
                () -> new DronCarga("X3", "x", 1.0, 30.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DronCarga("X4", "x", 1.0, 1.0, 400.0));
    }

    @Test
    @DisplayName("DronEmergencia rechaza pesos mayores a 15 kg y vuelos de mas de 180 minutos")
    void emergencia_valida_limites() {
        assertThrows(IllegalArgumentException.class,
                () -> new DronEmergencia("X5", "x", 1.0, 20.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DronEmergencia("X6", "x", 1.0, 1.0, 200.0));
    }
}

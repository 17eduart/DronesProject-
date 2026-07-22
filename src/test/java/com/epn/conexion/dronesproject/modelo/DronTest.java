package com.epn.conexion.dronesproject.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de negocio de la jerarquia Dron.
 * No necesitan Spring ni base de datos: son objetos puros.
 *
 * Los drones se construyen con su CAPACIDAD MAXIMA; calcularCosto recibe lo
 * que se solicita en un pedido concreto.
 */
class DronTest {

    /** Capacidades holgadas, para que los tests de costo no choquen con los limites. */
    private Dron livianoCapaz() {
        return new DronLiviano("LV01", "Maverick", 100.0, 5.0, 120.0);
    }

    private Dron cargaCapaz() {
        return new DronCarga("CG01", "Titan", 100.0, 29.0, 300.0);
    }

    private Dron emergenciaCapaz() {
        return new DronEmergencia("EM01", "Rescue", 100.0, 15.0, 180.0);
    }

    @Test
    @DisplayName("El costo de cada tipo se calcula sobre lo SOLICITADO, con su propia formula")
    void calcular_costo_por_tipo() {
        // Mismo servicio pedido a los tres: 20 km, 8 kg, 100 minutos.
        // 0.50 + (20 * 0.50) + (8 * 0.00)  -> el liviano no cobra por peso
        assertEquals(10.50, livianoCapaz().calcularCosto(20.0, 4.0, 100.0), 0.0001);
        // 6.0 + (20 * 0.70) + (8 * 1.20)
        assertEquals(29.60, cargaCapaz().calcularCosto(20.0, 8.0, 100.0), 0.0001);
        // 15.0 + (20 * 1.00) + (8 * 1.50)
        assertEquals(47.00, emergenciaCapaz().calcularCosto(20.0, 8.0, 100.0), 0.0001);
    }

    @Test
    @DisplayName("El costo depende de lo solicitado, no de la capacidad del modelo")
    void el_costo_no_usa_la_capacidad_del_modelo() {
        // Dos modelos con capacidades muy distintas pero la misma tarifa:
        // pedirles el mismo servicio debe costar lo mismo.
        Dron pequeno = new DronCarga("CG-A", "Titan", 50.0, 10.0, 200.0);
        Dron grande = new DronCarga("CG-B", "Titan XL", 500.0, 29.0, 300.0);

        assertEquals(pequeno.calcularCosto(10.0, 5.0, 60.0),
                grande.calcularCosto(10.0, 5.0, 60.0), 0.0001);
    }

    @Test
    @DisplayName("El dron de emergencia es el mas caro a igualdad de servicio")
    void emergencia_es_el_tipo_mas_caro() {
        double servicioCarga = cargaCapaz().calcularCosto(20.0, 8.0, 100.0);
        double servicioEmergencia = emergenciaCapaz().calcularCosto(20.0, 8.0, 100.0);

        assertTrue(servicioEmergencia > servicioCarga);
    }

    @Test
    @DisplayName("El desglose de componentes suma exactamente el costo total")
    void el_desglose_suma_el_total() {
        Dron carga = cargaCapaz();

        double total = carga.calcularCosto(20.0, 8.0, 100.0);
        double suma = carga.getCosto_base()
                + carga.componenteDistancia(20.0)
                + carga.componentePeso(8.0);

        assertEquals(total, suma, 0.0001);
    }

    // ---------- Capacidad excedida ----------

    @Test
    @DisplayName("Pedir mas distancia de la que alcanza el modelo lanza IllegalArgumentException")
    void distancia_excedida_falla() {
        Dron liviano = new DronLiviano("LV02", "Maverick", 30.0, 5.0, 120.0);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> liviano.calcularCosto(31.0, 1.0, 10.0));

        assertTrue(e.getMessage().contains("distancia"), e.getMessage());
        assertTrue(e.getMessage().contains("30.0"), e.getMessage());
    }

    @Test
    @DisplayName("Pedir mas peso del que carga el modelo lanza IllegalArgumentException")
    void peso_excedido_falla() {
        Dron carga = new DronCarga("CG02", "Titan", 100.0, 20.0, 300.0);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> carga.calcularCosto(10.0, 25.0, 10.0));

        assertTrue(e.getMessage().contains("peso"), e.getMessage());
        assertTrue(e.getMessage().contains("20.0"), e.getMessage());
    }

    @Test
    @DisplayName("Pedir mas minutos de los que aguanta el modelo lanza IllegalArgumentException")
    void horas_excedidas_falla() {
        Dron emergencia = new DronEmergencia("EM02", "Rescue", 100.0, 15.0, 90.0);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> emergencia.calcularCosto(10.0, 1.0, 120.0));

        assertTrue(e.getMessage().contains("vuelo"), e.getMessage());
        assertTrue(e.getMessage().contains("90.0"), e.getMessage());
    }

    @Test
    @DisplayName("Pedir exactamente la capacidad maxima es valido")
    void el_limite_exacto_es_valido() {
        Dron carga = new DronCarga("CG03", "Titan", 50.0, 20.0, 200.0);

        // 6.0 + (50 * 0.70) + (20 * 1.20)
        assertEquals(65.00, carga.calcularCosto(50.0, 20.0, 200.0), 0.0001);
    }

    // ---------- Limites del constructor (capacidades imposibles) ----------

    @Test
    @DisplayName("DronLiviano rechaza capacidades mayores a 5 kg o 120 minutos")
    void liviano_valida_limites_de_construccion() {
        assertThrows(IllegalArgumentException.class,
                () -> new DronLiviano("X1", "x", 1.0, 10.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DronLiviano("X2", "x", 1.0, 1.0, 200.0));
    }

    @Test
    @DisplayName("DronCarga rechaza capacidades de 30 kg o mas, o mas de 300 minutos")
    void carga_valida_limites_de_construccion() {
        assertThrows(IllegalArgumentException.class,
                () -> new DronCarga("X3", "x", 1.0, 30.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DronCarga("X4", "x", 1.0, 1.0, 400.0));
    }

    @Test
    @DisplayName("DronEmergencia rechaza capacidades mayores a 15 kg o 180 minutos")
    void emergencia_valida_limites_de_construccion() {
        assertThrows(IllegalArgumentException.class,
                () -> new DronEmergencia("X5", "x", 1.0, 20.0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DronEmergencia("X6", "x", 1.0, 1.0, 200.0));
    }

    @Test
    @DisplayName("getTipo devuelve el discriminador de cada subclase")
    void get_tipo_coincide_con_el_discriminador() {
        assertEquals("LIVIANO", livianoCapaz().getTipo());
        assertEquals("CARGA", cargaCapaz().getTipo());
        assertEquals("EMERGENCIA", emergenciaCapaz().getTipo());
    }
}

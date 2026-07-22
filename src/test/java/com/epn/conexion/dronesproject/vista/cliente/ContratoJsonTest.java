package com.epn.conexion.dronesproject.vista.cliente;

import com.epn.conexion.dronesproject.modelo.Dron;
import com.epn.conexion.dronesproject.modelo.DronCarga;
import com.epn.conexion.dronesproject.modelo.DronRequest;
import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.LoginResponse;
import com.epn.conexion.dronesproject.modelo.Pedido;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import com.epn.conexion.dronesproject.modelo.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el contrato JSON entre el backend y el cliente JavaFX.
 *
 * Es la unica forma de comprobar el cliente sin abrir una ventana: si el
 * servidor emite un nombre de campo y el DTO del cliente espera otro, la app
 * fallaria recien al ejecutarse. Aqui falla en el build.
 *
 * Los JSON de entrada estan escritos a mano imitando EXACTAMENTE lo que
 * responde Spring (incluidos los guiones bajos de distancia_km y las fechas en
 * ISO-8601), en vez de generarlos con el mismo mapper del cliente, que ocultaria
 * justamente las diferencias que se quieren detectar.
 */
class ContratoJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("LoginResponse se deserializa desde el JSON de POST /login")
    void login_response() {
        String json = """
                {"token":"eyJhbGciOiJIUzI1NiJ9.abc.def","username":"ana","rol":"CLIENTE"}
                """;

        LoginResponse respuesta = mapper.readValue(json, LoginResponse.class);

        assertEquals("eyJhbGciOiJIUzI1NiJ9.abc.def", respuesta.getToken());
        assertEquals("ana", respuesta.getUsername());
        assertEquals("CLIENTE", respuesta.getRol());
    }

    @Test
    @DisplayName("DronResponse mapea los nombres con guion bajo que emite la entidad Dron")
    void dron_response_nombres_del_servidor() {
        // Forma exacta que produce Jackson a partir de los getters de Dron:
        // getDistancia_km() -> "distancia_km", getTipo() -> "tipo".
        String json = """
                [{"codigo":"CG01","modelo":"Titan","distancia_km":100.0,"peso_maximo":29.0,
                  "horas_vuelo":300.0,"costo_base":6.0,"tipo":"CARGA"}]
                """;

        List<DronResponse> drones = mapper.readValue(json, new TypeReference<List<DronResponse>>() {});

        assertEquals(1, drones.size());
        DronResponse dron = drones.get(0);
        assertEquals("CG01", dron.getCodigo());
        assertEquals("Titan", dron.getModelo());
        assertEquals("CARGA", dron.getTipo());
        assertEquals(100.0, dron.getDistanciaKm(), 0.0001);
        assertEquals(29.0, dron.getPesoMaximo(), 0.0001);
        assertEquals(300.0, dron.getHorasVuelo(), 0.0001);
        assertEquals(6.0, dron.getCostoBase(), 0.0001);
    }

    @Test
    @DisplayName("Los nombres que emite el servidor son los que espera DronResponse")
    void los_nombres_del_servidor_coinciden() {
        // Se serializa una entidad real igual que haria GET /dron y se comprueba
        // que el JSON resultante trae las claves que DronResponse declara.
        Dron dron = new DronCarga("CG01", "Titan", 100.0, 29.0, 300.0);
        String json = mapper.writeValueAsString(dron);

        assertTrue(json.contains("\"distancia_km\""), json);
        assertTrue(json.contains("\"peso_maximo\""), json);
        assertTrue(json.contains("\"horas_vuelo\""), json);
        assertTrue(json.contains("\"costo_base\""), json);
        assertTrue(json.contains("\"tipo\""), json);

        // Y que ese mismo JSON se puede leer como DronResponse.
        DronResponse leido = mapper.readValue(json, DronResponse.class);
        assertEquals("CARGA", leido.getTipo());
        assertEquals(29.0, leido.getPesoMaximo(), 0.0001);
    }

    @Test
    @DisplayName("PedidoResponse se deserializa, incluida la fecha en ISO-8601")
    void pedido_response_con_fecha() {
        String json = """
                {"id":7,"username":"ana","codigoDron":"CG01","modeloDron":"Titan","tipoDron":"CARGA",
                 "distanciaSolicitada":20.0,"pesoSolicitado":8.0,"horasSolicitadas":100.0,
                 "costoTotal":29.6,"fechaCreacion":"2026-07-22T10:15:30"}
                """;

        PedidoResponse pedido = mapper.readValue(json, PedidoResponse.class);

        assertEquals(7L, pedido.getId());
        assertEquals("ana", pedido.getUsername());
        assertEquals("CARGA", pedido.getTipoDron());
        assertEquals(29.6, pedido.getCostoTotal(), 0.0001);
        assertEquals(LocalDateTime.of(2026, 7, 22, 10, 15, 30), pedido.getFechaCreacion());
    }

    @Test
    @DisplayName("FacturaResponse se deserializa y su desglose suma el total")
    void factura_response() {
        String json = """
                {"numeroFactura":"FAC-000007","fechaEmision":"2026-07-22T10:15:30",
                 "nombreCliente":"ana","detalleDron":"Titan (CARGA)",
                 "costoBase":6.0,"componenteDistancia":14.0,"componentePeso":9.6,"total":29.6}
                """;

        FacturaResponse factura = mapper.readValue(json, FacturaResponse.class);

        assertEquals("FAC-000007", factura.getNumeroFactura());
        assertEquals("Titan (CARGA)", factura.getDetalleDron());
        assertNotNull(factura.getFechaEmision());
        assertEquals(factura.getTotal(),
                factura.getCostoBase() + factura.getComponenteDistancia() + factura.getComponentePeso(),
                0.0001);
    }

    @Test
    @DisplayName("Ida y vuelta con los DTO reales del servidor")
    void ida_y_vuelta_con_los_dto_del_servidor() {
        Usuario ana = new Usuario("ana", "hash", Usuario.ROL_CLIENTE);
        Dron dron = new DronCarga("CG01", "Titan", 100.0, 29.0, 300.0);
        Pedido pedido = new Pedido(ana, dron, 20.0, 8.0, 100.0,
                dron.calcularCosto(20.0, 8.0, 100.0), LocalDateTime.now());
        pedido.setId(7L);

        PedidoResponse ida = PedidoResponse.desde(pedido);
        PedidoResponse vuelta = mapper.readValue(mapper.writeValueAsString(ida), PedidoResponse.class);

        assertEquals(ida.getId(), vuelta.getId());
        assertEquals(ida.getCostoTotal(), vuelta.getCostoTotal(), 0.0001);
        assertEquals(ida.getTipoDron(), vuelta.getTipoDron());

        FacturaResponse facturaIda = FacturaResponse.desde(pedido);
        FacturaResponse facturaVuelta =
                mapper.readValue(mapper.writeValueAsString(facturaIda), FacturaResponse.class);

        assertEquals("FAC-000007", facturaVuelta.getNumeroFactura());
        assertEquals(facturaIda.getTotal(), facturaVuelta.getTotal(), 0.0001);
    }

    @Test
    @DisplayName("DronRequest se serializa con los nombres que espera el backend")
    void dron_request_se_serializa_con_guion_bajo() {
        // Lo que manda la pantalla de administracion en POST /dron/{tipo}.
        // Si estos nombres no coincidieran, el backend recibiria nulls y
        // guardaria un dron sin capacidades en vez de fallar.
        DronRequest solicitud = new DronRequest();
        solicitud.setCodigo("CG09");
        solicitud.setModelo("Titan II");
        solicitud.setDistancia_km(120.0);
        solicitud.setPeso_maximo(25.0);
        solicitud.setHoras_vuelo(280.0);

        String json = mapper.writeValueAsString(solicitud);

        assertTrue(json.contains("\"codigo\":\"CG09\""), json);
        assertTrue(json.contains("\"modelo\":\"Titan II\""), json);
        assertTrue(json.contains("\"distancia_km\":120.0"), json);
        assertTrue(json.contains("\"peso_maximo\":25.0"), json);
        assertTrue(json.contains("\"horas_vuelo\":280.0"), json);

        // Y el backend lo puede volver a leer como DronRequest.
        DronRequest leido = mapper.readValue(json, DronRequest.class);
        assertEquals("CG09", leido.getCodigo());
        assertEquals(25.0, leido.getPeso_maximo(), 0.0001);
    }

    @Test
    @DisplayName("El mensaje de error sale del campo Mensaje de GlobalException")
    void extrae_el_mensaje_de_error_del_backend() {
        String json = """
                {"Status":400,"Error":"Bad Request",
                 "Mensaje":"El peso solicitado (50.0 kg) supera la capacidad del modelo Titan, cuyo maximo es 29.0 kg"}
                """;

        String mensaje = ApiException.extraerMensaje(400, json);

        assertTrue(mensaje.startsWith("El peso solicitado"), mensaje);
        assertTrue(mensaje.contains("29.0 kg"), mensaje);
    }

    @Test
    @DisplayName("Si el cuerpo del error no tiene formato conocido, cae a un mensaje generico")
    void mensaje_generico_si_el_cuerpo_no_es_json() {
        String mensaje = ApiException.extraerMensaje(403, "<html>Forbidden</html>");

        assertTrue(mensaje.contains("permisos"), mensaje);
    }
}

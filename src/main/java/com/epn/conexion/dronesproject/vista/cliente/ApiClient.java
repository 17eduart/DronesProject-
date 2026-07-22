package com.epn.conexion.dronesproject.vista.cliente;

import com.epn.conexion.dronesproject.modelo.DronRequest;
import com.epn.conexion.dronesproject.modelo.FacturaResponse;
import com.epn.conexion.dronesproject.modelo.LoginResponse;
import com.epn.conexion.dronesproject.modelo.PedidoRequest;
import com.epn.conexion.dronesproject.modelo.PedidoResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP de la API, con java.net.http (JDK), sin librerias externas.
 *
 * Adjunta el header Authorization automaticamente en todas las llamadas salvo
 * login y registro, que son las dos rutas publicas del backend.
 *
 * OJO - las llamadas son SINCRONAS: bloquean el hilo desde el que se invocan.
 * Como los controllers las llaman desde el hilo de JavaFX, la ventana se queda
 * congelada mientras dura la peticion. Para una demo local contra localhost es
 * imperceptible, y por eso se prefirio la version simple; los timeouts de
 * abajo garantizan que el peor caso sean unos segundos y no un cuelgue
 * indefinido. Si la app llegara a hablar con un servidor remoto, habria que
 * mover estas llamadas a un javafx.concurrent.Task.
 */
public class ApiClient {

    /** Se puede apuntar a otro host con -Dapi.base.url=http://... */
    private static final String BASE_URL =
            System.getProperty("api.base.url", "http://localhost:8080");

    private static final ApiClient INSTANCIA = new ApiClient();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private ApiClient() {}

    public static ApiClient getInstancia() {
        return INSTANCIA;
    }

    // ------------------------------------------------------------------
    // Endpoints
    // ------------------------------------------------------------------

    public LoginResponse login(String username, String password) throws ApiException {
        String cuerpo = escribirJson(new Credenciales(username, password));
        HttpResponse<String> respuesta = enviar(peticion("/login").POST(cuerpoJson(cuerpo)), false);
        return leerJson(respuesta.body(), LoginResponse.class);
    }

    public void registrar(String username, String password) throws ApiException {
        String cuerpo = escribirJson(new Credenciales(username, password));
        enviar(peticion("/registro").POST(cuerpoJson(cuerpo)), false);
    }

    public List<DronResponse> obtenerDrones() throws ApiException {
        HttpResponse<String> respuesta = enviar(peticion("/dron").GET(), true);
        return leerJson(respuesta.body(), new TypeReference<List<DronResponse>>() {});
    }

    // --- Catalogo de drones (solo ADMINISTRADOR; el backend responde 403 al resto) ---

    public DronResponse crearDron(String tipo, DronRequest datos) throws ApiException {
        String cuerpo = escribirJson(datos);
        HttpResponse<String> respuesta =
                enviar(peticion("/dron/" + segmento(tipo)).POST(cuerpoJson(cuerpo)), true);
        return leerJson(respuesta.body(), DronResponse.class);
    }

    public DronResponse actualizarDron(String tipo, String codigo, DronRequest datos) throws ApiException {
        String cuerpo = escribirJson(datos);
        HttpResponse<String> respuesta =
                enviar(peticion("/dron/" + segmento(tipo) + "/" + segmento(codigo)).PUT(cuerpoJson(cuerpo)), true);
        return leerJson(respuesta.body(), DronResponse.class);
    }

    public void eliminarDron(String codigo) throws ApiException {
        // Responde 204 sin cuerpo; no hay nada que deserializar.
        enviar(peticion("/dron/" + segmento(codigo)).DELETE(), true);
    }

    public PedidoResponse crearPedido(PedidoRequest datos) throws ApiException {
        String cuerpo = escribirJson(datos);
        HttpResponse<String> respuesta = enviar(peticion("/pedidos").POST(cuerpoJson(cuerpo)), true);
        return leerJson(respuesta.body(), PedidoResponse.class);
    }

    public FacturaResponse obtenerFactura(Long pedidoId) throws ApiException {
        HttpResponse<String> respuesta = enviar(peticion("/pedidos/" + pedidoId + "/factura").GET(), true);
        return leerJson(respuesta.body(), FacturaResponse.class);
    }

    // ------------------------------------------------------------------
    // Plomeria
    // ------------------------------------------------------------------

    private HttpRequest.Builder peticion(String ruta) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ruta))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private HttpRequest.BodyPublisher cuerpoJson(String json) {
        return HttpRequest.BodyPublishers.ofString(json);
    }

    /**
     * Codifica un valor que va dentro de la ruta (por ejemplo el codigo del
     * dron, que lo escribe el usuario). Sin esto, un codigo con un espacio o
     * una barra haria que URI.create lanzara IllegalArgumentException, que al
     * no ser ApiException se escaparia del controller y tumbaria la accion.
     *
     * URLEncoder codifica para formularios, donde el espacio es "+"; en una
     * ruta el espacio debe ser %20, de ahi el reemplazo.
     */
    private String segmento(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    /**
     * Ejecuta la peticion y traduce cualquier respuesta de error a ApiException.
     *
     * @param conToken si true, adjunta el Bearer de la sesion actual
     */
    private HttpResponse<String> enviar(HttpRequest.Builder builder, boolean conToken) throws ApiException {
        if (conToken) {
            SessionManager sesion = SessionManager.getInstancia();
            if (!sesion.estaAutenticado()) {
                throw new ApiException(401, "No hay sesion activa. Vuelva a iniciar sesion.");
            }
            builder.header("Authorization", "Bearer " + sesion.getToken());
        }

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException("No se pudo conectar con el servidor. "
                    + "¿Esta levantado en " + BASE_URL + "?", e);
        } catch (InterruptedException e) {
            // Restaurar la bandera: tragarse la interrupcion deja el hilo en un
            // estado inconsistente para quien lo gestione mas arriba.
            Thread.currentThread().interrupt();
            throw new ApiException("La peticion fue interrumpida.", e);
        }

        if (respuesta.statusCode() >= 400) {
            throw ApiException.desdeRespuesta(respuesta.statusCode(), respuesta.body());
        }
        return respuesta;
    }

    private String escribirJson(Object valor) throws ApiException {
        try {
            return mapper.writeValueAsString(valor);
        } catch (RuntimeException e) {
            throw new ApiException("No se pudo preparar la peticion.", e);
        }
    }

    private <T> T leerJson(String json, Class<T> tipo) throws ApiException {
        try {
            return mapper.readValue(json, tipo);
        } catch (RuntimeException e) {
            throw new ApiException("Respuesta inesperada del servidor.", e);
        }
    }

    private <T> T leerJson(String json, TypeReference<T> tipo) throws ApiException {
        try {
            return mapper.readValue(json, tipo);
        } catch (RuntimeException e) {
            throw new ApiException("Respuesta inesperada del servidor.", e);
        }
    }

    /**
     * Body de /login y /registro. Coincide con UsuarioRequest del backend, pero
     * se declara aqui para no arrastrar el DTO del servidor solo por dos
     * campos; ademas deja explicito que el cliente NUNCA manda un rol.
     */
    private record Credenciales(String username, String password) {}
}

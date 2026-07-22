package com.epn.conexion.dronesproject.vista.cliente;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Error devuelto por la API o fallo al comunicarse con ella.
 *
 * El mensaje ya viene listo para mostrarse al usuario: cuando el backend
 * responde con el JSON de GlobalException, se extrae el campo "Mensaje" y se
 * pone aqui, para que los controllers puedan hacer
 * lbl_error.setText(e.getMessage()) sin traducir nada.
 */
public class ApiException extends Exception {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final int codigoHttp;

    public ApiException(int codigoHttp, String mensaje) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    public ApiException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoHttp = 0;
    }

    /** Codigo HTTP de la respuesta, o 0 si el fallo fue de red. */
    public int getCodigoHttp() {
        return codigoHttp;
    }

    /**
     * Construye la excepcion a partir de una respuesta de error del backend.
     *
     * Vive aqui y no en ApiClient porque es logica pura sobre (codigo, cuerpo):
     * no necesita red, y asi se puede probar sin abrir un HttpClient.
     */
    public static ApiException desdeRespuesta(int codigo, String cuerpo) {
        return new ApiException(codigo, extraerMensaje(codigo, cuerpo));
    }

    /**
     * Saca el texto del campo "Mensaje" del JSON de error de GlobalException.
     * Si la respuesta no tiene ese formato (por ejemplo una pagina de error del
     * contenedor), cae a un mensaje generico segun el codigo HTTP.
     */
    static String extraerMensaje(int codigo, String cuerpo) {
        if (cuerpo != null && !cuerpo.isBlank()) {
            try {
                JsonNode raiz = MAPPER.readTree(cuerpo);
                JsonNode mensaje = raiz.get("Mensaje");
                if (mensaje != null && !mensaje.isNull()) {
                    return mensaje.asString();
                }
            } catch (RuntimeException e) {
                // El cuerpo no era JSON; seguimos al mensaje generico.
            }
        }
        return switch (codigo) {
            case 401 -> "Credenciales invalidas o sesion expirada.";
            case 403 -> "No tiene permisos para esta operacion.";
            case 404 -> "El recurso solicitado no existe.";
            default -> "Error del servidor (HTTP " + codigo + ").";
        };
    }
}

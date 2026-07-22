package com.epn.conexion.dronesproject.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hace que los errores de seguridad salgan con el mismo formato JSON que ya
 * usa GlobalException, en vez de la pagina de error por defecto de Spring.
 *
 * Estos dos casos no pasan por @RestControllerAdvice: ocurren en la cadena de
 * filtros, antes de que exista un controlador al que aplicarle el advice. Por
 * eso hay que resolverlos aqui y no en GlobalException.
 *
 * Implementa las dos interfaces en una sola clase a proposito, para que el
 * formato de 401 y 403 no se pueda desincronizar.
 */
@Component
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ManejadorErroresSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Falta el token o no es valido: 401. */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED,
                "Se requiere un token valido en el header Authorization");
    }

    /** Hay token valido pero el rol no alcanza: 403. */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operacion");
    }

    private void escribir(HttpServletResponse response, HttpStatus estado, String mensaje) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Mismas claves que GlobalException: Status, Error, Mensaje.
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("Status", estado.value());
        error.put("Error", estado.getReasonPhrase());
        error.put("Mensaje", mensaje);

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}

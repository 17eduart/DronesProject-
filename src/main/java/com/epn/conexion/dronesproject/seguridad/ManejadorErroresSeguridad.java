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

@Component
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ManejadorErroresSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED,
                "Se requiere un token valido en el header Authorization");
    }

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

package com.epn.conexion.dronesproject.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta de POST /login.
 *
 * El servidor la serializa y el cliente JavaFX la deserializa, por eso el
 * constructor lleva @JsonCreator: sin el, Jackson no sabe como construir una
 * clase inmutable sin constructor vacio ni setters.
 */
public class LoginResponse {
    private final String token;
    private final String username;
    private final String rol;

    @JsonCreator
    public LoginResponse(@JsonProperty("token") String token,
                         @JsonProperty("username") String username,
                         @JsonProperty("rol") String rol) {
        this.token = token;
        this.username = username;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }
}

package com.epn.conexion.dronesproject.modelo;

/**
 * Respuesta de POST /login. Solo se serializa hacia el cliente, nunca se
 * deserializa, por eso no necesita setters ni constructor vacio.
 */
public class LoginResponse {
    private final String token;
    private final String username;
    private final String rol;

    public LoginResponse(String token, String username, String rol) {
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

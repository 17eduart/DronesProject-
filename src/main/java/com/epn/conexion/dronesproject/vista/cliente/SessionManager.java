package com.epn.conexion.dronesproject.vista.cliente;

import com.epn.conexion.dronesproject.modelo.Usuario;

/**
 * Sesion del usuario en memoria: vive mientras la aplicacion este abierta y se
 * pierde al cerrarla. No persiste el token en disco a proposito; guardarlo en
 * un archivo seria darle a cualquiera que lea el disco la capacidad de actuar
 * como este usuario hasta que el token expire.
 */
public class SessionManager {

    private static final SessionManager INSTANCIA = new SessionManager();

    private String token;
    private String username;
    private String rol;

    private SessionManager() {}

    public static SessionManager getInstancia() {
        return INSTANCIA;
    }

    public void guardarSesion(String token, String username, String rol) {
        this.token = token;
        this.username = username;
        this.rol = rol;
    }

    public void cerrarSesion() {
        this.token = null;
        this.username = null;
        this.rol = null;
    }

    public boolean estaAutenticado() {
        return token != null && !token.isBlank();
    }

    public boolean esAdministrador() {
        return Usuario.ROL_ADMINISTRADOR.equals(rol);
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

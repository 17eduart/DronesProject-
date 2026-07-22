package com.epn.conexion.dronesproject.controlador;

import com.epn.conexion.dronesproject.modelo.LoginResponse;
import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRequest;
import com.epn.conexion.dronesproject.seguridad.JwtService;
import com.epn.conexion.dronesproject.servicio.UsuarioServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class UsuarioController {
    private final UsuarioServicio usuarioServicio;
    private final JwtService jwtService;

    public UsuarioController(UsuarioServicio usuarioServicio, JwtService jwtService) {
        this.usuarioServicio = usuarioServicio;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registrar(@RequestBody UsuarioRequest datos){
        // El rol va fijo a CLIENTE y el DTO no expone el campo: si se leyera del
        // body, cualquiera podria auto-registrarse como ADMINISTRADOR.
        usuarioServicio.registrar(datos.getUsername(), datos.getPassword(), Usuario.ROL_CLIENTE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario Registrado"+datos.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UsuarioRequest datos){
        return usuarioServicio.autenticar(datos.getUsername(), datos.getPassword())
                .map(usuario -> ResponseEntity.ok(new LoginResponse(
                        jwtService.generarToken(usuario.getUsername(), usuario.getRol()),
                        usuario.getUsername(),
                        usuario.getRol())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/admin/usuarios")
    public ResponseEntity<String> crearAdministrador(@RequestBody UsuarioRequest datos){
        usuarioServicio.registrar(datos.getUsername(), datos.getPassword(), Usuario.ROL_ADMINISTRADOR);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Administrador Registrado"+datos.getUsername());
    }
}

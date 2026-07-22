package com.epn.conexion.dronesproject.controlador;

import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class UsuarioController {
    @Autowired
    private UsuarioServicio usuarioServicio;

    @PostMapping("/registro")
    public ResponseEntity<String> registrar(@RequestBody Usuario datos){
        usuarioServicio.registrar(datos.getUsername(), datos.getPassword(), datos.getRol());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario Registrado"+datos.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario datos){
        boolean valido = usuarioServicio.login(datos.getUsername(), datos.getPassword());
        if (valido){
            return ResponseEntity.ok("Login Correcto");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Invalidas");
    }
}

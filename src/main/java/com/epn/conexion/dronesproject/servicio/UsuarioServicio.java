package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServicio {
    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Usuario registrar(String username, String passwordPlano, String rol){
        if (usuarioRepository.existsByUsername(username)){
            throw new IllegalArgumentException("El usuario ya existe");
        }
        String hash = encoder.encode(passwordPlano);
        Usuario usuario = new Usuario(username, hash, rol);
        return usuarioRepository.save(usuario);
    }

    public boolean login(String username, String passwordPlano){
        Optional<Usuario> encontrado = usuarioRepository.findByUsername(username);
        if (encontrado.isEmpty()){
            return false;
        }
        Usuario usuario = encontrado.get();
        return encoder.matches(passwordPlano, usuario.getPassword());
    }
}

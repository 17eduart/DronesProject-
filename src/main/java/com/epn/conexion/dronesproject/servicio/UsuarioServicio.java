package com.epn.conexion.dronesproject.servicio;

import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServicio {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;

    public UsuarioServicio(UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
    }

    public Usuario registrar(String username, String passwordPlano, String rol){
        if (usuarioRepository.existsByUsername(username)){
            throw new IllegalArgumentException("El usuario ya existe");
        }
        String hash = encoder.encode(passwordPlano);
        Usuario usuario = new Usuario(username, hash, rol);
        return usuarioRepository.save(usuario);
    }

    /**
     * Verifica las credenciales y devuelve el usuario si son correctas.
     * El controlador lo necesita para poder meter el rol dentro del token.
     */
    public Optional<Usuario> autenticar(String username, String passwordPlano){
        return usuarioRepository.findByUsername(username)
                .filter(usuario -> encoder.matches(passwordPlano, usuario.getPassword()));
    }

    public boolean login(String username, String passwordPlano){
        return autenticar(username, passwordPlano).isPresent();
    }
}

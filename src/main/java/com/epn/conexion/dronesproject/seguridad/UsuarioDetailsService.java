package com.epn.conexion.dronesproject.seguridad;

import com.epn.conexion.dronesproject.modelo.Usuario;
import com.epn.conexion.dronesproject.modelo.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Carga el Usuario de la base y lo traduce al modelo que entiende Spring
 * Security.
 *
 * Ojo con la convencion de nombres: hasRole("ADMINISTRADOR") busca en realidad
 * la authority "ROLE_ADMINISTRADOR". El prefijo lo pone quien construye la
 * authority, no Spring, asi que hay que anteponerlo aqui.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    public static final String PREFIJO_ROL = "ROLE_";

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + username));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(PREFIJO_ROL + usuario.getRol())))
                .build();
    }
}

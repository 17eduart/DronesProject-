package com.epn.conexion.dronesproject.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee el header Authorization, valida el token y autentica la peticion.
 *
 * Este filtro nunca rechaza nada: si no hay token o el token es invalido deja
 * la peticion sin autenticar y sigue la cadena. Quien decide si la ruta exige
 * autenticacion es el SecurityFilterChain, y quien responde el 401/403 son el
 * AuthenticationEntryPoint y el AccessDeniedHandler. Asi las rutas publicas
 * (/registro, /login) siguen funcionando aunque llegue un token basura.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extraerToken(request);

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.esValido(token)) {
            autenticar(request, token);
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIJO_BEARER)) {
            return null;
        }
        return header.substring(PREFIJO_BEARER.length());
    }

    private void autenticar(HttpServletRequest request, String token) {
        try {
            // Las authorities salen de la base, no del claim "rol" del token.
            // Si el token trae un rol y la base dice otro, manda la base: un
            // token viejo de un usuario degradado no debe seguir dando
            // permisos de ADMINISTRADOR hasta que expire.
            UserDetails usuario = userDetailsService.loadUserByUsername(jwtService.extraerUsername(token));

            UsernamePasswordAuthenticationToken autenticacion =
                    UsernamePasswordAuthenticationToken.authenticated(
                            usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
        } catch (UsernameNotFoundException e) {
            // Token con firma valida pero de un usuario que ya no existe:
            // se queda sin autenticar y la cadena respondera 401.
            SecurityContextHolder.clearContext();
        }
    }
}

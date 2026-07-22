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

            UserDetails usuario = userDetailsService.loadUserByUsername(jwtService.extraerUsername(token));

            UsernamePasswordAuthenticationToken autenticacion =
                    UsernamePasswordAuthenticationToken.authenticated(
                            usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
        }
    }
}

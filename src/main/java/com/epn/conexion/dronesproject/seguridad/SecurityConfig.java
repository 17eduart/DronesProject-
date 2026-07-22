package com.epn.conexion.dronesproject.seguridad;

import com.epn.conexion.dronesproject.modelo.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ManejadorErroresSeguridad manejadorErrores;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, ManejadorErroresSeguridad manejadorErrores) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.manejadorErrores = manejadorErrores;
    }

    /**
     * Encoder centralizado. Estaba instanciado dentro de UsuarioServicio con
     * new; como @Bean queda un unico encoder compartido por toda la app.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS para el frontend React servido por Vite en otro puerto.
     *
     * El navegador trata http://localhost:5173 y http://localhost:8080 como
     * origenes distintos, asi que sin esto bloquea las respuestas y ni siquiera
     * deja pasar el preflight OPTIONS. La app JavaFX no lo necesitaba porque no
     * es un navegador y no aplica la politica de mismo origen.
     *
     * El origen esta acotado a la URL de desarrollo: no se usa "*" porque
     * dejaria que cualquier pagina web llamara a esta API con el token del
     * usuario.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:5173"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Sin cookies: el token viaja en el header Authorization, asi que no
        // hace falta permitir credenciales.
        configuracion.setAllowCredentials(false);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Toma el bean corsConfigurationSource de arriba. El CorsFilter
                // corre antes que el filtro JWT, de modo que el preflight
                // OPTIONS se responde sin exigir token.
                .cors(Customizer.withDefaults())
                // Sin cookies de sesion no hay vector CSRF que proteger: el
                // navegador no adjunta el header Authorization solo.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publico: hay que poder registrarse y pedir token.
                        .requestMatchers(HttpMethod.POST, "/registro", "/login").permitAll()

                        // Solo ADMINISTRADOR: gestion del catalogo y alta de admins.
                        .requestMatchers(HttpMethod.POST, "/dron/*").hasRole(Usuario.ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.PUT, "/dron/*/*").hasRole(Usuario.ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.DELETE, "/dron/*").hasRole(Usuario.ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.POST, "/admin/usuarios").hasRole(Usuario.ROL_ADMINISTRADOR)

                        // Consultar el catalogo: cualquiera autenticado.
                        .requestMatchers(HttpMethod.GET, "/dron", "/dron/*").authenticated()

                        // Pedidos: CLIENTE y ADMINISTRADOR por igual. La regla
                        // de "solo puedo ver lo mio" no cabe aqui porque depende
                        // del dueño del pedido, que la cadena de filtros no
                        // conoce sin cargarlo; se resuelve en PedidoController.
                        .requestMatchers("/pedidos/**").authenticated()

                        // Por defecto se exige autenticacion: una ruta nueva
                        // queda protegida hasta que alguien decida lo contrario.
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(manejadorErrores)
                        .accessDeniedHandler(manejadorErrores))
                // Antes del filtro de usuario/contrasena: para cuando la cadena
                // llegue a decidir autorizacion, el token ya fue procesado.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

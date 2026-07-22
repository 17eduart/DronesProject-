package com.epn.conexion.dronesproject.seguridad;

import com.epn.conexion.dronesproject.modelo.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
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

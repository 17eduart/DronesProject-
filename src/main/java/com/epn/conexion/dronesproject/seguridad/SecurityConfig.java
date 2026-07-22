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


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:5173"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setAllowCredentials(false);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/registro", "/login").permitAll()

                        .requestMatchers(HttpMethod.POST, "/dron/*").hasRole(Usuario.ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.PUT, "/dron/*/*").hasRole(Usuario.ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.DELETE, "/dron/*").hasRole(Usuario.ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.POST, "/admin/usuarios").hasRole(Usuario.ROL_ADMINISTRADOR)

                        .requestMatchers(HttpMethod.GET, "/dron", "/dron/*").authenticated()

                        .requestMatchers("/pedidos/**").authenticated()

                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(manejadorErrores)
                        .accessDeniedHandler(manejadorErrores))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

package com.softwells.fanops.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <-- 1. Habilitamos la seguridad a nivel de método
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        // Definir las reglas de autorización para las peticiones HTTP
        .authorizeHttpRequests(authz -> authz

            // Permitir el acceso sin autenticación a estas rutas
            .requestMatchers(
                "/",                  // Permitir acceso a la raíz
                "/index.html",        // Archivos estáticos
                "/media/**",          // Permitir acceso a la carpeta de media (fuentes, etc.)
                "/assets/**",         // Carpeta de assets del frontend
                "/auth/**",
                "/api/eventos/*/inscripcion-publica", // Inscripción pública de no socios
                "/api/eventos/*/info-publica",        // Info pública del evento para el formulario
                "/v2/api-docs",
                "/management/**",     // Permitir acceso a los endpoints de Actuator
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui/**",
                "/webjars/**",
                "/swagger-ui.html",
                "/*.*"
            ).permitAll()
            // Ahora, cualquier otra petición solo necesita estar autenticada. La lógica de roles la movemos al controlador.
            .anyRequest().authenticated()
        )
        // Configurar la gestión de sesiones como STATELESS (una sola vez)
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    // Nota: Para producción, es mejor restringir los orígenes.
    // Ejemplo: List.of("https://tu-dominio.com")
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*")); // Usar patterns en lugar de origins con credenciales
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}

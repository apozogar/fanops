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
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
        /*
         * Reglas de autorización.
         *
         * El reparto se apoya en el mismo invariante que SpaForwardingController: todo lo que
         * cuelga de /api es API y todo lo demás es el frontend (index.html, los bundles y los
         * assets). Por eso la regla general es al revés que antes: /api/** exige autenticación
         * salvo lo explícitamente público, y el resto se sirve sin autenticar.
         *
         * El cambio es necesario, no una relajación: al quitar el enrutado por hash, una ruta
         * como /mi-pena/auth/login llega al servidor como una petición real, y con
         * anyRequest().authenticated() devolvía 401 en lugar de servir el frontend. Lo que se
         * abre no es información: son ficheros estáticos que cualquiera puede descargar de todas
         * formas, y el HTML del frontend, que sin un token válido no puede leer ningún dato.
         *
         * IMPORTANTE: si algún día se añade un controlador fuera de /api, hay que protegerlo
         * aquí explícitamente; si no, quedará público al caer en la regla final.
         */
        .authorizeHttpRequests(authz -> authz

            // --- API pública (sin token) ---
            .requestMatchers(
                "/api/auth/**",                       // Login, registro, recuperación de contraseña
                "/api/pena/publica/**",               // Identidad de la peña por su dominio, para el login
                "/api/eventos/*/inscripcion-publica", // Inscripción pública de no socios
                "/api/eventos/*/info-publica"         // Info pública del evento para el formulario
            ).permitAll()

            // --- Resto de la API: hace falta token. El detalle de roles va en cada controlador. ---
            .requestMatchers("/api/**").authenticated()

            // --- Frontend, ficheros estáticos, Actuator y Swagger ---
            .anyRequest().permitAll()
        )
        // Configurar la gestión de sesiones como STATELESS (una sola vez)
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  /**
   * Jerarquía de roles: un superadmin puede hacer todo lo que puede un admin, y un admin todo
   * lo que puede un usuario.
   *
   * Es lo que permite al superadmin gestionar la peña que tiene seleccionada sin duplicar cada
   * anotación con hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERADMIN'): todos los hasRole('ADMIN') /
   * hasAuthority('ROLE_ADMIN') que ya existen le aceptan automáticamente, incluidos los que es
   * fácil pasar por alto (roles, cobros, eventos).
   *
   * Sigue siendo seguro porque el alcance no lo da el rol sino la peña de trabajo: todas las
   * consultas se filtran por la peña que resuelve UsuarioService, que para el superadmin es la
   * que haya elegido en el selector de la cabecera.
   */
  @Bean
  public static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("SUPERADMIN").implies("ADMIN")
        .role("ADMIN").implies("USER")
        .build();
  }

  /**
   * Registra la jerarquía en la seguridad a nivel de método. Sin esto solo aplicaría a las
   * reglas de HttpSecurity y no a las anotaciones @PreAuthorize, que es justamente donde vive
   * toda la autorización de esta aplicación.
   */
  @Bean
  public static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    // Nota: Para producción, es mejor restringir los orígenes.
    // Ejemplo: List.of("https://tu-dominio.com")
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*")); // Usar patterns en lugar de origins con credenciales
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", PenaContextFilter.HEADER_NAME));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}

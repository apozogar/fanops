package com.softwells.fanops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.softwells.fanops.security.JwtService;
import com.softwells.fanops.security.SecurityConfig;
import com.softwells.fanops.service.SocioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprueba que la jerarquía de roles está realmente conectada a la seguridad por anotaciones.
 *
 * El test unitario de la jerarquía verifica que SUPERADMIN implica ADMIN, pero eso no sirve de
 * nada si @PreAuthorize no la consulta. Aquí se ejercita un endpoint anotado con
 * hasAuthority('ROLE_ADMIN') y se comprueba que un superadmin pasa: si alguien quitase el
 * MethodSecurityExpressionHandler, este test fallaría en lugar de dejar al superadmin fuera de
 * la gestión de forma silenciosa.
 */
@WebMvcTest(controllers = SocioController.class)
@Import(SocioControllerSecurityTest.MethodSecurityTestConfig.class)
class SocioControllerSecurityTest {

  /**
   * Reutiliza las factorías reales de SecurityConfig, de modo que un cambio en la jerarquía de
   * producción se refleje aquí. No se importa SecurityConfig completa porque arrastraría la
   * cadena de filtros y el JwtAuthFilter, ajenos a lo que se quiere verificar.
   */
  @TestConfiguration
  @EnableMethodSecurity
  static class MethodSecurityTestConfig {

    @Bean
    static RoleHierarchy roleHierarchy() {
      return SecurityConfig.roleHierarchy();
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
      return SecurityConfig.methodSecurityExpressionHandler(roleHierarchy);
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SocioService socioService;

  /*
   * @WebMvcTest incluye los beans de tipo Filter, así que crea el JwtAuthFilter real y hay que
   * satisfacer sus dependencias. No pasa nada por dejarlo activo: sin cabecera Authorization
   * simplemente delega en la cadena sin tocar el SecurityContext que monta @WithMockUser.
   */
  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @Test
  @DisplayName("un admin accede a las estadísticas de socios")
  @WithMockUser(authorities = "ROLE_ADMIN")
  void adminAccedeAEstadisticas() throws Exception {
    mockMvc.perform(get("/api/socios/estadisticas")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("un superadmin accede a las estadísticas de socios por jerarquía de roles")
  @WithMockUser(authorities = "ROLE_SUPERADMIN")
  void superAdminAccedeAEstadisticasPorJerarquia() throws Exception {
    mockMvc.perform(get("/api/socios/estadisticas")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("un socio sin permisos de gestión recibe 403, no acceso")
  @WithMockUser(authorities = "ROLE_USER")
  void socioNoAccedeAEstadisticas() throws Exception {
    mockMvc.perform(get("/api/socios/estadisticas")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("sin autenticar no se accede a las estadísticas")
  @WithAnonymousUser
  void anonimoNoAccedeAEstadisticas() throws Exception {
    mockMvc.perform(get("/api/socios/estadisticas"))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 401 && status != 403) {
            throw new AssertionError("Se esperaba 401 o 403 para un anónimo, y fue " + status);
          }
        });
  }
}

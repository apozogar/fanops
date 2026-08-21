package com.softwells.fanops.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * La jerarquía de roles es la que permite al superadmin gestionar la peña que tiene
 * seleccionada sin duplicar cada @PreAuthorize. Si se rompiera, el superadmin perdería el
 * acceso a socios, cuotas, cobros y eventos de forma silenciosa, así que se fija aquí.
 */
class RoleHierarchyTest {

  private final RoleHierarchy hierarchy = SecurityConfig.roleHierarchy();

  private List<String> authoritiesFor(String role) {
    return hierarchy.getReachableGrantedAuthorities(List.of(new SimpleGrantedAuthority(role)))
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
  }

  @Test
  @DisplayName("el superadmin hereda los permisos de admin y de usuario")
  void superAdminImplicaAdminYUsuario() {
    assertThat(authoritiesFor("ROLE_SUPERADMIN"))
        .containsExactlyInAnyOrder("ROLE_SUPERADMIN", "ROLE_ADMIN", "ROLE_USER");
  }

  @Test
  @DisplayName("el admin hereda los permisos de usuario")
  void adminImplicaUsuario() {
    assertThat(authoritiesFor("ROLE_ADMIN"))
        .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
  }

  @Test
  @DisplayName("un admin no escala a superadmin: la gestión de peñas le sigue estando vetada")
  void adminNoEscalaASuperAdmin() {
    assertThat(authoritiesFor("ROLE_ADMIN")).doesNotContain("ROLE_SUPERADMIN");
  }

  @Test
  @DisplayName("un socio no hereda ningún permiso de gestión")
  void usuarioNoHeredaGestion() {
    assertThat(authoritiesFor("ROLE_USER"))
        .containsExactly("ROLE_USER");
  }
}

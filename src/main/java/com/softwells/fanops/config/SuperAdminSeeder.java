package com.softwells.fanops.config;

import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.RoleRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer usuario superadmin en el arranque si todavía no existe ningún usuario con
 * ROLE_SUPERADMIN. El rol en sí lo siembra Liquibase (ver
 * db/changelog/changes/001-roles-table-and-seed.xml); este runner solo se preocupa de que exista
 * al menos una cuenta capaz de usarlo.
 *
 * <p>Es idempotente: en cada arranque comprueba si ya hay un superadmin y, si lo hay, no hace
 * nada. Pensado para el primer despliegue; la contraseña de bootstrap debe cambiarse desde la
 * aplicación en cuanto se hace el primer login.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {

  private final UsuarioRepository usuarioRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.superadmin.email}")
  private String superAdminEmail;

  @Value("${app.superadmin.password}")
  private String superAdminPassword;

  @Override
  public void run(String... args) {
    boolean yaExisteSuperAdmin = usuarioRepository.findAll().stream()
        .flatMap(u -> u.getRoles().stream())
        .anyMatch(r -> "ROLE_SUPERADMIN".equals(r.getName()));

    if (yaExisteSuperAdmin) {
      return;
    }

    RoleEntity roleSuperAdmin = roleRepository.findByName("ROLE_SUPERADMIN")
        .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_SUPERADMIN")));

    var usuarioExistente = usuarioRepository.findByEmailIgnoreCase(superAdminEmail);
    if (usuarioExistente.isPresent()) {
      // Ya hay una cuenta con ese email: solo se le concede el rol, no se toca su contraseña.
      UsuarioEntity usuario = usuarioExistente.get();
      usuario.addRole(roleSuperAdmin);
      usuarioRepository.save(usuario);
      log.warn("Rol ROLE_SUPERADMIN concedido a la cuenta existente '{}'.", superAdminEmail);
      return;
    }

    UsuarioEntity superAdmin = new UsuarioEntity();
    superAdmin.setEmail(superAdminEmail);
    superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
    superAdmin.setActivo(true);
    superAdmin.addRole(roleSuperAdmin);
    usuarioRepository.save(superAdmin);

    log.warn(
        "Usuario superadmin de arranque creado con email '{}'. "
            + "Cambia su contraseña desde la aplicación en cuanto inicies sesión.",
        superAdminEmail);
  }
}

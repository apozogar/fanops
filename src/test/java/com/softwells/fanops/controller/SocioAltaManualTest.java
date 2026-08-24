package com.softwells.fanops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.RoleRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alta manual de una ficha de socio desde el panel de administración.
 *
 * El formulario manda siempre un objeto "usuario" con los roles del checkbox, aunque la ficha
 * todavía no tenga cuenta: al crear, ese objeto llegaba a Hibernate como instancia transitoria y
 * el guardado fallaba con TransientPropertyValueException. Aquí se fija que el alta funciona y
 * que la ficha se crea sin cuenta, que es lo normal ahora: la cuenta la crea la propia persona
 * al registrarse y confirmar el enlace de vinculación.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SocioAltaManualTest {

  private static final String EMAIL_ADMIN = "test.admin.alta@fanops.local";
  private static final String EMAIL_SOCIO = "test.alta.manual@fanops.local";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private SocioRepository socioRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private PenaRepository penaRepository;

  @Test
  @DisplayName("Crear una ficha desde el panel funciona aunque el formulario mande roles")
  void altaManualDeUnaFichaSinCuenta() throws Exception {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity admin = adminDePruebas(pena);
    int numero = socioRepository.findMaxNumeroSocio().orElse(0) + 1;

    // Cuerpo tal cual lo manda el diálogo de socios: incluye "usuario" con los roles del
    // checkbox de administrador, aunque la ficha nueva todavía no tenga ninguna cuenta.
    String cuerpo = """
        {
          "numeroSocio": %d,
          "nombre": "Alta Manual De Prueba",
          "dni": "00000000T",
          "email": "%s",
          "fechaAlta": "2026-01-15",
          "activo": true,
          "abonadoBetis": false,
          "accionistaBetis": false,
          "exentoPago": false,
          "usuario": { "roles": [ { "id": %d, "name": "ROLE_USER" } ] }
        }
        """.formatted(numero, EMAIL_SOCIO, rolUsuario().getId());

    mockMvc.perform(post("/api/socios")
            .with(user(admin.getEmail()).authorities(() -> "ROLE_ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(cuerpo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.numeroSocio").value(numero));

    List<SocioEntity> creados = socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(EMAIL_SOCIO);
    assertThat(creados).hasSize(1);
    assertThat(creados.get(0).getUsuario())
        .as("el alta manual crea la ficha, nunca la cuenta")
        .isNull();
    assertThat(creados.get(0).getPena())
        .as("la ficha se da de alta en la peña de trabajo de quien la crea")
        .isEqualTo(pena);
  }

  private RoleEntity rolUsuario() {
    return roleRepository.findByName("ROLE_USER")
        .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_USER")));
  }

  private UsuarioEntity adminDePruebas(PenaEntity pena) {
    RoleEntity rolAdmin = roleRepository.findByName("ROLE_ADMIN")
        .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_ADMIN")));
    UsuarioEntity admin = new UsuarioEntity();
    admin.setEmail(EMAIL_ADMIN);
    admin.setPassword("no-se-usa");
    admin.setActivo(true);
    admin.setRoles(Set.of(rolAdmin));
    admin.setPena(pena);
    return usuarioRepository.save(admin);
  }

  private PenaEntity penaDePruebas() {
    List<PenaEntity> penas = penaRepository.findAll();
    if (!penas.isEmpty()) {
      return penas.get(0);
    }
    PenaEntity pena = new PenaEntity();
    pena.setNombre("Peña de pruebas");
    // El dominio es obligatorio y único desde que cada peña se identifica por la URL.
    pena.setSlug("pena-de-pruebas");
    return penaRepository.save(pena);
  }
}

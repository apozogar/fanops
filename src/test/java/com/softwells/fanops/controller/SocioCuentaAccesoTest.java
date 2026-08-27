package com.softwells.fanops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.RoleRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alta manual de la cuenta de acceso de un socio desde el listado de gestión: el administrador le
 * pone una contraseña, en lugar de esperar a que la persona se registre y confirme el enlace de
 * vinculación.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SocioCuentaAccesoTest {

  private static final String EMAIL_ADMIN = "test.admin.cuenta@fanops.local";
  private static final String EMAIL_SOCIO = "test.cuenta.socio@fanops.local";
  private static final String PASSWORD = "ContrasenaDePrueba1";

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
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Crear la cuenta con contraseña vincula todas las fichas de ese email")
  void crearCuentaConPassword() throws Exception {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity admin = adminDePruebas(pena);
    SocioEntity socio = fichaDePruebas(pena, "Socio Con Cuenta", EMAIL_SOCIO);
    // Segunda ficha con el mismo correo: el caso de la familia que comparte buzón.
    SocioEntity familiar = fichaDePruebas(pena, "Familiar Del Socio", EMAIL_SOCIO);

    establecerCuenta(socio, admin, PASSWORD, false).andExpect(status().isOk());

    UsuarioEntity cuenta = usuarioRepository.findByEmailIgnoreCase(EMAIL_SOCIO).orElseThrow();
    assertThat(passwordEncoder.matches(PASSWORD, cuenta.getPassword()))
        .as("la cuenta entra con la contraseña que ha puesto el administrador")
        .isTrue();
    assertThat(cuenta.isActivo()).isTrue();
    assertThat(nombresDeRol(cuenta)).containsExactly("ROLE_USER");
    assertThat(cuenta.getPena()).isEqualTo(pena);
    assertThat(cuenta.getUltimoAcceso())
        .as("crear la cuenta no cuenta como acceso: nadie ha entrado todavía")
        .isNull();

    assertThat(socioRepository.findById(socio.getUid()).orElseThrow().getUsuario())
        .isEqualTo(cuenta);
    assertThat(socioRepository.findById(familiar.getUid()).orElseThrow().getUsuario())
        .as("las demás fichas del mismo email también quedan vinculadas")
        .isEqualTo(cuenta);
  }

  @Test
  @DisplayName("Con el interruptor de administrador la cuenta nueva puede gestionar la peña")
  void crearCuentaDeAdministrador() throws Exception {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity admin = adminDePruebas(pena);
    SocioEntity socio = fichaDePruebas(pena, "Socio Administrador", EMAIL_SOCIO);

    establecerCuenta(socio, admin, PASSWORD, true).andExpect(status().isOk());

    UsuarioEntity cuenta = usuarioRepository.findByEmailIgnoreCase(EMAIL_SOCIO).orElseThrow();
    assertThat(nombresDeRol(cuenta)).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }

  @Test
  @DisplayName("Cambiar la contraseña de una cuenta que ya existe no le toca los roles")
  void cambiarPasswordNoDegradaLaCuenta() throws Exception {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity admin = adminDePruebas(pena);
    SocioEntity socio = fichaDePruebas(pena, "Socio Ya Con Cuenta", EMAIL_SOCIO);

    // La ficha ya tiene cuenta, y esa cuenta es administradora de la peña.
    UsuarioEntity cuenta = new UsuarioEntity();
    cuenta.setEmail(EMAIL_SOCIO);
    cuenta.setPassword(passwordEncoder.encode("la-de-antes"));
    cuenta.setActivo(false);
    cuenta.setRoles(Set.of(rol("ROLE_USER"), rol("ROLE_ADMIN")));
    cuenta.setPena(pena);
    UsuarioEntity guardada = usuarioRepository.save(cuenta);
    socio.setUsuario(guardada);
    socioRepository.save(socio);

    // El interruptor llega a false, que es como lo manda el modal al cambiar una contraseña.
    establecerCuenta(socio, admin, PASSWORD, false).andExpect(status().isOk());

    UsuarioEntity actualizada = usuarioRepository.findByEmailIgnoreCase(EMAIL_SOCIO).orElseThrow();
    assertThat(passwordEncoder.matches(PASSWORD, actualizada.getPassword())).isTrue();
    assertThat(nombresDeRol(actualizada))
        .as("cambiar la contraseña no debe quitarle el rol de administrador")
        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    assertThat(actualizada.isActivo())
        .as("si se le pone contraseña es para que entre, así que se desbloquea")
        .isTrue();
  }

  @Test
  @DisplayName("Sin email en la ficha no se puede crear la cuenta")
  void sinEmailNoHayCuenta() throws Exception {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity admin = adminDePruebas(pena);
    SocioEntity socio = fichaDePruebas(pena, "Socio Sin Email", null);

    establecerCuenta(socio, admin, PASSWORD, false).andExpect(status().isBadRequest());

    assertThat(socioRepository.findById(socio.getUid()).orElseThrow().getUsuario()).isNull();
  }

  @Test
  @DisplayName("Una contraseña demasiado corta se rechaza")
  void passwordCorta() throws Exception {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity admin = adminDePruebas(pena);
    SocioEntity socio = fichaDePruebas(pena, "Socio Password Corta", EMAIL_SOCIO);

    establecerCuenta(socio, admin, "corta", false).andExpect(status().isBadRequest());

    assertThat(usuarioRepository.findByEmailIgnoreCase(EMAIL_SOCIO)).isEmpty();
  }

  private ResultActions establecerCuenta(SocioEntity socio, UsuarioEntity admin, String password,
      boolean esAdmin) throws Exception {
    String cuerpo = """
        { "password": "%s", "admin": %s }
        """.formatted(password, esAdmin);
    return mockMvc.perform(post("/api/socios/{id}/cuenta", socio.getUid())
        .with(user(admin.getEmail()).authorities(() -> "ROLE_ADMIN"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo));
  }

  private List<String> nombresDeRol(UsuarioEntity usuario) {
    return usuario.getRoles().stream().map(RoleEntity::getName).toList();
  }

  private SocioEntity fichaDePruebas(PenaEntity pena, String nombre, String email) {
    SocioEntity socio = new SocioEntity();
    socio.setNumeroSocio(socioRepository.findMaxNumeroSocio().orElse(0) + 1);
    socio.setNombre(nombre);
    socio.setEmail(email);
    socio.setFechaAlta(LocalDate.now());
    socio.setActivo(true);
    socio.setPena(pena);
    return socioRepository.save(socio);
  }

  private RoleEntity rol(String nombre) {
    return roleRepository.findByName(nombre)
        .orElseGet(() -> roleRepository.save(new RoleEntity(nombre)));
  }

  private UsuarioEntity adminDePruebas(PenaEntity pena) {
    UsuarioEntity admin = new UsuarioEntity();
    admin.setEmail(EMAIL_ADMIN);
    admin.setPassword("no-se-usa");
    admin.setActivo(true);
    admin.setRoles(Set.of(rol("ROLE_ADMIN")));
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
    pena.setSlug("pena-de-pruebas");
    return penaRepository.save(pena);
  }
}

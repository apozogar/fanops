package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.model.VinculacionSocioEntity;
import com.softwells.fanops.repository.RoleRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import com.softwells.fanops.repository.VinculacionSocioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * La vinculación por token es lo único que separa "reclamo mi ficha de socio" de "me apropio de
 * la ficha de otro conociendo su email", así que aquí se fija ese contrato: sin invitación válida
 * no se crea ninguna cuenta, y una invitación solo sirve una vez.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VinculacionSocioServiceTest {

  private static final String EMAIL = "socio@peña.test";
  private static final String TOKEN = "token-de-prueba";

  @Mock
  private VinculacionSocioRepository vinculacionRepository;
  @Mock
  private SocioRepository socioRepository;
  @Mock
  private UsuarioRepository usuarioRepository;
  @Mock
  private RoleRepository roleRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JavaMailSender mailSender;

  private VinculacionSocioService service;

  @BeforeEach
  void setUp() {
    service = new VinculacionSocioService(vinculacionRepository, socioRepository, usuarioRepository,
        roleRepository, passwordEncoder, mailSender);
    ReflectionTestUtils.setField(service, "fromAddress", "noreply@fanops.test");
    ReflectionTestUtils.setField(service, "publicBaseUrl", "https://fanops.test");
    ReflectionTestUtils.setField(service, "horasValidez", 48L);

    when(usuarioRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
    when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(passwordEncoder.encode(anyString())).thenAnswer(i -> "hash:" + i.getArgument(0));
    when(roleRepository.findByName("ROLE_USER"))
        .thenReturn(Optional.of(new RoleEntity("ROLE_USER")));
  }

  @Test
  @DisplayName("El registro solo pide verificación si el email tiene fichas sin cuenta")
  void detectaSociosVinculables() {
    when(socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(EMAIL))
        .thenReturn(List.of(socio(1L)));
    assertThat(service.tieneSociosVinculables(EMAIL)).isTrue();

    // Con cuenta ya creada no hay nada que vincular: el registro devuelve su 409 de siempre.
    when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
        .thenReturn(Optional.of(new UsuarioEntity()));
    assertThat(service.tieneSociosVinculables(EMAIL)).isFalse();

    assertThat(service.tieneSociosVinculables("  ")).isFalse();
  }

  @Test
  @DisplayName("El enlace guarda solo el hash del token, nunca el token en claro")
  void guardaElHashDelToken() {
    when(socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(EMAIL))
        .thenReturn(List.of(socio(1L)));

    service.enviarInvitacion(EMAIL, "MiClave123", "https://mi-peña.test");

    ArgumentCaptor<VinculacionSocioEntity> captor =
        ArgumentCaptor.forClass(VinculacionSocioEntity.class);
    verify(vinculacionRepository).save(captor.capture());
    VinculacionSocioEntity invitacion = captor.getValue();

    assertThat(invitacion.getTokenHash()).hasSize(64);
    assertThat(invitacion.getPassword()).isEqualTo("hash:MiClave123");
    assertThat(invitacion.getFechaUso()).isNull();
    assertThat(invitacion.getFechaExpiracion()).isAfter(LocalDateTime.now());
  }

  @Test
  @DisplayName("Confirmar crea la cuenta en la peña de la ficha y le vincula todas sus fichas")
  void confirmarVinculaLasFichasDeLaPenaDeLaFicha() {
    PenaEntity pena = pena(7L);
    SocioEntity titular = socio(1, pena);
    SocioEntity familiar = socio(2, pena);
    when(vinculacionRepository.findByTokenHash(hash(TOKEN)))
        .thenReturn(Optional.of(invitacionValida("hash:MiClave123")));
    when(socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(EMAIL))
        .thenReturn(List.of(titular, familiar));

    UsuarioEntity usuario = service.confirmar(TOKEN, null);

    assertThat(usuario.getEmail()).isEqualTo(EMAIL);
    assertThat(usuario.getPassword()).isEqualTo("hash:MiClave123");
    // La peña la manda la ficha existente, no la peña por defecto del auto-registro.
    assertThat(usuario.getPena()).isEqualTo(pena);
    assertThat(titular.getUsuario()).isEqualTo(usuario);
    assertThat(familiar.getUsuario()).isEqualTo(usuario);
    // No se crea ninguna ficha nueva: se reutilizan las que ya estaban en el listado.
    verify(socioRepository, never()).save(any());
  }

  @Test
  @DisplayName("Una invitación ya usada o caducada no vuelve a crear cuenta")
  void rechazaInvitacionesNoUtilizables() {
    VinculacionSocioEntity usada = invitacionValida("hash:MiClave123");
    usada.setFechaUso(LocalDateTime.now().minusMinutes(1));
    when(vinculacionRepository.findByTokenHash(hash(TOKEN))).thenReturn(Optional.of(usada));
    assertThatThrownBy(() -> service.confirmar(TOKEN, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ya se ha usado");

    VinculacionSocioEntity caducada = invitacionValida("hash:MiClave123");
    caducada.setFechaExpiracion(LocalDateTime.now().minusHours(1));
    when(vinculacionRepository.findByTokenHash(hash(TOKEN))).thenReturn(Optional.of(caducada));
    assertThatThrownBy(() -> service.confirmar(TOKEN, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("caducado");

    when(vinculacionRepository.findByTokenHash(hash("otro"))).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.confirmar("otro", null))
        .isInstanceOf(IllegalArgumentException.class);

    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("Si la invitación no traía contraseña, hay que indicarla al confirmar")
  void exigePasswordCuandoLaInvitacionNoLaTrae() {
    when(vinculacionRepository.findByTokenHash(hash(TOKEN)))
        .thenReturn(Optional.of(invitacionValida(null)));
    when(socioRepository.findByEmailIgnoreCaseAndUsuarioIsNull(EMAIL))
        .thenReturn(List.of(socio(1L)));

    assertThatThrownBy(() -> service.confirmar(TOKEN, " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("contraseña");

    assertThat(service.confirmar(TOKEN, "OtraClave123").getPassword())
        .isEqualTo("hash:OtraClave123");
  }

  private VinculacionSocioEntity invitacionValida(String passwordCodificada) {
    VinculacionSocioEntity invitacion = new VinculacionSocioEntity();
    invitacion.setTokenHash(hash(TOKEN));
    invitacion.setEmail(EMAIL);
    invitacion.setPassword(passwordCodificada);
    invitacion.setFechaCreacion(LocalDateTime.now());
    invitacion.setFechaExpiracion(LocalDateTime.now().plusHours(48));
    return invitacion;
  }

  private SocioEntity socio(Long penaId) {
    return socio(1, pena(penaId));
  }

  private SocioEntity socio(int numeroSocio, PenaEntity pena) {
    SocioEntity socio = new SocioEntity();
    socio.setNombre("Socio " + numeroSocio);
    socio.setNumeroSocio(numeroSocio);
    socio.setEmail(EMAIL);
    socio.setPena(pena);
    return socio;
  }

  private PenaEntity pena(Long id) {
    PenaEntity pena = new PenaEntity();
    pena.setId(id);
    pena.setNombre("Peña " + id);
    return pena;
  }

  private String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}

package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwells.fanops.controller.dto.InscripcionPublicaRequest;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.EventoInscripcionRepository;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inscripción desde el enlace público. El correo es lo que identifica al socio: con el de su
 * ficha (o el de su cuenta) se le trata igual que si se hubiera apuntado desde la aplicación.
 */
@SpringBootTest
@Transactional
class InscripcionPublicaFlowTest {

  private static final String EMAIL_FICHA = "test.publica.ficha@fanops.local";
  private static final String EMAIL_CUENTA = "test.publica.cuenta@fanops.local";

  @Autowired
  private EventoService eventoService;
  @Autowired
  private EventoInscripcionRepository inscripcionRepository;
  @Autowired
  private SocioRepository socioRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private PenaRepository penaRepository;

  @Test
  @DisplayName("Un socio que usa el enlace público con el correo de su ficha entra con plaza")
  void socioPorCorreoDeFichaEntraConfirmado() {
    SocioEntity socio = ficha("Juan Pérez", EMAIL_FICHA, null);
    EventoEntity evento = evento();

    EstadoInscripcion estado = eventoService.inscribirPublico(evento.getUid(),
        peticion("juan perez", EMAIL_FICHA.toUpperCase()));

    assertThat(estado).isEqualTo(EstadoInscripcion.CONFIRMADA);
    EventoInscripcionEntity inscripcion = inscripcionRepository
        .findByEventoUidAndSocioUid(evento.getUid(), socio.getUid()).orElseThrow();
    assertThat(inscripcion.getSocio().getUid())
        .as("la inscripción queda ligada a la ficha, como si viniera de la app")
        .isEqualTo(socio.getUid());
  }

  @Test
  @DisplayName("El correo de la cuenta también identifica a su ficha")
  void socioPorCorreoDeCuenta() {
    SocioEntity socio = ficha("Ana López", null, cuenta(EMAIL_CUENTA));
    EventoEntity evento = evento();

    eventoService.inscribirPublico(evento.getUid(), peticion("Ana López", EMAIL_CUENTA));

    assertThat(inscripcionRepository.existsByEventoUidAndSocioUid(evento.getUid(),
        socio.getUid())).isTrue();
  }

  @Test
  @DisplayName("En un multicarnet se elige la ficha por el nombre; sin coincidencia es no socio")
  void multicarnetSeResuelvePorNombre() {
    UsuarioEntity usuario = cuenta(EMAIL_CUENTA);
    ficha("Padre Gómez", EMAIL_CUENTA, usuario);
    SocioEntity hijo = ficha("Hijo Gómez", EMAIL_CUENTA, usuario);
    EventoEntity evento = evento();

    eventoService.inscribirPublico(evento.getUid(), peticion("Hijo Gómez", EMAIL_CUENTA));
    assertThat(inscripcionRepository.existsByEventoUidAndSocioUid(evento.getUid(),
        hijo.getUid())).isTrue();

    EstadoInscripcion desconocido = eventoService.inscribirPublico(evento().getUid(),
        peticion("Otra Persona", EMAIL_CUENTA));
    assertThat(desconocido)
        .as("no se adivina a qué ficha de la familia apuntar")
        .isEqualTo(EstadoInscripcion.EN_ESPERA);
  }

  @Test
  @DisplayName("Un socio no puede apuntarse dos veces aunque use el enlace público")
  void socioYaInscritoNoRepite() {
    ficha("Juan Pérez", EMAIL_FICHA, null);
    EventoEntity evento = evento();
    eventoService.inscribirPublico(evento.getUid(), peticion("Juan Pérez", EMAIL_FICHA));

    assertThatThrownBy(() -> eventoService.inscribirPublico(evento.getUid(),
        peticion("Juan Pérez", EMAIL_FICHA)))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Quien no es socio sigue entrando en lista de espera")
  void noSocioEntraEnEspera() {
    EventoEntity evento = evento();

    EstadoInscripcion estado = eventoService.inscribirPublico(evento.getUid(),
        peticion("Invitado", "test.publica.invitado@fanops.local"));

    assertThat(estado).isEqualTo(EstadoInscripcion.EN_ESPERA);
  }

  private InscripcionPublicaRequest peticion(String nombre, String email) {
    InscripcionPublicaRequest request = new InscripcionPublicaRequest();
    request.setNombre(nombre);
    request.setEmail(email);
    return request;
  }

  private EventoEntity evento() {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Partido de pruebas");
    evento.setFechaEvento(LocalDate.now().plusDays(7));
    evento.setNumeroPlazas(50);
    return eventoService.save(evento);
  }

  private UsuarioEntity cuenta(String email) {
    UsuarioEntity usuario = new UsuarioEntity();
    usuario.setEmail(email);
    usuario.setPassword("no-se-usa");
    usuario.setActivo(true);
    usuario.setPena(penaDePruebas());
    return usuarioRepository.save(usuario);
  }

  private SocioEntity ficha(String nombre, String email, UsuarioEntity usuario) {
    SocioEntity socio = new SocioEntity();
    socio.setNumeroSocio(socioRepository.findMaxNumeroSocio().orElse(0) + 1);
    socio.setNombre(nombre);
    socio.setEmail(email);
    socio.setFechaAlta(LocalDate.now());
    socio.setActivo(true);
    socio.setPena(penaDePruebas());
    socio.setUsuario(usuario);
    return socioRepository.saveAndFlush(socio);
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

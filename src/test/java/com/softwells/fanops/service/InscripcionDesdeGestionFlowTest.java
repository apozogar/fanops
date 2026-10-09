package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.EventoInscripcionRepository;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.SolicitudCarnetRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inscripción de un socio desde el listado de socios, para quien no usa la aplicación. Sigue las
 * mismas reglas que si se apuntara él y solo alcanza a socios de la peña de quien gestiona.
 */
@SpringBootTest
@Transactional
@WithMockUser(username = InscripcionDesdeGestionFlowTest.EMAIL_ADMIN)
class InscripcionDesdeGestionFlowTest {

  static final String EMAIL_ADMIN = "test.gestion.inscripcion@fanops.local";

  @Autowired
  private EventoService eventoService;
  @Autowired
  private EventoInscripcionRepository inscripcionRepository;
  @Autowired
  private SolicitudCarnetRepository solicitudRepository;
  @Autowired
  private SocioRepository socioRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private PenaRepository penaRepository;

  private PenaEntity miPena;

  @BeforeEach
  void cuentaDeGestion() {
    miPena = pena();
    UsuarioEntity admin = new UsuarioEntity();
    admin.setEmail(EMAIL_ADMIN);
    admin.setPassword("no-se-usa");
    admin.setActivo(true);
    admin.setPena(miPena);
    usuarioRepository.save(admin);
  }

  @Test
  @DisplayName("Apunta al socio con plaza si hay hueco")
  void apuntaConPlaza() {
    SocioEntity socio = ficha(miPena);
    EventoEntity evento = evento(50, false);

    EstadoInscripcion estado = eventoService.inscribirSocioDesdeGestion(evento.getUid(),
        socio.getUid(), false);

    assertThat(estado).isEqualTo(EstadoInscripcion.CONFIRMADA);
    assertThat(inscripcionRepository.existsByEventoUidAndSocioUid(evento.getUid(),
        socio.getUid())).isTrue();
  }

  @Test
  @DisplayName("Con el evento completo queda en lista de espera, como si se apuntara él")
  void completoVaAEspera() {
    SocioEntity primero = ficha(miPena);
    SocioEntity segundo = ficha(miPena);
    EventoEntity evento = evento(1, false);
    eventoService.inscribirSocioDesdeGestion(evento.getUid(), primero.getUid(), false);

    EstadoInscripcion estado = eventoService.inscribirSocioDesdeGestion(evento.getUid(),
        segundo.getUid(), false);

    assertThat(estado).isEqualTo(EstadoInscripcion.EN_ESPERA);
  }

  @Test
  @DisplayName("No se puede apuntar dos veces al mismo socio")
  void noRepite() {
    SocioEntity socio = ficha(miPena);
    EventoEntity evento = evento(50, false);
    eventoService.inscribirSocioDesdeGestion(evento.getUid(), socio.getUid(), false);

    assertThatThrownBy(() -> eventoService.inscribirSocioDesdeGestion(evento.getUid(),
        socio.getUid(), false))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Un socio de otra peña no se puede apuntar desde aquí")
  void otraPena() {
    SocioEntity ajeno = ficha(pena());
    EventoEntity evento = evento(50, false);

    assertThatThrownBy(() -> eventoService.inscribirSocioDesdeGestion(evento.getUid(),
        ajeno.getUid(), false))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Al sorteo del carnet: entra en el bombo y queda apuntado al evento")
  void alSorteoDelCarnet() {
    SocioEntity socio = ficha(miPena);
    EventoEntity evento = evento(50, true);

    EstadoInscripcion estado = eventoService.inscribirSocioDesdeGestion(evento.getUid(),
        socio.getUid(), true);

    assertThat(estado).isEqualTo(EstadoInscripcion.CONFIRMADA);
    assertThat(solicitudRepository.existsByEventoUidAndSocioUid(evento.getUid(),
        socio.getUid())).isTrue();
    assertThat(inscripcionRepository.existsByEventoUidAndSocioUid(evento.getUid(),
        socio.getUid())).isTrue();
  }

  // ----------------------------------------------------------------

  private EventoEntity evento(int plazas, boolean conSorteo) {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Partido de pruebas");
    evento.setFechaEvento(LocalDate.now().plusDays(7));
    evento.setNumeroPlazas(plazas);
    if (conSorteo) {
      evento.setPlazasCarnet(2);
      evento.setFechaSorteoCarnet(LocalDateTime.now().plusDays(2));
      evento.setPlazasCarnetReservadas(false); // evento anterior a la reserva de plazas
    }
    return eventoService.save(evento);
  }

  private SocioEntity ficha(PenaEntity pena) {
    SocioEntity socio = new SocioEntity();
    socio.setNumeroSocio(socioRepository.findMaxNumeroSocio().orElse(0) + 1);
    socio.setNombre("Socio Gestión " + UUID.randomUUID().toString().substring(0, 6));
    socio.setFechaAlta(LocalDate.now());
    socio.setActivo(true);
    socio.setPena(pena);
    return socioRepository.saveAndFlush(socio);
  }

  private PenaEntity pena() {
    PenaEntity pena = new PenaEntity();
    pena.setNombre("Peña gestión " + UUID.randomUUID());
    pena.setSlug("pena-gestion-" + UUID.randomUUID());
    return penaRepository.save(pena);
  }
}

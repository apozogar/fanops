package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwells.fanops.controller.dto.ParticipanteSorteoDTO;
import com.softwells.fanops.controller.dto.SorteoCarnetDTO;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.SolicitudCarnetEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.SolicitudCarnetRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Papeletas extra del sorteo de carnets: se suman a las del historial, se publican aparte y solo
 * las puede poner la gestión de la propia peña, con la opción activada y antes del sorteo.
 */
@SpringBootTest
@Transactional
@WithMockUser(username = PapeletasExtraFlowTest.EMAIL_USUARIO)
class PapeletasExtraFlowTest {

  static final String EMAIL_USUARIO = "test.papeletas.extra@fanops.local";

  @Autowired
  private SorteoCarnetService sorteoCarnetService;
  @Autowired
  private EventoService eventoService;
  @Autowired
  private SolicitudCarnetRepository solicitudRepository;
  @Autowired
  private SocioRepository socioRepository;
  @Autowired
  private UsuarioRepository usuarioRepository;
  @Autowired
  private PenaRepository penaRepository;

  @Test
  @DisplayName("Las papeletas extra se suman al historial y el sorteo se celebra con el total")
  void seSumanYCuentanAlCelebrar() {
    PenaEntity pena = penaConPapeletasExtra(true);
    Bombo bombo = fichaEnBombo(pena);
    SocioEntity socio = bombo.socio();
    EventoEntity evento = bombo.evento();

    SorteoCarnetDTO sorteo = sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(),
        socio.getUid(), 3);

    assertThat(sorteo.isAjustePapeletasPermitido()).isTrue();
    ParticipanteSorteoDTO participante = participante(sorteo, socio.getUid());
    assertThat(participante.getPapeletasExtra()).isEqualTo(3);
    assertThat(participante.getPapeletas())
        .as("una de salida por no tener historial, más las tres extra")
        .isEqualTo(4);

    sorteoCarnetService.celebrarAhora(evento.getUid());
    SolicitudCarnetEntity solicitud = solicitudRepository
        .findByEventoUidAndSocioUid(evento.getUid(), socio.getUid()).orElseThrow();
    assertThat(solicitud.getPesoSorteo()).isEqualTo(4);
  }

  @Test
  @DisplayName("Poner 0 quita las papeletas extra")
  void ceroLasQuita() {
    PenaEntity pena = penaConPapeletasExtra(true);
    Bombo bombo = fichaEnBombo(pena);
    SocioEntity socio = bombo.socio();
    EventoEntity evento = bombo.evento();
    sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(), socio.getUid(), 5);

    SorteoCarnetDTO sorteo = sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(),
        socio.getUid(), 0);

    assertThat(participante(sorteo, socio.getUid()).getPapeletas()).isEqualTo(1);
  }

  @Test
  @DisplayName("Con la opción desactivada en la peña no se pueden poner ni cuentan")
  void opcionDesactivada() {
    PenaEntity pena = penaConPapeletasExtra(false);
    Bombo bombo = fichaEnBombo(pena);
    SocioEntity socio = bombo.socio();
    EventoEntity evento = bombo.evento();

    assertThatThrownBy(() -> sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(),
        socio.getUid(), 2))
        .isInstanceOf(IllegalStateException.class);
    assertThat(sorteoCarnetService.consultar(evento.getUid()).isAjustePapeletasPermitido())
        .isFalse();
  }

  @Test
  @DisplayName("Con el sorteo celebrado ya no se tocan las papeletas")
  void sorteoCelebrado() {
    PenaEntity pena = penaConPapeletasExtra(true);
    Bombo bombo = fichaEnBombo(pena);
    SocioEntity socio = bombo.socio();
    EventoEntity evento = bombo.evento();
    sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThatThrownBy(() -> sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(),
        socio.getUid(), 2))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Fuera del rango permitido se rechaza")
  void fueraDeRango() {
    PenaEntity pena = penaConPapeletasExtra(true);
    Bombo bombo = fichaEnBombo(pena);
    SocioEntity socio = bombo.socio();
    EventoEntity evento = bombo.evento();

    assertThatThrownBy(() -> sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(),
        socio.getUid(), SorteoCarnetService.MAX_PAPELETAS_EXTRA + 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> sorteoCarnetService.ajustarPapeletasExtra(evento.getUid(),
        socio.getUid(), -1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ----------------------------------------------------------------

  private ParticipanteSorteoDTO participante(SorteoCarnetDTO sorteo, UUID socioUid) {
    return sorteo.getParticipantes().stream()
        .filter(p -> p.getSocioUid().equals(socioUid))
        .findFirst().orElseThrow();
  }

  private record Bombo(SocioEntity socio, EventoEntity evento) {
  }

  /**
   * Una cuenta con una ficha metida en el bombo de un evento nuevo. La cuenta es a la vez la de
   * quien gestiona, así que su peña es la que se comprueba al ajustar.
   */
  private Bombo fichaEnBombo(PenaEntity pena) {
    UsuarioEntity usuario = new UsuarioEntity();
    usuario.setEmail(EMAIL_USUARIO);
    usuario.setPassword("no-se-usa");
    usuario.setActivo(true);
    usuario.setPena(pena);
    UsuarioEntity guardado = usuarioRepository.save(usuario);

    SocioEntity socio = new SocioEntity();
    socio.setNumeroSocio(socioRepository.findMaxNumeroSocio().orElse(0) + 1);
    socio.setNombre("Socio Papeletas");
    socio.setFechaAlta(LocalDate.now());
    socio.setActivo(true);
    socio.setPena(pena);
    socio.setUsuario(guardado);
    SocioEntity ficha = socioRepository.save(socio);
    guardado.getSocios().add(ficha);
    usuarioRepository.save(guardado);

    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Partido de pruebas");
    evento.setFechaEvento(LocalDate.now().plusDays(7));
    evento.setNumeroPlazas(50);
    evento.setPlazasCarnet(2);
    evento.setFechaSorteoCarnet(LocalDateTime.now().plusDays(2));
    EventoEntity guardadoEvento = eventoService.save(evento);

    sorteoCarnetService.solicitar(guardadoEvento.getUid(), List.of(ficha));
    return new Bombo(ficha, guardadoEvento);
  }

  private PenaEntity penaConPapeletasExtra(boolean activadas) {
    PenaEntity pena = new PenaEntity();
    pena.setNombre("Peña papeletas " + UUID.randomUUID());
    pena.setSlug("pena-papeletas-" + UUID.randomUUID());
    pena.setPapeletasExtraSorteo(activadas);
    return penaRepository.save(pena);
  }
}

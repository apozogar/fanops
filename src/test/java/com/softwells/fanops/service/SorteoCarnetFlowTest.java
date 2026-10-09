package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.InscripcionSocioRequest;
import com.softwells.fanops.controller.dto.SolicitudCarnetRequest;
import com.softwells.fanops.controller.dto.SorteoCarnetDTO;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import com.softwells.fanops.enums.EstadoSorteo;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.EventoInscripcionRepository;
import com.softwells.fanops.repository.PenaRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.SorteoCarnetRepository;
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
 * Flujo completo del sorteo de carnets contra la base de datos real.
 *
 * Aquí no se comprueba el azar (de eso va {@link SorteoAleatorioTest}) sino lo que rodea al
 * bombo: que programar el evento deja la semilla fijada, que celebrarlo reparte exactamente
 * los carnets que hay y deja al resto ordenado como suplentes, y que una renuncia mueve la lista
 * sin repetir el sorteo.
 */
@SpringBootTest
@Transactional
@WithMockUser(username = SorteoCarnetFlowTest.EMAIL_USUARIO)
class SorteoCarnetFlowTest {

  static final String EMAIL_USUARIO = "test.sorteo.carnet@fanops.local";

  private static final int CARNETS = 2;

  @Autowired
  private SorteoCarnetService sorteoCarnetService;
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
  @Autowired
  private SorteoCarnetRepository sorteoRepository;

  @Test
  @DisplayName("Programar un evento con carnets deja la semilla fijada y el bombo abierto")
  void programarDejaElBomboAbierto() {
    cuentaConFichas(1);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));

    SorteoCarnetDTO sorteo = sorteoCarnetService.consultar(evento.getUid());

    assertThat(sorteo.isHabilitado()).isTrue();
    assertThat(sorteo.isAbierto()).isTrue();
    assertThat(sorteo.getEstado()).isEqualTo(EstadoSorteo.PROGRAMADO);
    assertThat(sorteo.getPlazasCarnet()).isEqualTo(CARNETS);
    assertThat(sorteoRepository.findByEventoUid(evento.getUid()).orElseThrow().getHashSemilla())
        .as("la semilla queda fijada al programar el sorteo")
        .hasSize(64);
  }

  @Test
  @DisplayName("Celebrar reparte los carnets y deja al resto de suplentes en orden")
  void celebrarReparteYOrdenaSuplentes() {
    List<SocioEntity> fichas = cuentaConFichas(5);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), fichas);

    SorteoCarnetDTO sorteo = sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThat(sorteo.getEstado()).isEqualTo(EstadoSorteo.EJECUTADO);
    assertThat(sorteo.isAbierto()).isFalse();

    assertThat(sorteo.getParticipantes()).hasSize(5);
    assertThat(sorteo.getParticipantes())
        .extracting(p -> p.getPosicion())
        .containsExactly(1, 2, 3, 4, 5);
    assertThat(sorteo.getParticipantes().stream()
        .filter(p -> p.getEstado() == EstadoSolicitudCarnet.GANADORA))
        .hasSize(CARNETS);
    assertThat(sorteo.getParticipantes().stream()
        .filter(p -> p.getEstado() == EstadoSolicitudCarnet.SUPLENTE))
        .hasSize(3);
  }

  @Test
  @DisplayName("Volver a consultar un sorteo celebrado devuelve exactamente el mismo orden")
  void elOrdenNoCambiaAlConsultarlo() {
    List<SocioEntity> fichas = cuentaConFichas(6);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), fichas);

    List<UUID> ordenAlCelebrar = sorteoCarnetService.celebrarAhora(evento.getUid())
        .getParticipantes().stream().map(p -> p.getSocioUid()).toList();
    List<UUID> ordenAlRepetir = sorteoCarnetService.consultar(evento.getUid())
        .getParticipantes().stream().map(p -> p.getSocioUid()).toList();

    assertThat(ordenAlRepetir)
        .as("la repetición del bombo tiene que enseñar lo mismo que se vio en directo")
        .isEqualTo(ordenAlCelebrar);
  }

  @Test
  @DisplayName("Renunciar a un carnet lo pasa al primer suplente, sin repetir el sorteo")
  void renunciarPromocionaAlPrimerSuplente() {
    List<SocioEntity> fichas = cuentaConFichas(4);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), fichas);

    SorteoCarnetDTO celebrado = sorteoCarnetService.celebrarAhora(evento.getUid());
    UUID ganador = celebrado.getParticipantes().get(0).getSocioUid();
    UUID primerSuplente = celebrado.getParticipantes().get(CARNETS).getSocioUid();

    SorteoCarnetDTO trasRenuncia = sorteoCarnetService.renunciar(evento.getUid(), ganador);

    assertThat(estadoDe(trasRenuncia, ganador)).isEqualTo(EstadoSolicitudCarnet.RENUNCIADA);
    assertThat(estadoDe(trasRenuncia, primerSuplente))
        .as("el carnet baja por la lista de extracción en vez de volver a sortearse")
        .isEqualTo(EstadoSolicitudCarnet.GANADORA);
    assertThat(trasRenuncia.getParticipantes())
        .extracting(p -> p.getPosicion())
        .as("las posiciones son el resultado del bombo y no se tocan nunca")
        .containsExactly(1, 2, 3, 4);
  }

  @Test
  @DisplayName("Quedarse sin carnet suma papeletas para el siguiente sorteo")
  void quedarseSinCarnetSumaPapeletas() {
    List<SocioEntity> fichas = cuentaConFichas(3);
    EventoEntity primero = eventoConSorteo(LocalDateTime.now().plusDays(1));
    apuntar(primero.getUid(), fichas);
    SorteoCarnetDTO celebrado = sorteoCarnetService.celebrarAhora(primero.getUid());

    // Con 3 participantes y 2 carnets, el tercero de la extracción es el que se queda fuera.
    UUID sinSuerte = celebrado.getParticipantes().get(2).getSocioUid();
    UUID premiado = celebrado.getParticipantes().get(0).getSocioUid();

    EventoEntity segundo = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(segundo.getUid(), fichas);
    SorteoCarnetDTO siguiente = sorteoCarnetService.consultar(segundo.getUid());

    assertThat(papeletasDe(siguiente, sinSuerte))
        .as("el que se quedó sin carnet entra con una papeleta más")
        .isEqualTo(2);
    assertThat(papeletasDe(siguiente, premiado))
        .as("al que le tocó vuelve a empezar desde una")
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Entrar en el bombo apunta también al evento, sin una segunda acción")
  void entrarEnElBomboApuntaAlEvento() {
    List<SocioEntity> fichas = cuentaConFichas(2);
    EventoEntity evento = eventoSinReserva(LocalDateTime.now().plusDays(2), 50);

    apuntar(evento.getUid(), fichas);

    for (SocioEntity ficha : fichas) {
      assertThat(inscripcionRepository.findByEventoUidAndSocioUid(evento.getUid(), ficha.getUid()))
          .as("quien entra al sorteo se queda inscrito en el evento")
          .isPresent()
          .get()
          .extracting(EventoInscripcionEntity::getEstado)
          .isEqualTo(EstadoInscripcion.CONFIRMADA);
    }
  }

  @Test
  @DisplayName("Entrar en el bombo no ocupa plaza de autobús: se reservan las de los carnets")
  void entrarEnElBomboNoOcupaPlaza() {
    // 10 plazas y CARNETS reservadas para el sorteo; 6 fichas entran en el bombo.
    List<SocioEntity> fichas = cuentaConFichas(6);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2), 10);

    apuntar(evento.getUid(), fichas);

    assertThat(inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid()))
        .as("entrar en el bombo no crea inscripción").isEmpty();
    EventoInscripcionDTO info = eventoService.infoPublica(evento.getUid());
    assertThat(info.getPlazasLibres())
        .as("a las inscripciones normales solo les quedan las no reservadas")
        .isEqualTo(10 - CARNETS);
  }

  @Test
  @DisplayName("Al celebrar, solo los ganadores tienen plaza; reiniciar se la quita")
  void soloLosGanadoresTienenPlaza() {
    List<SocioEntity> fichas = cuentaConFichas(6);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2), 10);
    apuntar(evento.getUid(), fichas);

    sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThat(inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid()))
        .as("una plaza por carnet, confirmada")
        .hasSize(CARNETS)
        .extracting(EventoInscripcionEntity::getEstado)
        .containsOnly(EstadoInscripcion.CONFIRMADA);
    assertThat(eventoService.infoPublica(evento.getUid()).getPlazasLibres())
        .isEqualTo(10 - CARNETS);

    sorteoCarnetService.reiniciar(evento.getUid());

    assertThat(inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid()))
        .as("al reiniciar vuelven a quedar reservadas, sin inscripciones").isEmpty();
  }

  @Test
  @DisplayName("Las plazas reservadas que no hacen falta pasan a la lista de espera al sortear")
  void lasReservadasSobrantesPasanALaEspera() {
    // 3 plazas y 2 reservadas: a las normales solo les cabe 1. Solo una ficha entra en el bombo,
    // así que sobra una de las dos reservadas.
    List<SocioEntity> fichas = cuentaConFichas(3);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2), 3);
    for (int i = 0; i < 2; i++) {
      InscripcionSocioRequest inscripcion = new InscripcionSocioRequest();
      inscripcion.setSocioUids(List.of(fichas.get(i).getUid()));
      eventoService.inscribirSocios(evento.getUid(), inscripcion);
    }
    assertThat(inscripcionRepository.findByEventoUidAndSocioUid(evento.getUid(),
        fichas.get(1).getUid()).orElseThrow().getEstado())
        .as("solo cabe una plaza normal").isEqualTo(EstadoInscripcion.EN_ESPERA);
    apuntar(evento.getUid(), List.of(fichas.get(2)));

    sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThat(inscripcionRepository.findByEventoUidAndSocioUid(evento.getUid(),
        fichas.get(1).getUid()).orElseThrow().getEstado())
        .as("la plaza reservada que sobra pasa al de la espera")
        .isEqualTo(EstadoInscripcion.CONFIRMADA);
  }

  @Test
  @DisplayName("Al pasar un evento a plazas reservadas, los del bombo siguen apuntados pero no ocupan; los que no ganan salen")
  void pasarUnEventoYaEnMarchaALasPlazasReservadas() {
    List<SocioEntity> fichas = cuentaConFichas(4);
    // Evento anterior a la reserva: 3 plazas, y los 3 primeros entran al bombo con plaza.
    EventoEntity evento = eventoSinReserva(LocalDateTime.now().plusDays(2), 3);
    apuntar(evento.getUid(), fichas.subList(0, 3));
    assertThat(eventoService.infoPublica(evento.getUid()).getPlazasLibres()).isZero();

    EventoEntity cambios = new EventoEntity();
    cambios.setNombreEvento(evento.getNombreEvento());
    cambios.setFechaEvento(evento.getFechaEvento());
    cambios.setNumeroPlazas(3);
    cambios.setPlazasCarnet(CARNETS);
    cambios.setFechaSorteoCarnet(evento.getFechaSorteoCarnet());
    cambios.setPlazasCarnetReservadas(true);
    eventoService.update(evento.getUid(), cambios);

    assertThat(inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid()))
        .as("siguen apuntados").hasSize(3);
    assertThat(eventoService.infoPublica(evento.getUid()).getPlazasLibres())
        .as("solo queda reservada la de los carnets: 3 plazas menos 2 de carnets")
        .isEqualTo(1);

    // El cuarto socio ya cabe en el autobús sin pasar por el sorteo.
    InscripcionSocioRequest inscripcion = new InscripcionSocioRequest();
    inscripcion.setSocioUids(List.of(fichas.get(3).getUid()));
    eventoService.inscribirSocios(evento.getUid(), inscripcion);
    assertThat(inscripcionRepository.findByEventoUidAndSocioUid(evento.getUid(),
        fichas.get(3).getUid()).orElseThrow().getEstado()).isEqualTo(EstadoInscripcion.CONFIRMADA);

    sorteoCarnetService.celebrarAhora(evento.getUid());

    long conPlaza = inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(
        evento.getUid()).stream().filter(i -> i.getEstado() == EstadoInscripcion.CONFIRMADA)
        .count();
    assertThat(conPlaza)
        .as("los 2 ganadores del bombo más el socio que solo quería autobús").isEqualTo(3);
  }

  @Test
  @DisplayName("Quien ya tenía plaza de autobús la conserva aunque no gane el carnet")
  void quienYaTeniaPlazaLaConserva() {
    List<SocioEntity> fichas = cuentaConFichas(6);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2), 10);
    SocioEntity conPlaza = fichas.get(0);
    InscripcionSocioRequest inscripcion = new InscripcionSocioRequest();
    inscripcion.setSocioUids(List.of(conPlaza.getUid()));
    eventoService.inscribirSocios(evento.getUid(), inscripcion);
    apuntar(evento.getUid(), fichas);

    sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThat(inscripcionRepository.findByEventoUidAndSocioUid(evento.getUid(),
        conPlaza.getUid()))
        .as("su inscripción no depende del resultado")
        .isPresent();
  }

  @Test
  @DisplayName("Entrando por el sorteo, 'solo si entramos todos' sigue valiendo para la plaza")
  void elGrupoNoSeParteAlEntrarPorElSorteo() {
    List<SocioEntity> fichas = cuentaConFichas(2);
    EventoEntity evento = eventoSinReserva(LocalDateTime.now().plusDays(2), 1);

    SolicitudCarnetRequest request = new SolicitudCarnetRequest();
    request.setSocioUids(fichas.stream().map(SocioEntity::getUid).toList());
    request.setSoloSiEntranTodos(true);
    eventoService.apuntarAlSorteoCarnet(evento.getUid(), request);

    assertThat(inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid()))
        .as("con una sola plaza y el grupo sin partir, los dos esperan")
        .extracting(EventoInscripcionEntity::getEstado)
        .containsOnly(EstadoInscripcion.EN_ESPERA);
  }

  @Test
  @DisplayName("A quien ya estaba inscrito en el evento no se le duplica ni se le toca la plaza")
  void noSeReinscribeAQuienYaTeniaPlaza() {
    List<SocioEntity> fichas = cuentaConFichas(1);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));

    InscripcionSocioRequest inscripcion = new InscripcionSocioRequest();
    inscripcion.setSocioUids(List.of(fichas.get(0).getUid()));
    eventoService.inscribirSocios(evento.getUid(), inscripcion);

    apuntar(evento.getUid(), fichas);

    assertThat(inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid()))
        .as("la inscripción que ya existía se respeta, no se crea otra")
        .hasSize(1);
  }

  @Test
  @DisplayName("Con el sorteo ya celebrado no se admiten más solicitudes")
  void noSeEntraAlBomboDespuesDelSorteo() {
    List<SocioEntity> fichas = cuentaConFichas(2);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), List.of(fichas.get(0)));
    sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThatThrownBy(() -> apuntar(evento.getUid(), List.of(fichas.get(1))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ya se ha celebrado");
  }

  // ----------------------------------------------------------------
  // Utilidades del test
  // ----------------------------------------------------------------

  @Test
  @DisplayName("Reiniciar un sorteo celebrado reabre el bombo con todos pendientes")
  void reiniciarReabreElBombo() {
    List<SocioEntity> fichas = cuentaConFichas(4);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), fichas);
    sorteoCarnetService.celebrarAhora(evento.getUid());

    SorteoCarnetDTO reabierto = sorteoCarnetService.reiniciar(evento.getUid());

    assertThat(reabierto.getEstado()).isEqualTo(EstadoSorteo.PROGRAMADO);
    assertThat(reabierto.isAbierto()).isTrue();
    assertThat(reabierto.getParticipantes()).hasSize(4)
        .allSatisfy(p -> {
          assertThat(p.getEstado()).isEqualTo(EstadoSolicitudCarnet.PENDIENTE);
          assertThat(p.getPosicion()).isNull();
        });
  }

  @Test
  @DisplayName("Reiniciar no sirve para repetir: con el mismo bombo sale el mismo resultado")
  void reiniciarMantieneLaSemilla() {
    List<SocioEntity> fichas = cuentaConFichas(6);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), fichas);
    SorteoCarnetDTO primero = sorteoCarnetService.celebrarAhora(evento.getUid());
    String semilla = sorteoRepository.findByEventoUid(evento.getUid()).orElseThrow().getSemilla();

    sorteoCarnetService.reiniciar(evento.getUid());
    SorteoCarnetDTO segundo = sorteoCarnetService.celebrarAhora(evento.getUid());

    assertThat(sorteoRepository.findByEventoUid(evento.getUid()).orElseThrow().getSemilla())
        .isEqualTo(semilla);
    assertThat(segundo.getParticipantes().stream().map(p -> p.getSocioUid()).toList())
        .isEqualTo(primero.getParticipantes().stream().map(p -> p.getSocioUid()).toList());
  }

  @Test
  @DisplayName("Al reiniciar, quien renunció a su carnet sale del bombo")
  void reiniciarSacaALosQueRenunciaron() {
    List<SocioEntity> fichas = cuentaConFichas(4);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));
    apuntar(evento.getUid(), fichas);
    UUID ganador = sorteoCarnetService.celebrarAhora(evento.getUid())
        .getParticipantes().get(0).getSocioUid();
    sorteoCarnetService.renunciar(evento.getUid(), ganador);

    SorteoCarnetDTO reabierto = sorteoCarnetService.reiniciar(evento.getUid());

    assertThat(reabierto.getParticipantes()).hasSize(3)
        .noneMatch(p -> p.getSocioUid().equals(ganador));
  }

  @Test
  @DisplayName("No se reinicia un sorteo sin celebrar ni uno con la fecha ya pasada")
  void reiniciarSoloSiTieneSentido() {
    cuentaConFichas(1);
    EventoEntity abierto = eventoConSorteo(LocalDateTime.now().plusDays(2));
    assertThatThrownBy(() -> sorteoCarnetService.reiniciar(abierto.getUid()))
        .isInstanceOf(IllegalStateException.class);

    EventoEntity celebrado = eventoConSorteo(LocalDateTime.now().plusDays(2));
    sorteoCarnetService.celebrarAhora(celebrado.getUid());
    celebrado.setFechaSorteoCarnet(LocalDateTime.now().minusHours(1));
    eventoService.save(celebrado);
    assertThatThrownBy(() -> sorteoCarnetService.reiniciar(celebrado.getUid()))
        .as("con la fecha pasada el planificador lo volvería a celebrar al momento")
        .isInstanceOf(IllegalStateException.class);
  }

  private EstadoSolicitudCarnet estadoDe(SorteoCarnetDTO sorteo, UUID socioUid) {
    return sorteo.getParticipantes().stream()
        .filter(p -> p.getSocioUid().equals(socioUid))
        .findFirst()
        .orElseThrow()
        .getEstado();
  }

  private int papeletasDe(SorteoCarnetDTO sorteo, UUID socioUid) {
    return sorteo.getMisSocios().stream()
        .filter(s -> s.getSocioUid().equals(socioUid))
        .findFirst()
        .orElseThrow()
        .getPapeletas();
  }

  private void apuntar(UUID eventoUid, List<SocioEntity> fichas) {
    SolicitudCarnetRequest request = new SolicitudCarnetRequest();
    request.setSocioUids(fichas.stream().map(SocioEntity::getUid).toList());
    // Por el mismo camino que el botón: entrar en el bombo apunta también al evento.
    eventoService.apuntarAlSorteoCarnet(eventoUid, request);
  }

  private EventoEntity eventoConSorteo(LocalDateTime fechaSorteo) {
    return eventoConSorteo(fechaSorteo, 50);
  }

  /** Evento anterior a la reserva de plazas: entrar en el bombo apunta al autobús. */
  private EventoEntity eventoSinReserva(LocalDateTime fechaSorteo, int plazas) {
    EventoEntity evento = eventoConSorteo(fechaSorteo, plazas);
    evento.setPlazasCarnetReservadas(false);
    return eventoService.save(evento);
  }

  private EventoEntity eventoConSorteo(LocalDateTime fechaSorteo, int plazas) {
    EventoEntity evento = new EventoEntity();
    evento.setNombreEvento("Partido de pruebas");
    evento.setFechaEvento(LocalDate.now().plusDays(7));
    evento.setNumeroPlazas(plazas);
    evento.setPlazasCarnet(CARNETS);
    evento.setFechaSorteoCarnet(fechaSorteo);
    return eventoService.save(evento);
  }

  /**
   * Una cuenta con varias fichas a su nombre. Todas cuelgan del mismo usuario porque el servicio
   * solo deja apuntar al bombo fichas propias, igual que en las inscripciones.
   */
  private List<SocioEntity> cuentaConFichas(int cuantas) {
    PenaEntity pena = penaDePruebas();
    UsuarioEntity usuario = new UsuarioEntity();
    usuario.setEmail(EMAIL_USUARIO);
    usuario.setPassword("no-se-usa");
    usuario.setActivo(true);
    usuario.setPena(pena);
    UsuarioEntity guardado = usuarioRepository.save(usuario);

    int siguienteNumero = socioRepository.findMaxNumeroSocio().orElse(0) + 1;
    for (int i = 0; i < cuantas; i++) {
      SocioEntity socio = new SocioEntity();
      socio.setNumeroSocio(siguienteNumero + i);
      socio.setNombre("Socio Sorteo " + (i + 1));
      socio.setFechaAlta(LocalDate.now());
      socio.setActivo(true);
      socio.setPena(pena);
      socio.setUsuario(guardado);
      guardado.getSocios().add(socioRepository.save(socio));
    }
    return usuarioRepository.save(guardado).getSocios().stream()
        .sorted((a, b) -> a.getNumeroSocio() - b.getNumeroSocio())
        .toList();
  }

  private PenaEntity penaDePruebas() {
    return penaRepository.findAll().stream().findFirst().orElseGet(() -> {
      PenaEntity pena = new PenaEntity();
      pena.setNombre("Peña de pruebas");
      pena.setSlug("pena-de-pruebas");
      return penaRepository.save(pena);
    });
  }
}

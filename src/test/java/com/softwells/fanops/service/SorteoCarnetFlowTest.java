package com.softwells.fanops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
 * bombo: que programar el evento deja la semilla comprometida, que celebrarlo reparte exactamente
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

  @Test
  @DisplayName("Programar un evento con carnets deja la semilla comprometida y el bombo abierto")
  void programarDejaElBomboAbierto() {
    cuentaConFichas(1);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));

    SorteoCarnetDTO sorteo = sorteoCarnetService.consultar(evento.getUid());

    assertThat(sorteo.isHabilitado()).isTrue();
    assertThat(sorteo.isAbierto()).isTrue();
    assertThat(sorteo.getEstado()).isEqualTo(EstadoSorteo.PROGRAMADO);
    assertThat(sorteo.getPlazasCarnet()).isEqualTo(CARNETS);
    assertThat(sorteo.getHashSemilla()).hasSize(64);
    assertThat(sorteo.getSemilla())
        .as("la semilla no se enseña antes de celebrarlo: permitiría calcular el resultado")
        .isNull();
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
    assertThat(sorteo.getSemilla())
        .as("al celebrarlo la semilla se revela, para que cualquiera pueda rehacer el sorteo")
        .isNotNull();

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
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2));

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
  @DisplayName("Entrando por el sorteo, 'solo si entramos todos' sigue valiendo para la plaza")
  void elGrupoNoSeParteAlEntrarPorElSorteo() {
    List<SocioEntity> fichas = cuentaConFichas(2);
    EventoEntity evento = eventoConSorteo(LocalDateTime.now().plusDays(2), 1);

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

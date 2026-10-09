package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.ParticipanteSorteoDTO;
import com.softwells.fanops.controller.dto.SocioSolicitudCarnetDTO;
import com.softwells.fanops.controller.dto.SorteoCarnetDTO;
import com.softwells.fanops.controller.dto.SorteoResumenDTO;
import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import com.softwells.fanops.enums.EstadoSorteo;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.SolicitudCarnetEntity;
import com.softwells.fanops.model.SorteoCarnetEntity;
import com.softwells.fanops.repository.EventoInscripcionRepository;
import com.softwells.fanops.repository.EventoRepository;
import com.softwells.fanops.repository.SolicitudCarnetRepository;
import com.softwells.fanops.repository.SorteoCarnetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sorteo de los carnets de un evento.
 *
 * <p>Es un recurso independiente de las plazas de bus: apuntarse al bombo no da plaza ni al
 * revés. El reparto tampoco sigue el mismo criterio, porque los carnets son pocos y siempre los
 * quiere más gente de la que caben: en vez de por orden de llegada se reparten por sorteo
 * ponderado, dando más papeletas a quien lleva más sorteos seguidos sin que le toque.
 *
 * <p>El resultado se calcula una sola vez, se guarda como un orden completo de extracción y no se
 * vuelve a tocar. Eso es lo que permite repetir la animación del bombo tantas veces como se
 * quiera viendo siempre lo mismo, y que una renuncia no obligue a repetir el sorteo: entra el
 * siguiente de la lista.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SorteoCarnetService {

  /** Tope de papeletas extra por participante, para que un error al teclear no decida el sorteo. */
  static final int MAX_PAPELETAS_EXTRA = 100;

  private final EventoRepository eventoRepository;
  private final SorteoCarnetRepository sorteoRepository;
  private final SolicitudCarnetRepository solicitudRepository;
  private final FichasUsuarioService fichasUsuarioService;
  private final NotificacionService notificacionService;
  private final UsuarioService usuarioService;
  private final EventoInscripcionRepository inscripcionRepository;
  private final ApplicationEventPublisher eventos;

  // ----------------------------------------------------------------
  // Programación del sorteo
  // ----------------------------------------------------------------

  /**
   * Crea o actualiza el sorteo al guardar el evento. La semilla se genera aquí, al programarlo, y
   * no se vuelve a tocar nunca: si se regenerase más tarde, publicar su hash de antemano no
   * demostraría nada.
   */
  public void sincronizar(EventoEntity evento) {
    Optional<SorteoCarnetEntity> existente = sorteoRepository.findByEventoUid(evento.getUid());

    if (!sorteaCarnets(evento)) {
      // Un sorteo ya celebrado no se borra aunque se quiten los carnets del evento: es historia,
      // y encima es la que da papeletas a quien no ganó.
      existente.filter(sorteo -> !sorteo.estaEjecutado()).ifPresent(sorteo -> {
        solicitudRepository.deleteAll(
            solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(evento.getUid()));
        sorteoRepository.delete(sorteo);
      });
      return;
    }

    SorteoCarnetEntity sorteo = existente.orElseGet(() -> {
      SorteoCarnetEntity nuevo = new SorteoCarnetEntity();
      nuevo.setEvento(evento);
      String semilla = SorteoAleatorio.nuevaSemilla();
      nuevo.setSemilla(semilla);
      nuevo.setHashSemilla(SorteoAleatorio.hash(semilla));
      return nuevo;
    });

    if (sorteo.estaEjecutado()) {
      return; // celebrado: ni la fecha ni el número de carnets cambian lo que ya salió
    }
    sorteo.setNumeroCarnets(evento.getPlazasCarnet());
    sorteo.setFechaProgramada(evento.getFechaSorteoCarnet());
    sorteoRepository.save(sorteo);
  }

  /** true si el evento tiene sorteo y ya se ha celebrado. */
  public boolean estaCelebrado(UUID eventoId) {
    return sorteoRepository.findByEventoUid(eventoId)
        .map(SorteoCarnetEntity::estaEjecutado)
        .orElse(false);
  }

  /** Borra sorteo y solicitudes de un evento que se elimina. */
  public void eliminarDeEvento(UUID eventoId) {
    solicitudRepository.deleteAll(
        solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(eventoId));
    sorteoRepository.findByEventoUid(eventoId).ifPresent(sorteoRepository::delete);
  }

  // ----------------------------------------------------------------
  // Consulta
  // ----------------------------------------------------------------

  /** Estado del bombo de un evento, celebrándolo antes si ya se le había pasado la hora. */
  public SorteoCarnetDTO consultar(UUID eventoId) {
    EventoEntity evento = findEvento(eventoId);
    if (sorteoRepository.findByEventoUid(eventoId).isPresent()) {
      celebrarSiVencido(eventoId);
    }
    return construirDto(evento);
  }

  // ----------------------------------------------------------------
  // Solicitudes
  // ----------------------------------------------------------------

  /**
   * Mete en el bombo las fichas indicadas, que ya vienen validadas como propias.
   *
   * <p>No apunta al evento: de eso se encarga
   * {@link EventoService#apuntarAlSorteoCarnet}, que es quien llama aquí. Las dos cosas pasan en
   * la misma transacción, así que si una falla no queda a medias.
   */
  public SorteoCarnetDTO solicitar(UUID eventoId, List<SocioEntity> fichas) {
    EventoEntity evento = findEvento(eventoId);
    sorteoAbierto(eventoId);

    for (SocioEntity socio : fichas) {
      if (!socio.isActivo()) {
        throw new IllegalStateException(
            socio.getNombre() + " no está de alta como socio, no puede entrar en el sorteo.");
      }
      if (solicitudRepository.existsByEventoUidAndSocioUid(eventoId, socio.getUid())) {
        throw new IllegalStateException(socio.getNombre() + " ya está apuntado al sorteo.");
      }
    }

    for (SocioEntity socio : fichas) {
      SolicitudCarnetEntity solicitud = new SolicitudCarnetEntity();
      solicitud.setEvento(evento);
      solicitud.setSocio(socio);
      solicitud.setFechaSolicitud(LocalDateTime.now());
      solicitud.setEstado(EstadoSolicitudCarnet.PENDIENTE);
      solicitudRepository.save(solicitud);
    }
    log.info("{} fichas apuntadas al sorteo de carnets del evento {}", fichas.size(), eventoId);

    return construirDto(evento);
  }

  /**
   * Mete en el bombo a alguien que no es socio (enlace público). Solo si el evento lo permite.
   * No lleva historial ni papeletas extra: entra siempre con una.
   */
  public void solicitarInvitado(UUID eventoId, String nombre, String email, String telefono) {
    EventoEntity evento = findEvento(eventoId);
    sorteoAbierto(eventoId);
    if (!evento.sorteoAbierto()) {
      throw new IllegalStateException("El sorteo de carnets de este evento es solo para socios. "
          + "Usa el email de tu ficha o apúntate solo al evento.");
    }
    if (solicitudRepository.existsByEventoUidAndEmailInvitadoIgnoreCase(eventoId, email)) {
      throw new IllegalStateException("Ya estás apuntado al sorteo con ese email.");
    }
    SolicitudCarnetEntity solicitud = new SolicitudCarnetEntity();
    solicitud.setEvento(evento);
    solicitud.setNombreInvitado(nombre);
    solicitud.setEmailInvitado(email);
    solicitud.setTelefonoInvitado(telefono);
    solicitud.setFechaSolicitud(LocalDateTime.now());
    solicitud.setEstado(EstadoSolicitudCarnet.PENDIENTE);
    solicitudRepository.save(solicitud);
    log.info("Invitado apuntado al sorteo de carnets del evento {}", eventoId);
  }

  /**
   * Entradas al bombo de quien no es socio hechas con ese correo, para enlazarlas a su ficha si
   * resulta que lo es. Solo las de sorteos que todavía no se han celebrado.
   */
  public List<SolicitudCarnetEntity> invitadosPendientesCon(String email) {
    return solicitudRepository.findByEmailInvitadoIgnoreCaseAndSocioIsNull(email).stream()
        .filter(s -> !estaCelebrado(s.getEvento().getUid()))
        .collect(Collectors.toList());
  }

  /** Entradas al bombo de no socios de un evento, si su sorteo todavía no se ha celebrado. */
  public List<SolicitudCarnetEntity> invitadosPendientesDe(UUID eventoId) {
    if (estaCelebrado(eventoId)) {
      return List.of();
    }
    return solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(eventoId).stream()
        .filter(SolicitudCarnetEntity::esInvitado)
        .collect(Collectors.toList());
  }

  /** Convierte la entrada de un invitado en la de su ficha de socio (con su historial). */
  public void vincularInvitado(SolicitudCarnetEntity solicitud, SocioEntity socio) {
    UUID eventoId = solicitud.getEvento().getUid();
    if (solicitudRepository.existsByEventoUidAndSocioUid(eventoId, socio.getUid())) {
      return; // la ficha ya está en el bombo: no se duplica
    }
    solicitud.setSocio(socio);
    solicitud.setNombreInvitado(null);
    solicitud.setEmailInvitado(null);
    solicitud.setTelefonoInvitado(null);
    solicitudRepository.save(solicitud);
  }

  /** Saca del bombo una ficha del usuario. Solo antes de celebrarse el sorteo. */
  public SorteoCarnetDTO anularSolicitud(UUID eventoId, UUID socioUid) {
    EventoEntity evento = findEvento(eventoId);
    sorteoAbierto(eventoId);
    SocioEntity socio = fichaPropia(socioUid);
    SolicitudCarnetEntity solicitud =
        solicitudRepository.findByEventoUidAndSocioUid(eventoId, socio.getUid())
            .orElseThrow(() -> new EntityNotFoundException(
                socio.getNombre() + " no está apuntado al sorteo."));
    // Si estaba aceptado a la espera del sorteo, al salir se queda con su plaza como una
    // inscripción normal (deja de ser "del sorteo"): no se le quita en silencio.
    inscripcionDe(eventoId, solicitud).filter(i -> Boolean.TRUE.equals(i.getOrigenSorteo()))
        .ifPresent(i -> {
          i.setOrigenSorteo(null);
          inscripcionRepository.save(i);
        });
    solicitudRepository.delete(solicitud);
    return construirDto(evento);
  }

  /**
   * Devuelve un carnet que había tocado. El hueco no se vuelve a sortear: pasa al primer suplente
   * por orden de extracción, que es justo para lo que se vacía el bombo entero.
   */
  public SorteoCarnetDTO renunciar(UUID eventoId, UUID socioUid) {
    EventoEntity evento = findEvento(eventoId);
    SocioEntity socio = fichaPropia(socioUid);
    SolicitudCarnetEntity solicitud =
        solicitudRepository.findByEventoUidAndSocioUid(eventoId, socio.getUid())
            .orElseThrow(() -> new EntityNotFoundException(
                socio.getNombre() + " no está apuntado al sorteo."));
    if (solicitud.getEstado() != EstadoSolicitudCarnet.GANADORA) {
      throw new IllegalStateException("Solo se puede renunciar a un carnet que te ha tocado.");
    }

    solicitud.setEstado(EstadoSolicitudCarnet.RENUNCIADA);
    solicitudRepository.save(solicitud);
    if (evento.reservaCarnet()) {
      // Sin carnet no hay autobús: su plaza pasa al suplente, no se queda libre para la espera.
      inscripcionRepository.findByEventoUidAndSocioUid(eventoId, socio.getUid())
          .ifPresent(inscripcionRepository::delete);
    }

    solicitudRepository
        .findByEventoUidAndEstadoOrderByPosicionSorteoAsc(eventoId, EstadoSolicitudCarnet.SUPLENTE)
        .stream()
        .findFirst()
        .ifPresent(suplente -> {
          suplente.setEstado(EstadoSolicitudCarnet.GANADORA);
          solicitudRepository.save(suplente);
          if (evento.reservaCarnet()) {
            darPlazaDeAutobus(evento, suplente);
          }
          notificacionService.enviarCarnetPorRenuncia(suplente, evento);
        });

    return construirDto(evento);
  }

  /**
   * Fija las papeletas extra de un participante (acción de gestión). Se suman a las del historial
   * y se publican junto al total, así que el sorteo sigue siendo comprobable desde fuera.
   *
   * <p>Solo mientras el sorteo no se ha celebrado, y solo si la peña del socio tiene la opción
   * activada y es la misma que la de quien gestiona: un admin no puede tocar el bombo de otra.
   *
   * @param papeletasExtra papeletas a sumar, entre 0 y {@value #MAX_PAPELETAS_EXTRA}; 0 las quita
   */
  public SorteoCarnetDTO ajustarPapeletasExtra(UUID eventoId, UUID socioUid, int papeletasExtra) {
    if (papeletasExtra < 0 || papeletasExtra > MAX_PAPELETAS_EXTRA) {
      throw new IllegalArgumentException(
          "Las papeletas extra tienen que estar entre 0 y " + MAX_PAPELETAS_EXTRA + ".");
    }
    EventoEntity evento = findEvento(eventoId);
    // Si ya le tocaba celebrarse, se celebra antes: ajustar un bombo que debería estar vacío
    // cambiaría un resultado que en rigor ya existe.
    celebrarSiVencido(eventoId);
    SorteoCarnetEntity sorteo = sorteoRepository.findByEventoUid(eventoId)
        .orElseThrow(() -> new EntityNotFoundException("Este evento no sortea carnets."));
    if (sorteo.estaEjecutado()) {
      throw new IllegalStateException(
          "El sorteo ya se ha celebrado: sus papeletas no se pueden cambiar.");
    }

    SolicitudCarnetEntity solicitud = solicitudRepository.findByEventoUidAndSocioUid(eventoId,
            socioUid)
        .orElseThrow(() -> new EntityNotFoundException("Ese socio no está en el bombo."));
    PenaEntity penaSocio = solicitud.getSocio().getPena();
    PenaEntity miPena = usuarioService.obtenerPenaDelUsuarioAutenticado();
    if (penaSocio == null || !penaSocio.getId().equals(miPena.getId())) {
      throw new IllegalStateException("Ese socio no pertenece a tu peña.");
    }
    if (!penaSocio.permitePapeletasExtraSorteo()) {
      throw new IllegalStateException(
          "Las papeletas extra no están activadas para esta peña.");
    }

    solicitud.setPapeletasExtra(papeletasExtra == 0 ? null : papeletasExtra);
    solicitudRepository.save(solicitud);
    log.info("Papeletas extra de {} en el sorteo del evento {}: {}",
        solicitud.getSocio().getNombre(), eventoId, papeletasExtra);
    return construirDto(evento);
  }

  // ----------------------------------------------------------------
  // Celebración
  // ----------------------------------------------------------------

  /** Celebra el sorteo antes de tiempo (acción de administración). */
  public SorteoCarnetDTO celebrarAhora(UUID eventoId) {
    EventoEntity evento = findEvento(eventoId);
    celebrar(eventoId, true);
    return construirDto(evento);
  }

  /**
   * Deshace un sorteo ya celebrado y vuelve a dejar el bombo abierto (acción de gestión), por
   * ejemplo si se celebró antes de tiempo o hay que corregir quién estaba dentro.
   *
   * <p>Se conserva la <strong>misma semilla</strong>: con los mismos participantes y papeletas,
   * volver a celebrarlo da exactamente el mismo resultado, así que reiniciar no sirve para repetir
   * el sorteo hasta que salga otro reparto. Lo que cambie el resultado tiene que ser un cambio en
   * el bombo (alguien que entra o sale, o unas papeletas).
   *
   * <p>Quien renunció a su carnet sale del bombo: ya dijo que no lo quería. Al resto se le borra
   * la posición y vuelve a quedar pendiente; como el historial de papeletas solo cuenta sorteos
   * celebrados, este deja de contar hasta que se vuelva a celebrar.
   *
   * <p>La fecha del sorteo del evento tiene que ser futura: con una pasada, el planificador lo
   * volvería a celebrar en menos de un minuto.
   */
  public SorteoCarnetDTO reiniciar(UUID eventoId) {
    EventoEntity evento = findEvento(eventoId);
    SorteoCarnetEntity sorteo = sorteoRepository.findByEventoUidParaEjecutar(eventoId)
        .orElseThrow(() -> new EntityNotFoundException("Este evento no sortea carnets."));
    if (!sorteo.estaEjecutado()) {
      throw new IllegalStateException(
          "El sorteo todavía no se ha celebrado: no hay nada que reiniciar.");
    }
    if (!sorteaCarnets(evento) || !evento.getFechaSorteoCarnet().isAfter(LocalDateTime.now())) {
      throw new IllegalStateException("Para reiniciar el sorteo, pon antes en el evento una fecha "
          + "de sorteo futura: con la de ahora se volvería a celebrar al momento.");
    }

    for (SolicitudCarnetEntity solicitud :
        solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(eventoId)) {
      if (solicitud.getEstado() == EstadoSolicitudCarnet.RENUNCIADA) {
        solicitudRepository.delete(solicitud);
        continue;
      }
      solicitud.setEstado(EstadoSolicitudCarnet.PENDIENTE);
      solicitud.setPosicionSorteo(null);
      solicitud.setPesoSorteo(null);
      solicitudRepository.save(solicitud);
    }

    if (evento.reservaCarnet()) {
      // Las plazas que dio el sorteo vuelven a quedar reservadas hasta que se repita.
      inscripcionRepository.deleteAll(inscripcionRepository.findByEventoUidAndOrigenSorteoTrue(eventoId));
    }

    sorteo.setEstado(EstadoSorteo.PROGRAMADO);
    sorteo.setFechaEjecucion(null);
    sorteo.setFechaProgramada(evento.getFechaSorteoCarnet());
    sorteo.setNumeroCarnets(evento.getPlazasCarnet());
    sorteoRepository.save(sorteo);
    log.info("Sorteo de carnets reiniciado para el evento {}; se celebrará el {}", eventoId,
        sorteo.getFechaProgramada());

    return construirDto(evento);
  }

  /** Eventos cuyo sorteo ya debería estar celebrado. Lo consulta el planificador. */
  @Transactional(readOnly = true)
  public List<UUID> eventosConSorteoVencido() {
    return sorteoRepository
        .findByEstadoAndFechaProgramadaLessThanEqual(EstadoSorteo.PROGRAMADO, LocalDateTime.now())
        .stream()
        .map(sorteo -> sorteo.getEvento().getUid())
        .collect(Collectors.toList());
  }

  /** Celebra el sorteo si ya se le ha pasado la hora; si no, no hace nada. */
  public void celebrarSiVencido(UUID eventoId) {
    celebrar(eventoId, false);
  }

  /**
   * Vacía el bombo y guarda el orden de extracción.
   *
   * <p>La fila del sorteo se coge bloqueada: el planificador y un admin que lo adelanta pueden
   * coincidir en el mismo instante, y sin el bloqueo se celebraría dos veces.
   */
  private void celebrar(UUID eventoId, boolean forzado) {
    SorteoCarnetEntity sorteo = sorteoRepository.findByEventoUidParaEjecutar(eventoId)
        .orElseThrow(() -> new EntityNotFoundException("Este evento no sortea carnets."));
    if (sorteo.estaEjecutado()) {
      return;
    }
    if (!forzado && LocalDateTime.now().isBefore(sorteo.getFechaProgramada())) {
      return;
    }

    List<SolicitudCarnetEntity> participantes = participantesEnOrdenEstable(eventoId);
    participantes.forEach(solicitud -> solicitud.setPesoSorteo(papeletasTotales(solicitud)));

    List<SolicitudCarnetEntity> extraidos = SorteoAleatorio.extraer(participantes,
        SolicitudCarnetEntity::getPesoSorteo, sorteo.getSemilla());

    for (int i = 0; i < extraidos.size(); i++) {
      SolicitudCarnetEntity solicitud = extraidos.get(i);
      solicitud.setPosicionSorteo(i + 1);
      solicitud.setEstado(i < sorteo.getNumeroCarnets()
          ? EstadoSolicitudCarnet.GANADORA
          : EstadoSolicitudCarnet.SUPLENTE);
      solicitudRepository.save(solicitud);
    }

    sorteo.setEstado(EstadoSorteo.EJECUTADO);
    sorteo.setFechaEjecucion(LocalDateTime.now());
    sorteoRepository.save(sorteo);

    EventoEntity evento = findEvento(eventoId);
    if (evento.reservaCarnet()) {
      extraidos.stream().filter(s -> s.getEstado() == EstadoSolicitudCarnet.GANADORA)
          .forEach(ganadora -> darPlazaDeAutobus(evento, ganadora));
      // Quien estaba aceptado a la espera del sorteo y no ha ganado se queda sin plaza.
      extraidos.stream().filter(s -> s.getEstado() != EstadoSolicitudCarnet.GANADORA)
          .forEach(perdedora -> inscripcionDe(eventoId, perdedora)
              .filter(i -> Boolean.TRUE.equals(i.getOrigenSorteo()))
              .ifPresent(inscripcionRepository::delete));
      // Las reservadas que no hacen falta (menos participantes que carnets, o ganadores que ya
      // tenían plaza) pasan a la lista de espera.
      eventos.publishEvent(new SorteoCelebradoEvent(eventoId));
    }
    log.info("Sorteo de carnets celebrado para el evento {} ({} participantes, {} carnets)",
        eventoId, extraidos.size(), sorteo.getNumeroCarnets());
    notificacionService.enviarResultadoSorteoCarnet(extraidos, evento, sorteo.getNumeroCarnets());
  }

  /**
   * Un evento con gente ya en el bombo pasa a reservar las plazas de los carnets: quien está
   * dentro y tiene inscripción se queda apuntado, pero esa inscripción se marca como del sorteo,
   * así que deja de ocupar plaza hasta que gane y desaparece si no gana. No se puede saber quién
   * se apuntó al autobús antes por su cuenta, así que también a esos les alcanza. Solo vale con el
   * sorteo sin celebrar.
   */
  public void liberarPlazasDeQuienEstaEnElBombo(EventoEntity evento) {
    if (estaCelebrado(evento.getUid())) {
      return;
    }
    for (SolicitudCarnetEntity solicitud :
        solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(evento.getUid())) {
      inscripcionDe(evento.getUid(), solicitud).ifPresent(inscripcion -> {
        inscripcion.setOrigenSorteo(true);
        inscripcionRepository.save(inscripcion);
      });
    }
  }

  private Optional<EventoInscripcionEntity> inscripcionDe(UUID eventoId,
      SolicitudCarnetEntity solicitud) {
    return solicitud.esInvitado()
        ? inscripcionRepository.findFirstByEventoUidAndEmailIgnoreCaseAndSocioIsNull(eventoId,
            solicitud.getEmailInvitado())
        : inscripcionRepository.findByEventoUidAndSocioUid(eventoId,
            solicitud.getSocio().getUid());
  }

  /**
   * Plaza de autobús de quien consigue carnet en un evento que las reserva. Si ya estaba
   * inscrito se queda con lo que tenía (y pasa de lista de espera a confirmada); si no, se le crea
   * la inscripción, marcada para poder deshacerla al reiniciar el sorteo.
   */
  private void darPlazaDeAutobus(EventoEntity evento, SolicitudCarnetEntity solicitud) {
    UUID eventoId = evento.getUid();
    Optional<EventoInscripcionEntity> existente = inscripcionDe(eventoId, solicitud);
    if (existente.isPresent()) {
      EventoInscripcionEntity inscripcion = existente.get();
      if (inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA) {
        inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
        inscripcionRepository.save(inscripcion);
      }
      return;
    }
    EventoInscripcionEntity inscripcion = new EventoInscripcionEntity();
    inscripcion.setEvento(evento);
    inscripcion.setSocio(solicitud.getSocio());
    inscripcion.setNombre(solicitud.nombreParticipante());
    inscripcion.setEmail(solicitud.emailParticipante());
    inscripcion.setTelefono(solicitud.telefonoParticipante());
    inscripcion.setFechaInscripcion(LocalDateTime.now());
    inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
    inscripcion.setSocioPrioritario(false);
    inscripcion.setOrigenSorteo(true);
    inscripcionRepository.save(inscripcion);
  }

  /**
   * Participantes en un orden fijado de antemano. El resultado depende de este orden, así que no
   * puede quedar al criterio de la base de datos: se ordena por fecha de solicitud y se desempata
   * por uid, que es estable y no lo controla nadie.
   */
  private List<SolicitudCarnetEntity> participantesEnOrdenEstable(UUID eventoId) {
    return solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(eventoId).stream()
        .filter(solicitud -> solicitud.getEstado() == EstadoSolicitudCarnet.PENDIENTE)
        .sorted(Comparator.comparing(SolicitudCarnetEntity::getFechaSolicitud)
            .thenComparing(solicitud -> solicitud.getUid().toString()))
        .collect(Collectors.toList());
  }

  /**
   * Papeletas de un socio: una de salida más otra por cada sorteo en el que se ha quedado sin
   * carnet desde la última vez que le tocó. Así el que nunca tiene suerte va acumulando ventaja y
   * el que acaba de llevárselo vuelve al mínimo, sin que eso llegue a excluir a nadie.
   */
  private int papeletasDe(UUID socioUid) {
    int sinPremio = 0;
    for (SolicitudCarnetEntity participacion : solicitudRepository.historialSorteosDe(socioUid)) {
      sinPremio = participacion.fuePremiada() ? 0 : sinPremio + 1;
    }
    return 1 + sinPremio;
  }

  /** Papeletas con las que entra una solicitud: las del historial más las extra que cuenten. */
  private int papeletasTotales(SolicitudCarnetEntity solicitud) {
    return solicitud.esInvitado()
        ? 1
        : papeletasDe(solicitud.getSocio().getUid()) + papeletasExtraEfectivas(solicitud);
  }

  /**
   * Papeletas extra que de verdad cuentan: si la peña desactiva la opción, las que se hubieran
   * puesto dejan de sumar sin necesidad de ir borrándolas una a una.
   */
  private int papeletasExtraEfectivas(SolicitudCarnetEntity solicitud) {
    if (solicitud.esInvitado()) {
      return 0;
    }
    PenaEntity pena = solicitud.getSocio().getPena();
    Integer extra = solicitud.getPapeletasExtra();
    return pena != null && pena.permitePapeletasExtraSorteo() && extra != null ? extra : 0;
  }

  /** true si quien consulta puede ajustar papeletas extra en un sorteo sin celebrar. */
  private boolean ajustePapeletasPermitido(SorteoCarnetEntity sorteo) {
    if (sorteo.estaEjecutado()) {
      return false;
    }
    try {
      return usuarioService.obtenerPenaDelUsuarioAutenticado().permitePapeletasExtraSorteo();
    } catch (RuntimeException sinPena) {
      // Un superadmin sin peña seleccionada o un usuario sin peña: no hay nada que ajustar.
      return false;
    }
  }

  // ----------------------------------------------------------------
  // Construcción de la vista
  // ----------------------------------------------------------------

  private SorteoCarnetDTO construirDto(EventoEntity evento) {
    SorteoCarnetDTO.SorteoCarnetDTOBuilder dto = SorteoCarnetDTO.builder()
        .eventoUid(evento.getUid())
        .nombreEvento(evento.getNombreEvento());

    SorteoCarnetEntity sorteo = sorteoRepository.findByEventoUid(evento.getUid()).orElse(null);
    if (sorteo == null) {
      return dto.habilitado(false).participantes(List.of()).misSocios(List.of()).build();
    }

    List<SolicitudCarnetEntity> solicitudes =
        new ArrayList<>(solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(
            evento.getUid()));
    if (sorteo.estaEjecutado()) {
      solicitudes.sort(Comparator.comparing(SolicitudCarnetEntity::getPosicionSorteo,
          Comparator.nullsLast(Comparator.naturalOrder())));
    }

    Map<UUID, SolicitudCarnetEntity> porSocio = new LinkedHashMap<>();
    solicitudes.stream().filter(solicitud -> !solicitud.esInvitado())
        .forEach(solicitud -> porSocio.put(solicitud.getSocio().getUid(), solicitud));

    List<SocioEntity> misFichas = fichasUsuarioService.misFichasOVacio();
    List<UUID> uidsPropios = misFichas.stream().map(SocioEntity::getUid).collect(
        Collectors.toList());

    List<ParticipanteSorteoDTO> participantes = solicitudes.stream()
        .map(solicitud -> ParticipanteSorteoDTO.builder()
            // Un invitado no tiene ficha: su uid de solicitud hace de clave en la lista.
            .socioUid(solicitud.esInvitado() ? solicitud.getUid() : solicitud.getSocio().getUid())
            .invitado(solicitud.esInvitado())
            .numeroSocio(solicitud.esInvitado() ? null : solicitud.getSocio().getNumeroSocio())
            .nombre(solicitud.nombreParticipante())
            .papeletas(solicitud.getPesoSorteo() != null
                ? solicitud.getPesoSorteo()
                : papeletasTotales(solicitud))
            .papeletasExtra(papeletasExtraEfectivas(solicitud))
            .posicion(solicitud.getPosicionSorteo())
            .estado(solicitud.getEstado())
            .propio(!solicitud.esInvitado()
                && uidsPropios.contains(solicitud.getSocio().getUid()))
            .build())
        .collect(Collectors.toList());

    List<SocioSolicitudCarnetDTO> misSocios = misFichas.stream()
        .map(socio -> {
          SolicitudCarnetEntity solicitud = porSocio.get(socio.getUid());
          return SocioSolicitudCarnetDTO.builder()
              .socioUid(socio.getUid())
              .numeroSocio(socio.getNumeroSocio())
              .nombre(socio.getNombre())
              .estado(solicitud != null ? solicitud.getEstado() : null)
              .posicion(solicitud != null ? solicitud.getPosicionSorteo() : null)
              .papeletas(solicitud == null ? papeletasDe(socio.getUid())
                  : solicitud.getPesoSorteo() != null ? solicitud.getPesoSorteo()
                  : papeletasTotales(solicitud))
              .build();
        })
        .collect(Collectors.toList());

    return dto.habilitado(true)
        .plazasCarnet(sorteo.getNumeroCarnets())
        .costeCarnet(evento.getCosteCarnet())
        .fechaProgramada(sorteo.getFechaProgramada())
        .fechaEjecucion(sorteo.getFechaEjecucion())
        .estado(sorteo.getEstado())
        .abierto(!sorteo.estaEjecutado())
        .admiteSolicitudes(admiteSolicitudes(evento, sorteo))
        .plazaSoloSiGana(evento.reservaCarnet())
        .ajustePapeletasPermitido(ajustePapeletasPermitido(sorteo))
        // La semilla y su huella no salen de aquí: la peña prefirió no publicarlas. Con la semilla
        // a la vista, sobre todo tras reiniciar un sorteo, se podría calcular el resultado según
        // quién entrase en el bombo.
        .participantes(participantes)
        .misSocios(misSocios)
        .build();
  }

  // ----------------------------------------------------------------
  // Utilidades
  // ----------------------------------------------------------------

  /**
   * Resumen para la tarjeta de un evento. Recibe las fichas ya resueltas porque el listado pinta
   * muchos eventos y volver a buscarlas en cada uno sería una consulta por tarjeta.
   */
  public SorteoResumenDTO resumen(EventoEntity evento, List<SocioEntity> misFichas) {
    SorteoCarnetEntity sorteo = sorteoRepository.findByEventoUid(evento.getUid()).orElse(null);
    if (sorteo == null) {
      return null; // el evento no sortea carnets: la tarjeta no pinta nada
    }

    List<SolicitudCarnetEntity> solicitudes =
        solicitudRepository.findByEventoUidOrderByFechaSolicitudAsc(evento.getUid());
    Map<UUID, SolicitudCarnetEntity> porSocio = new LinkedHashMap<>();
    solicitudes.stream().filter(solicitud -> !solicitud.esInvitado())
        .forEach(solicitud -> porSocio.put(solicitud.getSocio().getUid(), solicitud));

    List<SocioSolicitudCarnetDTO> misSocios = misFichas.stream()
        .map(socio -> {
          SolicitudCarnetEntity solicitud = porSocio.get(socio.getUid());
          return SocioSolicitudCarnetDTO.builder()
              .socioUid(socio.getUid())
              .numeroSocio(socio.getNumeroSocio())
              .nombre(socio.getNombre())
              .estado(solicitud != null ? solicitud.getEstado() : null)
              .posicion(solicitud != null ? solicitud.getPosicionSorteo() : null)
              // Las papeletas no se calculan aquí: son una consulta por ficha y por evento, y
              // en la tarjeta no se enseñan. Salen al abrir el bombo.
              .papeletas(solicitud != null && solicitud.getPesoSorteo() != null
                  ? solicitud.getPesoSorteo()
                  : 0)
              .build();
        })
        .collect(Collectors.toList());

    return SorteoResumenDTO.builder()
        .plazasCarnet(sorteo.getNumeroCarnets())
        .fechaProgramada(sorteo.getFechaProgramada())
        .estado(sorteo.getEstado())
        .admiteSolicitudes(admiteSolicitudes(evento, sorteo))
        .abiertoATodos(evento.sorteoAbierto())
        .plazaSoloSiGana(evento.reservaCarnet())
        .participantes(solicitudes.size())
        .misSocios(misSocios)
        .build();
  }

  /**
   * true si todavía se puede entrar en el bombo. Mira también el plazo del evento porque entrar
   * al sorteo apunta al evento: con la inscripción cerrada, el bombo no puede admitir a nadie
   * más aunque su fecha no haya llegado.
   */
  private boolean admiteSolicitudes(EventoEntity evento, SorteoCarnetEntity sorteo) {
    return !sorteo.estaEjecutado()
        && LocalDateTime.now().isBefore(sorteo.getFechaProgramada())
        && !evento.plazoInscripcionCerrado();
  }

  private boolean sorteaCarnets(EventoEntity evento) {
    return evento.getPlazasCarnet() != null && evento.getPlazasCarnet() > 0
        && evento.getFechaSorteoCarnet() != null;
  }

  private SorteoCarnetEntity sorteoAbierto(UUID eventoId) {
    SorteoCarnetEntity sorteo = sorteoRepository.findByEventoUid(eventoId)
        .orElseThrow(() -> new EntityNotFoundException("Este evento no sortea carnets."));
    if (sorteo.estaEjecutado()) {
      throw new IllegalStateException("El sorteo ya se ha celebrado.");
    }
    if (LocalDateTime.now().isAfter(sorteo.getFechaProgramada())) {
      throw new IllegalStateException("El plazo para entrar en el sorteo ya está cerrado.");
    }
    return sorteo;
  }

  private SocioEntity fichaPropia(UUID socioUid) {
    return fichasUsuarioService
        .resolver(socioUid != null ? List.of(socioUid) : null, "sacar del sorteo")
        .get(0);
  }

  private EventoEntity findEvento(UUID eventoId) {
    return eventoRepository.findById(eventoId)
        .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id: " + eventoId));
  }
}

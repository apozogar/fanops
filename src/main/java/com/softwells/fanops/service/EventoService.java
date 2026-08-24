package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.FaltaEventoDTO;
import com.softwells.fanops.controller.dto.HistorialEventoSocioDto;
import com.softwells.fanops.controller.dto.HistorialSocioDto;
import com.softwells.fanops.controller.dto.InscripcionAdminDTO;
import com.softwells.fanops.controller.dto.InscripcionPublicaRequest;
import com.softwells.fanops.controller.dto.InscripcionSocioRequest;
import com.softwells.fanops.controller.dto.SocioInscripcionDTO;
import com.softwells.fanops.enums.AsistenciaEvento;
import com.softwells.fanops.enums.EstadoCuota;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.enums.MotivoFalta;
import com.softwells.fanops.mapper.EventoMapper;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.FaltaEventoEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.CuotaRepository;
import com.softwells.fanops.repository.EventoInscripcionRepository;
import com.softwells.fanops.repository.EventoRepository;
import com.softwells.fanops.repository.FaltaEventoRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventoService {

  private final EventoRepository eventoRepository;
  private final EventoInscripcionRepository inscripcionRepository;
  private final UsuarioRepository usuarioRepository;
  private final CuotaRepository cuotaRepository;
  private final FaltaEventoRepository faltaRepository;
  private final SocioRepository socioRepository;
  private final NotificacionService notificacionService;

  /** Penalización por falta si la peña no la tiene configurada. */
  private static final int PENALIZACION_POR_FALTA_DEFECTO = 1;

  // ----------------------------------------------------------------
  // Consultas
  // ----------------------------------------------------------------

  public List<EventoEntity> findAllForAdmin() {
    return eventoRepository.findAll().stream()
        .sorted(Comparator.comparing(EventoEntity::getFechaEvento).reversed())
        .peek(this::completarInfoAdmin)
        .collect(Collectors.toList());
  }

  private void completarInfoAdmin(EventoEntity evento) {
    long confirmadas = inscripcionRepository.countByEventoUidAndEstado(evento.getUid(),
        EstadoInscripcion.CONFIRMADA);
    long espera = inscripcionRepository.countByEventoUidAndEstado(evento.getUid(),
        EstadoInscripcion.EN_ESPERA);
    evento.setNumInscritos((int) confirmadas);
    evento.setNumEnEspera((int) espera);
    evento.setPlazasLibres(calcularPlazasLibres(evento, (int) confirmadas));
    evento.setInscripcionCerrada(inscripcionCerrada(evento));
  }

  public List<EventoInscripcionDTO> findAllForInscripcion() {
    List<SocioEntity> misSocios = sociosDelUsuarioAutenticado();

    return eventoRepository.findAll().stream()
        .map(evento -> EventoMapper.toInscripcionDTO(evento, completarInfoUsuario(evento,
            misSocios)))
        .sorted(Comparator.comparing(EventoInscripcionDTO::getFechaEvento))
        .collect(Collectors.toList());
  }

  /**
   * Rellena los contadores del evento y devuelve el estado de cada ficha de socio del usuario.
   * En un multicarnet hay una entrada por persona, inscrita o no, para que el usuario vea a
   * quién está apuntando en lugar de operar sobre un "socio principal" indeterminado.
   */
  private List<SocioInscripcionDTO> completarInfoUsuario(EventoEntity evento,
      List<SocioEntity> misSocios) {
    List<EventoInscripcionEntity> inscripciones =
        inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid());

    Map<UUID, EstadoInscripcion> estadoPorSocio = new LinkedHashMap<>();
    for (EventoInscripcionEntity inscripcion : inscripciones) {
      if (inscripcion.getSocio() != null) {
        estadoPorSocio.put(inscripcion.getSocio().getUid(), inscripcion.getEstado());
      }
    }

    List<SocioInscripcionDTO> estadoMisSocios = misSocios.stream()
        .map(socio -> SocioInscripcionDTO.builder()
            .socioUid(socio.getUid())
            .numeroSocio(socio.getNumeroSocio())
            .nombre(socio.getNombre())
            .estado(estadoPorSocio.get(socio.getUid()))
            .build())
        .collect(Collectors.toList());

    evento.setCurrentUserInscrito(estadoMisSocios.stream().anyMatch(s -> s.getEstado() != null));
    evento.setNumInscritos((int) inscripciones.stream()
        .filter(i -> i.getEstado() == EstadoInscripcion.CONFIRMADA).count());
    evento.setNumEnEspera((int) inscripciones.stream()
        .filter(i -> i.getEstado() == EstadoInscripcion.EN_ESPERA).count());
    evento.setPlazasLibres(calcularPlazasLibres(evento, evento.getNumInscritos()));
    evento.setInscripcionCerrada(inscripcionCerrada(evento));
    return estadoMisSocios;
  }

  /** Información pública de un evento para el formulario de inscripción de no socios. */
  public EventoInscripcionDTO infoPublica(UUID eventoId) {
    EventoEntity evento = findEvento(eventoId);
    completarInfoAdmin(evento);
    return EventoMapper.toInscripcionDTO(evento);
  }

  public List<InscripcionAdminDTO> getInscripciones(UUID eventoId) {
    // Las faltas de este evento se traen de una vez: son pocas y evita una consulta por inscrito.
    Map<UUID, FaltaEventoEntity> faltasDelEvento = new LinkedHashMap<>();
    for (FaltaEventoEntity falta : faltaRepository.findByEventoUid(eventoId)) {
      faltasDelEvento.putIfAbsent(falta.getSocio().getUid(), falta);
    }

    return inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(eventoId).stream()
        .map(inscripcion -> {
          InscripcionAdminDTO dto = EventoMapper.toInscripcionAdminDTO(inscripcion);
          SocioEntity socio = inscripcion.getSocio();
          if (socio != null) {
            dto.setFaltasAcumuladas(faltasDe(socio));
            dto.setPenalizacionesPendientes(penalizacionesPendientesDe(socio));
            FaltaEventoEntity falta = faltasDelEvento.get(socio.getUid());
            dto.setFaltaUid(falta != null ? falta.getUid() : null);
          }
          return dto;
        })
        .collect(Collectors.toList());
  }

  // ----------------------------------------------------------------
  // CRUD de eventos
  // ----------------------------------------------------------------

  public EventoEntity save(EventoEntity evento) {
    return eventoRepository.save(evento);
  }

  /**
   * Actualiza el evento. Si al ampliar (o quitar) el límite de plazas quedan huecos libres, se
   * promociona automáticamente a la lista de espera y se avisa a quien consigue plaza.
   */
  public EventoEntity update(UUID id, EventoEntity eventoDetails) {
    EventoEntity eventoExistente = findEvento(id);
    Integer plazasAnteriores = eventoExistente.getNumeroPlazas();

    eventoExistente.setNombreEvento(eventoDetails.getNombreEvento());
    eventoExistente.setFechaEvento(eventoDetails.getFechaEvento());
    eventoExistente.setFechaLimiteInscripcion(eventoDetails.getFechaLimiteInscripcion());
    eventoExistente.setUbicacion(eventoDetails.getUbicacion());
    eventoExistente.setDescripcion(eventoDetails.getDescripcion());
    eventoExistente.setNumeroPlazas(eventoDetails.getNumeroPlazas());
    eventoExistente.setCosteTotalEstimado(eventoDetails.getCosteTotalEstimado());
    eventoExistente.setCosteTotalReal(eventoDetails.getCosteTotalReal());

    EventoEntity guardado = eventoRepository.save(eventoExistente);

    if (seAmplioElAforo(plazasAnteriores, guardado.getNumeroPlazas())) {
      promoverListaEspera(id);
    }
    return guardado;
  }

  /** Cierto si el nuevo aforo deja más hueco que el anterior (incluido pasar a ilimitado). */
  private boolean seAmplioElAforo(Integer anterior, Integer nuevo) {
    if (anterior == null) {
      return false; // antes era ilimitado: no puede haber más hueco que antes
    }
    return nuevo == null || nuevo > anterior;
  }

  public void delete(UUID id) {
    eventoRepository.deleteById(id);
  }

  // ----------------------------------------------------------------
  // Inscripciones
  // ----------------------------------------------------------------

  /**
   * Inscribe a los socios indicados del usuario autenticado. Si hay plazas libres el socio tiene
   * la plaza asegurada y se confirma al instante; solo si el evento está completo queda en lista
   * de espera. Estar al día con la cuota no condiciona la plaza: únicamente da prioridad dentro
   * de la lista de espera.
   *
   * <p>En un multicarnet se pueden apuntar varias personas de una vez. Si no caben todas, el
   * comportamiento depende de {@code soloSiEntranTodos}: con el flag activo el grupo no se separa
   * y va entero a la lista de espera; sin él, los que quepan se confirman y el resto espera.
   *
   * @return estado resultante de cada socio inscrito, en el orden solicitado
   */
  public List<SocioInscripcionDTO> inscribirSocios(UUID eventoId,
      InscripcionSocioRequest request) {
    List<SocioEntity> aInscribir = resolverSociosSolicitados(request);

    EventoEntity evento = findEvento(eventoId);
    validarInscripcionAbierta(evento);

    for (SocioEntity socio : aInscribir) {
      if (inscripcionRepository.existsByEventoUidAndSocioUid(eventoId, socio.getUid())) {
        throw new IllegalStateException(socio.getNombre() + " ya está inscrito en este evento.");
      }
    }

    long confirmadas = inscripcionRepository.countByEventoUidAndEstado(eventoId,
        EstadoInscripcion.CONFIRMADA);
    // Con "solo si entramos todos" el grupo no se parte: si no caben todos, ninguno coge plaza.
    boolean entranTodos = plazasLibresPara(evento, confirmadas) >= aInscribir.size();
    boolean confirmarAlguno = !(request != null && request.isSoloSiEntranTodos() && !entranTodos);

    List<EventoInscripcionEntity> creadas = new ArrayList<>();
    for (SocioEntity socio : aInscribir) {
      // La penalización solo se gasta si de verdad le cuesta la plaza: si el evento estaba lleno
      // se habría quedado en espera igualmente, y castigarle entonces no sería castigo.
      boolean habriaEntrado = confirmarAlguno && hayHueco(evento, confirmadas);
      boolean penalizado = habriaEntrado && consumirPenalizacion(socio);
      EstadoInscripcion estado = habriaEntrado && !penalizado
          ? EstadoInscripcion.CONFIRMADA
          : EstadoInscripcion.EN_ESPERA;

      EventoInscripcionEntity inscripcion = new EventoInscripcionEntity();
      inscripcion.setEvento(evento);
      inscripcion.setSocio(socio);
      inscripcion.setNombre(socio.getNombre());
      inscripcion.setEmail(socio.getEmail());
      inscripcion.setTelefono(socio.getTelefono());
      inscripcion.setFechaInscripcion(LocalDateTime.now());
      inscripcion.setEstado(estado);
      inscripcion.setSocioPrioritario(esSocioAlDia(socio));
      inscripcionRepository.save(inscripcion);
      creadas.add(inscripcion);

      if (estado == EstadoInscripcion.CONFIRMADA) {
        confirmadas++;
      }
    }

    // Un único aviso por operación, detallando a cada persona: enviar un correo por socio
    // saturaría al titular de un multicarnet, que suele compartir email y teléfono con sus hijos.
    notificacionService.enviarResumenInscripcion(creadas, evento);

    return creadas.stream()
        .map(inscripcion -> SocioInscripcionDTO.builder()
            .socioUid(inscripcion.getSocio().getUid())
            .numeroSocio(inscripcion.getSocio().getNumeroSocio())
            .nombre(inscripcion.getNombre())
            .estado(inscripcion.getEstado())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * Resuelve y valida los socios de la petición. Todos deben pertenecer al usuario autenticado,
   * que es lo que impide apuntar a fichas ajenas pasando uids a mano. Si no se indica ninguno y
   * el usuario tiene una sola ficha, se asume esa.
   */
  private List<SocioEntity> resolverSociosSolicitados(InscripcionSocioRequest request) {
    List<SocioEntity> misSocios = sociosDelUsuarioAutenticado();
    if (misSocios.isEmpty()) {
      throw new IllegalStateException("El usuario no tiene ninguna ficha de socio asociada.");
    }

    List<UUID> solicitados = request != null ? request.getSocioUids() : null;
    if (solicitados == null || solicitados.isEmpty()) {
      if (misSocios.size() > 1) {
        throw new IllegalArgumentException(
            "Indica a quién quieres inscribir: tu cuenta tiene varias fichas de socio.");
      }
      return List.of(misSocios.get(0));
    }

    Map<UUID, SocioEntity> porUid = new LinkedHashMap<>();
    misSocios.forEach(socio -> porUid.put(socio.getUid(), socio));

    List<SocioEntity> resueltos = new ArrayList<>();
    for (UUID socioUid : solicitados.stream().distinct().collect(Collectors.toList())) {
      SocioEntity socio = porUid.get(socioUid);
      if (socio == null) {
        throw new IllegalArgumentException("Esa ficha de socio no pertenece a tu cuenta.");
      }
      resueltos.add(socio);
    }
    return resueltos;
  }

  /**
   * Fichas de socio del usuario autenticado, en orden estable. El orden importa: {@code socios}
   * es un Set, así que sin ordenar el "primer socio" varía entre llamadas.
   */
  private List<SocioEntity> sociosDelUsuarioAutenticado() {
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(userEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    return usuario.getSocios().stream()
        .sorted(Comparator
            .comparing(SocioEntity::getNumeroSocio, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(SocioEntity::getNombre, Comparator.nullsLast(Comparator.naturalOrder())))
        .collect(Collectors.toList());
  }

  /**
   * Inscripción pública para no socios (sustituye al Google Form). Siempre entran en lista de
   * espera; se confirmarán cuando queden plazas libres (asignación del administrador o baja de
   * un confirmado).
   */
  public EstadoInscripcion inscribirPublico(UUID eventoId, InscripcionPublicaRequest request) {
    EventoEntity evento = findEvento(eventoId);
    validarInscripcionAbierta(evento);

    String email = request.getEmail() != null ? request.getEmail().trim() : "";
    if (email.isBlank() || request.getNombre() == null || request.getNombre().isBlank()) {
      throw new IllegalArgumentException("Nombre y email son obligatorios.");
    }
    if (inscripcionRepository.existsByEventoUidAndEmailIgnoreCase(eventoId, email)) {
      throw new IllegalStateException("Ya existe una inscripción para ese email.");
    }

    EventoInscripcionEntity inscripcion = new EventoInscripcionEntity();
    inscripcion.setEvento(evento);
    inscripcion.setNombre(request.getNombre().trim());
    inscripcion.setEmail(email);
    inscripcion.setTelefono(request.getTelefono());
    inscripcion.setFechaInscripcion(LocalDateTime.now());
    inscripcion.setEstado(EstadoInscripcion.EN_ESPERA);
    inscripcion.setSocioPrioritario(false);
    inscripcionRepository.save(inscripcion);

    notificacionService.enviarConfirmacionInscripcionPublica(inscripcion, evento);
    return EstadoInscripcion.EN_ESPERA;
  }

  /**
   * Anula la inscripción de una ficha de socio del usuario autenticado. En un multicarnet
   * cualquiera de las fichas de la cuenta puede dar de baja a otra, ya que comparten login.
   *
   * @param socioUid ficha a dar de baja; puede ser null si el usuario tiene una única ficha
   */
  public void anularInscripcionSocio(UUID eventoId, UUID socioUid) {
    SocioEntity socio = resolverSocioPropio(socioUid);

    EventoInscripcionEntity inscripcion =
        inscripcionRepository.findByEventoUidAndSocioUid(eventoId, socio.getUid())
            .orElseThrow(() -> new IllegalStateException(
                socio.getNombre() + " no está inscrito en este evento."));

    EventoEntity evento = inscripcion.getEvento();
    EstadoInscripcion estadoAnulado = inscripcion.getEstado();
    // Renunciar a una plaza con el plazo ya cerrado deja un hueco que quizá nadie cubra, así que
    // cuenta como falta. Se retira sola si al repartir alguien acaba ocupando ese sitio.
    boolean cancelacionTardia = estadoAnulado == EstadoInscripcion.CONFIRMADA
        && inscripcionCerrada(evento);

    inscripcionRepository.delete(inscripcion);
    inscripcionRepository.flush();

    int promocionadas = estadoAnulado == EstadoInscripcion.CONFIRMADA
        ? promoverListaEspera(eventoId)
        : 0;

    // Se reparte primero y solo se pone la falta si el hueco ha quedado sin cubrir: así se evita
    // avisar de una falta que se iba a retirar acto seguido. Si el sitio se cubre más tarde, la
    // retira promoverListaEspera.
    if (cancelacionTardia && promocionadas == 0) {
      registrarFalta(socio, evento, MotivoFalta.CANCELACION_TARDIA);
    }
  }

  /**
   * Indica si anular ahora la plaza de un socio le costaría una falta, para poder avisarle antes
   * de que confirme. Solo penaliza con el plazo cerrado y si no hay nadie esperando que cubra el
   * hueco: si hay lista de espera, la plaza se reasigna al momento y no hay falta.
   */
  public boolean anularCostariaFalta(UUID eventoId, UUID socioUid) {
    SocioEntity socio = resolverSocioPropio(socioUid);
    EventoEntity evento = findEvento(eventoId);

    return inscripcionRepository.findByEventoUidAndSocioUid(eventoId, socio.getUid())
        .filter(i -> i.getEstado() == EstadoInscripcion.CONFIRMADA)
        .filter(i -> inscripcionCerrada(evento))
        .map(i -> inscripcionRepository.countByEventoUidAndEstado(eventoId,
            EstadoInscripcion.EN_ESPERA) == 0)
        .orElse(false);
  }

  /** Localiza una ficha del usuario autenticado, rechazando uids que no sean suyos. */
  private SocioEntity resolverSocioPropio(UUID socioUid) {
    List<SocioEntity> misSocios = sociosDelUsuarioAutenticado();
    if (misSocios.isEmpty()) {
      throw new IllegalStateException("El usuario no tiene ninguna ficha de socio asociada.");
    }
    if (socioUid == null) {
      if (misSocios.size() > 1) {
        throw new IllegalArgumentException(
            "Indica a quién quieres dar de baja: tu cuenta tiene varias fichas de socio.");
      }
      return misSocios.get(0);
    }
    return misSocios.stream()
        .filter(socio -> socio.getUid().equals(socioUid))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Esa ficha de socio no pertenece a tu cuenta."));
  }

  /**
   * Da de baja una inscripción desde el panel de gestión, tanto de un socio como de un no socio.
   * Se avisa a la persona dada de baja y, si tenía plaza confirmada, el hueco liberado se asigna
   * automáticamente al siguiente de la lista de espera, que recibe su aviso de plaza.
   *
   * @return número de personas promocionadas desde la lista de espera
   */
  public int eliminarInscripcion(UUID eventoId, UUID inscripcionId) {
    EventoInscripcionEntity inscripcion = inscripcionRepository.findById(inscripcionId)
        .orElseThrow(() -> new EntityNotFoundException(
            "Inscripción no encontrada con ID: " + inscripcionId));

    EventoEntity evento = inscripcion.getEvento();
    if (evento == null || !evento.getUid().equals(eventoId)) {
      throw new IllegalArgumentException("La inscripción no pertenece a este evento.");
    }

    EstadoInscripcion estadoAnulado = inscripcion.getEstado();
    inscripcionRepository.delete(inscripcion);
    inscripcionRepository.flush();

    notificacionService.enviarBajaInscripcion(inscripcion, evento);

    // Solo se libera hueco si ocupaba plaza; dar de baja a alguien en espera no promociona a nadie.
    return estadoAnulado == EstadoInscripcion.CONFIRMADA ? promoverListaEspera(eventoId) : 0;
  }

  /**
   * Asigna las plazas libres a la lista de espera por orden de prioridad (socios al día primero,
   * después por fecha de inscripción). Suele ejecutarse tras cerrarse el plazo de inscripción.
   *
   * @return número de personas promocionadas a CONFIRMADA
   */
  public int asignarPlazas(UUID eventoId) {
    return promoverListaEspera(eventoId);
  }

  private int promoverListaEspera(UUID eventoId) {
    EventoEntity evento = findEvento(eventoId);
    long confirmadas = inscripcionRepository.countByEventoUidAndEstado(eventoId,
        EstadoInscripcion.CONFIRMADA);

    List<EventoInscripcionEntity> enEspera =
        inscripcionRepository.findByEventoUidAndEstadoOrderByFechaInscripcionAsc(eventoId,
            EstadoInscripcion.EN_ESPERA);
    enEspera.sort(Comparator.comparing(EventoInscripcionEntity::isSocioPrioritario).reversed()
        .thenComparing(EventoInscripcionEntity::getFechaInscripcion));

    int promocionadas = 0;
    for (EventoInscripcionEntity inscripcion : enEspera) {
      if (!hayHueco(evento, confirmadas)) {
        break;
      }
      inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
      inscripcionRepository.save(inscripcion);
      notificacionService.enviarPromocionEspera(inscripcion, evento);
      confirmadas++;
      promocionadas++;
    }

    perdonarCancelacionesTardiasCubiertas(eventoId, promocionadas);
    return promocionadas;
  }

  /**
   * Retira una falta por cancelación tardía por cada plaza que se ha vuelto a cubrir: si alguien
   * ha ocupado el hueco, la baja no ha perjudicado a nadie. Se perdonan las más antiguas primero.
   */
  private void perdonarCancelacionesTardiasCubiertas(UUID eventoId, int plazasCubiertas) {
    if (plazasCubiertas <= 0) {
      return;
    }
    List<FaltaEventoEntity> pendientes =
        faltaRepository.findByEventoUidAndMotivoOrderByFechaRegistroAsc(eventoId,
            MotivoFalta.CANCELACION_TARDIA);
    pendientes.stream()
        .limit(plazasCubiertas)
        .forEach(faltaRepository::delete);
  }

  // ----------------------------------------------------------------
  // Faltas y penalizaciones
  // ----------------------------------------------------------------

  /**
   * Pasa lista a un inscrito con plaza. Marcarlo como ausente le genera una falta; corregir la
   * marca la retira, para que un error al pasar lista no penalice a quien no toca.
   *
   * @return las faltas acumuladas por ese socio después del cambio
   */
  public long marcarAsistencia(UUID eventoId, UUID inscripcionId, AsistenciaEvento asistencia) {
    EventoInscripcionEntity inscripcion = inscripcionRepository.findById(inscripcionId)
        .orElseThrow(() -> new EntityNotFoundException(
            "Inscripción no encontrada con ID: " + inscripcionId));

    EventoEntity evento = inscripcion.getEvento();
    if (evento == null || !evento.getUid().equals(eventoId)) {
      throw new IllegalArgumentException("La inscripción no pertenece a este evento.");
    }
    if (inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA) {
      throw new IllegalStateException(
          "Solo se pasa lista a quien tenía plaza: " + inscripcion.getNombre()
              + " se quedó en lista de espera.");
    }

    inscripcion.setAsistencia(asistencia != null ? asistencia : AsistenciaEvento.PENDIENTE);
    inscripcionRepository.save(inscripcion);

    SocioEntity socio = inscripcion.getSocio();
    if (socio == null) {
      return 0; // Un no socio no tiene ficha que penalizar; se guarda la asistencia y nada más.
    }

    if (inscripcion.getAsistencia() == AsistenciaEvento.NO_ASISTIO) {
      registrarFalta(socio, evento, MotivoFalta.NO_PRESENTADO);
    } else {
      faltaRepository.findByEventoUidAndSocioUidAndMotivo(eventoId, socio.getUid(),
          MotivoFalta.NO_PRESENTADO).ifPresent(faltaRepository::delete);
    }
    return faltaRepository.countBySocioUid(socio.getUid());
  }

  /** Quienes han fallado en este evento, tanto por no presentarse como por anular fuera de plazo. */
  public List<FaltaEventoDTO> getFaltas(UUID eventoId) {
    return faltaRepository.findByEventoUid(eventoId).stream()
        .map(falta -> {
          SocioEntity socio = falta.getSocio();
          return FaltaEventoDTO.builder()
              .uid(falta.getUid())
              .socioUid(socio.getUid())
              .numeroSocio(socio.getNumeroSocio())
              .nombre(socio.getNombre())
              .motivo(falta.getMotivo())
              .fechaRegistro(falta.getFechaRegistro())
              .penalizacionesRestantes(falta.getPenalizacionesRestantes())
              .faltasAcumuladas(faltasDe(socio))
              .build();
        })
        .sorted(Comparator.comparing(FaltaEventoDTO::getFechaRegistro))
        .collect(Collectors.toList());
  }

  /**
   * Historial de eventos de un socio junto con su recuento de faltas.
   *
   * <p>Se arma desde sus inscripciones y se le añaden las faltas que no tengan inscripción
   * detrás: anular fuera de plazo borra la inscripción, así que de esos eventos solo sobrevive la
   * falta y de otro modo no aparecerían en el historial.
   */
  @Transactional(readOnly = true)
  public HistorialSocioDto getHistorialSocio(UUID socioUid) {
    SocioEntity socio = socioRepository.findById(socioUid)
        .orElseThrow(() -> new EntityNotFoundException("Socio no encontrado con ID: " + socioUid));

    // Una falta por evento: en un mismo evento no puede haber a la vez ausencia y anulación,
    // porque anular borra la inscripción a la que se le pasaría lista.
    Map<UUID, FaltaEventoEntity> faltasPorEvento = new LinkedHashMap<>();
    for (FaltaEventoEntity falta :
        faltaRepository.findBySocioUidOrderByFechaRegistroDesc(socioUid)) {
      faltasPorEvento.putIfAbsent(falta.getEvento().getUid(), falta);
    }

    List<HistorialEventoSocioDto> filas = new ArrayList<>();
    for (EventoInscripcionEntity inscripcion : inscripcionRepository.findBySocioUid(socioUid)) {
      EventoEntity evento = inscripcion.getEvento();
      // Se saca del mapa: lo que quede al final son las faltas sin inscripción.
      FaltaEventoEntity falta = faltasPorEvento.remove(evento.getUid());
      filas.add(filaHistorial(evento, falta)
          .estado(inscripcion.getEstado())
          .asistencia(inscripcion.getAsistencia())
          .fechaInscripcion(inscripcion.getFechaInscripcion())
          .build());
    }
    for (FaltaEventoEntity falta : faltasPorEvento.values()) {
      filas.add(filaHistorial(falta.getEvento(), falta).build());
    }

    filas.sort(Comparator.comparing(HistorialEventoSocioDto::getFechaEvento).reversed());

    return HistorialSocioDto.builder()
        .socioUid(socio.getUid())
        .numeroSocio(socio.getNumeroSocio())
        .nombre(socio.getNombre())
        .faltasAcumuladas(filas.stream().filter(f -> f.getFaltaUid() != null).count())
        .faltasPendientes(filas.stream().filter(f -> f.getPenalizacionesRestantes() > 0).count())
        .eventosConPlaza(
            filas.stream().filter(f -> f.getEstado() == EstadoInscripcion.CONFIRMADA).count())
        .eventosAsistidos(
            filas.stream().filter(f -> f.getAsistencia() == AsistenciaEvento.ASISTIO).count())
        .eventos(filas)
        .build();
  }

  /** Parte común de una fila del historial: el evento y la falta que arrastre, si la hay. */
  private HistorialEventoSocioDto.HistorialEventoSocioDtoBuilder filaHistorial(EventoEntity evento,
      FaltaEventoEntity falta) {
    return HistorialEventoSocioDto.builder()
        .eventoUid(evento.getUid())
        .nombreEvento(evento.getNombreEvento())
        .fechaEvento(evento.getFechaEvento())
        .faltaUid(falta != null ? falta.getUid() : null)
        .motivoFalta(falta != null ? falta.getMotivo() : null)
        .fechaFalta(falta != null ? falta.getFechaRegistro() : null)
        .penalizacionesRestantes(falta != null ? falta.getPenalizacionesRestantes() : 0);
  }

  /**
   * Retira una falta: sirve tanto para justificar una ausencia como para corregir un error. Si la
   * inscripción sigue existiendo se le devuelve la asistencia a PENDIENTE, para que no quede
   * marcada como ausente una persona a la que se le ha perdonado la falta.
   */
  public void quitarFalta(UUID faltaId) {
    FaltaEventoEntity falta = faltaRepository.findById(faltaId)
        .orElseThrow(() -> new EntityNotFoundException("Falta no encontrada con ID: " + faltaId));

    inscripcionRepository
        .findByEventoUidAndSocioUid(falta.getEvento().getUid(), falta.getSocio().getUid())
        .ifPresent(inscripcion -> {
          inscripcion.setAsistencia(AsistenciaEvento.PENDIENTE);
          inscripcionRepository.save(inscripcion);
        });

    faltaRepository.delete(falta);
  }

  /** Crea la falta si el socio no tenía ya una del mismo motivo en ese evento. */
  private void registrarFalta(SocioEntity socio, EventoEntity evento, MotivoFalta motivo) {
    if (faltaRepository.findByEventoUidAndSocioUidAndMotivo(evento.getUid(), socio.getUid(), motivo)
        .isPresent()) {
      return;
    }
    FaltaEventoEntity falta = new FaltaEventoEntity();
    falta.setSocio(socio);
    falta.setEvento(evento);
    falta.setMotivo(motivo);
    falta.setFechaRegistro(LocalDateTime.now());
    falta.setPenalizacionesRestantes(penalizacionPorFalta(socio));
    faltaRepository.save(falta);
    notificacionService.enviarAvisoFalta(socio, evento, motivo,
        falta.getPenalizacionesRestantes());
  }

  /**
   * Gasta una penalización pendiente del socio, si tiene alguna.
   *
   * @return true si esta inscripción debe ir forzada a lista de espera
   */
  private boolean consumirPenalizacion(SocioEntity socio) {
    List<FaltaEventoEntity> pendientes = faltaRepository
        .findBySocioUidAndPenalizacionesRestantesGreaterThanOrderByFechaRegistroAsc(
            socio.getUid(), 0);
    if (pendientes.isEmpty()) {
      return false;
    }
    // Se gasta la falta más antigua: así se cumplen en el orden en que se cometieron.
    FaltaEventoEntity falta = pendientes.get(0);
    falta.setPenalizacionesRestantes(falta.getPenalizacionesRestantes() - 1);
    faltaRepository.save(falta);
    return true;
  }

  /** Eventos de castigo por falta según la peña del socio. */
  private int penalizacionPorFalta(SocioEntity socio) {
    Integer configurado = socio.getPena() != null
        ? socio.getPena().getEventosPenalizacionPorFalta()
        : null;
    return configurado != null ? Math.max(0, configurado) : PENALIZACION_POR_FALTA_DEFECTO;
  }

  /** Faltas acumuladas por socio, para pintarlas junto a cada inscrito. */
  private long faltasDe(SocioEntity socio) {
    return socio != null ? faltaRepository.countBySocioUid(socio.getUid()) : 0;
  }

  /** Inscripciones futuras que irán forzadas a lista de espera por faltas sin cumplir. */
  private int penalizacionesPendientesDe(SocioEntity socio) {
    return faltaRepository
        .findBySocioUidAndPenalizacionesRestantesGreaterThanOrderByFechaRegistroAsc(
            socio.getUid(), 0)
        .stream()
        .mapToInt(FaltaEventoEntity::getPenalizacionesRestantes)
        .sum();
  }

  // ----------------------------------------------------------------
  // Helpers
  // ----------------------------------------------------------------

  private EventoEntity findEvento(UUID id) {
    return eventoRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + id));
  }

  private void validarInscripcionAbierta(EventoEntity evento) {
    if (inscripcionCerrada(evento)) {
      throw new IllegalStateException(
          "El plazo de inscripción para este evento ha finalizado.");
    }
  }

  private boolean inscripcionCerrada(EventoEntity evento) {
    if (evento.getFechaEvento() != null && evento.getFechaEvento().isBefore(LocalDate.now())) {
      return true;
    }
    return evento.getFechaLimiteInscripcion() != null
        && LocalDateTime.now().isAfter(evento.getFechaLimiteInscripcion());
  }

  private boolean hayHueco(EventoEntity evento, long confirmadas) {
    return evento.getNumeroPlazas() == null || confirmadas < evento.getNumeroPlazas();
  }

  /** Plazas libres restantes; {@link Long#MAX_VALUE} si el evento no tiene límite de aforo. */
  private long plazasLibresPara(EventoEntity evento, long confirmadas) {
    if (evento.getNumeroPlazas() == null) {
      return Long.MAX_VALUE;
    }
    return Math.max(0, evento.getNumeroPlazas() - confirmadas);
  }

  private int calcularPlazasLibres(EventoEntity evento, int confirmadas) {
    if (evento.getNumeroPlazas() == null) {
      return -1; // sin límite
    }
    return Math.max(0, evento.getNumeroPlazas() - confirmadas);
  }

  /** Un socio es prioritario si está activo y tiene la cuota al día (o está exento de pago). */
  private boolean esSocioAlDia(SocioEntity socio) {
    if (!socio.isActivo()) {
      return false;
    }
    if (socio.isExentoPago()) {
      return true;
    }
    LocalDate desde = LocalDate.now().minusMonths(2);
    return cuotaRepository.existsBySocioAndEstadoAndFechaEmisionGreaterThanEqual(socio,
        EstadoCuota.PAGADA, desde);
  }
}

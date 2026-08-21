package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.InscripcionAdminDTO;
import com.softwells.fanops.controller.dto.InscripcionPublicaRequest;
import com.softwells.fanops.enums.EstadoCuota;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.mapper.EventoMapper;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.model.EventoInscripcionEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.CuotaRepository;
import com.softwells.fanops.repository.EventoInscripcionRepository;
import com.softwells.fanops.repository.EventoRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
  private final NotificacionService notificacionService;

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
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(userEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    // Asumimos que el primer socio es el principal para la inscripción
    SocioEntity socioPrincipal = usuario.getSocios().stream().findFirst()
        .orElseThrow(() -> new IllegalStateException("El usuario no tiene un socio asociado."));

    return eventoRepository.findAll().stream()
        .peek(evento -> completarInfoUsuario(evento, socioPrincipal))
        .map(EventoMapper::toInscripcionDTO)
        .sorted(Comparator.comparing(EventoInscripcionDTO::getFechaEvento))
        .collect(Collectors.toList());
  }

  private void completarInfoUsuario(EventoEntity evento, SocioEntity socioPrincipal) {
    List<EventoInscripcionEntity> inscripciones =
        inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(evento.getUid());
    Optional<EventoInscripcionEntity> miInscripcion = inscripciones.stream()
        .filter(i -> i.getSocio() != null && i.getSocio().getUid().equals(socioPrincipal.getUid()))
        .findFirst();

    evento.setCurrentUserInscrito(miInscripcion.isPresent());
    evento.setEstadoInscripcionActual(miInscripcion.map(EventoInscripcionEntity::getEstado)
        .orElse(null));
    evento.setNumInscritos((int) inscripciones.stream()
        .filter(i -> i.getEstado() == EstadoInscripcion.CONFIRMADA).count());
    evento.setNumEnEspera((int) inscripciones.stream()
        .filter(i -> i.getEstado() == EstadoInscripcion.EN_ESPERA).count());
    evento.setPlazasLibres(calcularPlazasLibres(evento, evento.getNumInscritos()));
    evento.setInscripcionCerrada(inscripcionCerrada(evento));
  }

  /** Información pública de un evento para el formulario de inscripción de no socios. */
  public EventoInscripcionDTO infoPublica(UUID eventoId) {
    EventoEntity evento = findEvento(eventoId);
    completarInfoAdmin(evento);
    return EventoMapper.toInscripcionDTO(evento);
  }

  public List<InscripcionAdminDTO> getInscripciones(UUID eventoId) {
    return inscripcionRepository.findByEventoUidOrderByFechaInscripcionAsc(eventoId).stream()
        .map(EventoMapper::toInscripcionAdminDTO)
        .collect(Collectors.toList());
  }

  // ----------------------------------------------------------------
  // CRUD de eventos
  // ----------------------------------------------------------------

  public EventoEntity save(EventoEntity evento) {
    return eventoRepository.save(evento);
  }

  public EventoEntity update(UUID id, EventoEntity eventoDetails) {
    EventoEntity eventoExistente = findEvento(id);

    eventoExistente.setNombreEvento(eventoDetails.getNombreEvento());
    eventoExistente.setFechaEvento(eventoDetails.getFechaEvento());
    eventoExistente.setFechaLimiteInscripcion(eventoDetails.getFechaLimiteInscripcion());
    eventoExistente.setUbicacion(eventoDetails.getUbicacion());
    eventoExistente.setDescripcion(eventoDetails.getDescripcion());
    eventoExistente.setNumeroPlazas(eventoDetails.getNumeroPlazas());
    eventoExistente.setCosteTotalEstimado(eventoDetails.getCosteTotalEstimado());
    eventoExistente.setCosteTotalReal(eventoDetails.getCosteTotalReal());

    return eventoRepository.save(eventoExistente);
  }

  public void delete(UUID id) {
    eventoRepository.deleteById(id);
  }

  // ----------------------------------------------------------------
  // Inscripciones
  // ----------------------------------------------------------------

  /**
   * Inscribe al socio principal del usuario autenticado. Los socios prioritarios (activos y con
   * la cuota al día) se confirman al instante si hay hueco; el resto queda en lista de espera.
   */
  public EstadoInscripcion inscribirSocio(UUID eventoId) {
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(userEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    SocioEntity socio = usuario.getSocios().stream().findFirst()
        .orElseThrow(() -> new IllegalStateException("Usuario sin socio principal."));

    EventoEntity evento = findEvento(eventoId);
    validarInscripcionAbierta(evento);

    if (inscripcionRepository.existsByEventoUidAndSocioUid(eventoId, socio.getUid())) {
      throw new IllegalStateException("Ya estás inscrito en este evento.");
    }

    boolean socioAlDia = esSocioAlDia(socio);
    long confirmadas = inscripcionRepository.countByEventoUidAndEstado(eventoId,
        EstadoInscripcion.CONFIRMADA);
    EstadoInscripcion estado =
        (socioAlDia && hayHueco(evento, confirmadas)) ? EstadoInscripcion.CONFIRMADA
            : EstadoInscripcion.EN_ESPERA;

    EventoInscripcionEntity inscripcion = new EventoInscripcionEntity();
    inscripcion.setEvento(evento);
    inscripcion.setSocio(socio);
    inscripcion.setNombre(socio.getNombre());
    inscripcion.setEmail(socio.getEmail());
    inscripcion.setTelefono(socio.getTelefono());
    inscripcion.setFechaInscripcion(LocalDateTime.now());
    inscripcion.setEstado(estado);
    inscripcion.setSocioPrioritario(socioAlDia);
    inscripcionRepository.save(inscripcion);

    notificacionService.enviarConfirmacionInscripcion(socio, evento, estado);
    return estado;
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

  public void anularInscripcionSocio(UUID eventoId) {
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    SocioEntity socio = usuarioRepository.findByEmailIgnoreCase(userEmail)
        .flatMap(u -> u.getSocios().stream().findFirst())
        .orElseThrow(() -> new IllegalStateException("Usuario sin socio principal."));

    EventoInscripcionEntity inscripcion =
        inscripcionRepository.findByEventoUidAndSocioUid(eventoId, socio.getUid())
            .orElseThrow(() -> new IllegalStateException("No estás inscrito en este evento."));

    EstadoInscripcion estadoAnulado = inscripcion.getEstado();
    inscripcionRepository.delete(inscripcion);

    if (estadoAnulado == EstadoInscripcion.CONFIRMADA) {
      promoverListaEspera(eventoId);
    }
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
    return promocionadas;
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

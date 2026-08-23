package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.InscripcionAdminDTO;
import com.softwells.fanops.controller.dto.InscripcionPublicaRequest;
import com.softwells.fanops.controller.dto.InscripcionSocioRequest;
import com.softwells.fanops.controller.dto.SocioInscripcionDTO;
import com.softwells.fanops.enums.AsistenciaEvento;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.model.EventoEntity;
import com.softwells.fanops.service.EventoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Proteger todo el controlador para administradores
public class EventoController {

  private final EventoService eventoService;

  @GetMapping
  @PreAuthorize("isAuthenticated()") // Permitir a cualquier usuario autenticado ver los eventos
  public ResponseEntity<ApiResponse<List<EventoInscripcionDTO>>> getAllEventos() {
    List<EventoInscripcionDTO> eventos = eventoService.findAllForInscripcion();
    return ResponseEntity.ok(new ApiResponse<>(true, "Eventos recuperados", eventos));
  }

  @GetMapping("/gestion")
  @PreAuthorize("hasRole('ADMIN')") // Solo para administradores
  public ResponseEntity<ApiResponse<List<EventoEntity>>> getAllEventosForAdmin() {
    List<EventoEntity> eventos = eventoService.findAllForAdmin();
    return ResponseEntity.ok(new ApiResponse<>(true, "Eventos para gestión recuperados", eventos));
  }

  /**
   * Información pública de un evento para el formulario de inscripción de no socios
   * (sustituye al Google Form). Accesible sin autenticación.
   */
  @GetMapping("/{id}/info-publica")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ApiResponse<EventoInscripcionDTO>> infoPublica(@PathVariable UUID id) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Evento recuperado",
        eventoService.infoPublica(id)));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<EventoEntity>> createEvento(
      @RequestBody EventoEntity evento) {
    EventoEntity nuevoEvento = eventoService.save(evento);
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Evento creado correctamente", nuevoEvento));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<EventoEntity>> updateEvento(
      @PathVariable UUID id, @RequestBody EventoEntity eventoDetails) {
    EventoEntity eventoActualizado = eventoService.update(id, eventoDetails);
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Evento actualizado correctamente", eventoActualizado));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteEvento(@PathVariable UUID id) {
    eventoService.delete(id);
    return ResponseEntity.ok(new ApiResponse<>(true, "Evento eliminado correctamente", null));
  }

  /**
   * Inscribe una o varias fichas de socio del usuario autenticado. En un multicarnet el cuerpo
   * indica a quién se apunta; si el usuario tiene una sola ficha puede omitirse.
   */
  @PostMapping("/{id}/inscribir")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<SocioInscripcionDTO>>> inscribir(@PathVariable UUID id,
      @RequestBody(required = false) InscripcionSocioRequest request) {
    List<SocioInscripcionDTO> resultado = eventoService.inscribirSocios(id, request);
    return ResponseEntity.ok(new ApiResponse<>(true, mensajeInscripcion(resultado), resultado));
  }

  private String mensajeInscripcion(List<SocioInscripcionDTO> resultado) {
    long confirmadas = resultado.stream()
        .filter(s -> s.getEstado() == EstadoInscripcion.CONFIRMADA)
        .count();
    long enEspera = resultado.size() - confirmadas;

    if (resultado.size() == 1) {
      return confirmadas == 1
          ? "Inscripción confirmada. ¡Nos vemos allí!"
          : "Te has apuntado. Estás en lista de espera, te avisaremos cuando haya hueco.";
    }
    if (enEspera == 0) {
      return confirmadas + " plazas confirmadas. ¡Nos vemos allí!";
    }
    if (confirmadas == 0) {
      return "Os habéis apuntado. Estáis en lista de espera, os avisaremos cuando haya hueco.";
    }
    return confirmadas + " con plaza confirmada y " + enEspera + " en lista de espera.";
  }

  /**
   * Inscripción pública de un no socio. Accesible sin autenticación desde el enlace compartido.
   */
  @PostMapping("/{id}/inscripcion-publica")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ApiResponse<EstadoInscripcion>> inscribirPublico(@PathVariable UUID id,
      @Valid @RequestBody InscripcionPublicaRequest request) {
    EstadoInscripcion estado = eventoService.inscribirPublico(id, request);
    return ResponseEntity.ok(new ApiResponse<>(true,
        "Te has apuntado. Estás en lista de espera, te avisaremos cuando haya hueco.", estado));
  }

  /**
   * Anula la inscripción de una ficha de socio del usuario. {@code socioUid} puede omitirse si
   * la cuenta tiene una única ficha.
   */
  @DeleteMapping("/{id}/anular")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> anularInscripcion(@PathVariable UUID id,
      @RequestParam(required = false) UUID socioUid) {
    eventoService.anularInscripcionSocio(id, socioUid);
    return ResponseEntity.ok(new ApiResponse<>(true, "Inscripción anulada correctamente", null));
  }

  @GetMapping("/{id}/inscripciones")
  public ResponseEntity<ApiResponse<List<InscripcionAdminDTO>>> getInscripciones(
      @PathVariable UUID id) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Inscripciones recuperadas",
        eventoService.getInscripciones(id)));
  }

  /**
   * Da de baja una inscripción desde gestión. Si ocupaba plaza confirmada, el hueco se asigna
   * automáticamente al siguiente de la lista de espera.
   */
  @DeleteMapping("/{id}/inscripciones/{inscripcionId}")
  public ResponseEntity<ApiResponse<Integer>> eliminarInscripcion(@PathVariable UUID id,
      @PathVariable UUID inscripcionId) {
    int promocionadas = eventoService.eliminarInscripcion(id, inscripcionId);
    String mensaje = promocionadas > 0
        ? "Inscripción eliminada. " + promocionadas
            + " persona(s) han pasado desde la lista de espera."
        : "Inscripción eliminada correctamente";
    return ResponseEntity.ok(new ApiResponse<>(true, mensaje, promocionadas));
  }

  /**
   * Pasa lista a un inscrito con plaza. Marcarlo como ausente le genera una falta; volver a
   * PENDIENTE o a ASISTIO la retira.
   *
   * @return faltas acumuladas por ese socio tras el cambio
   */
  @PutMapping("/{id}/inscripciones/{inscripcionId}/asistencia")
  public ResponseEntity<ApiResponse<Long>> marcarAsistencia(@PathVariable UUID id,
      @PathVariable UUID inscripcionId, @RequestParam AsistenciaEvento asistencia) {
    long faltas = eventoService.marcarAsistencia(id, inscripcionId, asistencia);
    String mensaje = switch (asistencia) {
      case NO_ASISTIO -> "Falta registrada";
      case ASISTIO -> "Asistencia registrada";
      case PENDIENTE -> "Marca de asistencia retirada";
    };
    return ResponseEntity.ok(new ApiResponse<>(true, mensaje, faltas));
  }

  /** Retira una falta, ya sea por estar justificada o por un error al pasar lista. */
  @DeleteMapping("/faltas/{faltaId}")
  public ResponseEntity<ApiResponse<Void>> quitarFalta(@PathVariable UUID faltaId) {
    eventoService.quitarFalta(faltaId);
    return ResponseEntity.ok(new ApiResponse<>(true, "Falta retirada", null));
  }

  /**
   * Avisa de si anular la plaza ahora costaría una falta, para poder advertirlo antes de que el
   * socio confirme la baja.
   */
  @GetMapping("/{id}/anular/aviso")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Boolean>> avisoAnulacion(@PathVariable UUID id,
      @RequestParam(required = false) UUID socioUid) {
    boolean costariaFalta = eventoService.anularCostariaFalta(id, socioUid);
    return ResponseEntity.ok(new ApiResponse<>(true, costariaFalta
        ? "Anular ahora supondría una falta"
        : "Anular ahora no supone falta", costariaFalta));
  }

  /**
   * Asigna las plazas libres a la lista de espera. Se usa tras cerrar el plazo de inscripción.
   */
  @PostMapping("/{id}/asignar-plazas")
  public ResponseEntity<ApiResponse<Integer>> asignarPlazas(@PathVariable UUID id) {
    int promocionadas = eventoService.asignarPlazas(id);
    return ResponseEntity.ok(new ApiResponse<>(true,
        promocionadas + " persona(s) promocionadas a plaza confirmada", promocionadas));
  }
}

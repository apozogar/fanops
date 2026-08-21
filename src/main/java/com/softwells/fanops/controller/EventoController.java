package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.EventoInscripcionDTO;
import com.softwells.fanops.controller.dto.InscripcionAdminDTO;
import com.softwells.fanops.controller.dto.InscripcionPublicaRequest;
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

  @PostMapping("/{id}/inscribir")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<EstadoInscripcion>> inscribir(@PathVariable UUID id) {
    EstadoInscripcion estado = eventoService.inscribirSocio(id);
    String mensaje = estado == EstadoInscripcion.CONFIRMADA
        ? "Inscripción confirmada. ¡Nos vemos allí!"
        : "Te has apuntado. Estás en lista de espera, te avisaremos cuando haya hueco.";
    return ResponseEntity.ok(new ApiResponse<>(true, mensaje, estado));
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

  @DeleteMapping("/{id}/anular")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> anularInscripcion(@PathVariable UUID id) {
    eventoService.anularInscripcionSocio(id);
    return ResponseEntity.ok(new ApiResponse<>(true, "Inscripción anulada correctamente", null));
  }

  @GetMapping("/{id}/inscripciones")
  public ResponseEntity<ApiResponse<List<InscripcionAdminDTO>>> getInscripciones(
      @PathVariable UUID id) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Inscripciones recuperadas",
        eventoService.getInscripciones(id)));
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

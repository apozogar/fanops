package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.SolicitudCarnetRequest;
import com.softwells.fanops.controller.dto.ApuntarSorteoResultado;
import com.softwells.fanops.controller.dto.SocioInscripcionDTO;
import com.softwells.fanops.controller.dto.SorteoCarnetDTO;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.service.EventoService;
import com.softwells.fanops.service.SorteoCarnetService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Sorteo de carnets de un evento: apuntarse al bombo, verlo y renunciar al premio. */
@RestController
@RequestMapping("/api/eventos/{id}/sorteo-carnet")
@RequiredArgsConstructor
public class SorteoCarnetController {

  private final SorteoCarnetService sorteoCarnetService;
  private final EventoService eventoService;

  /**
   * Estado del sorteo. Lo puede ver cualquier socio autenticado, no solo quien participa: que el
   * bombo sea público es justo lo que hace que el resultado no se discuta.
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SorteoCarnetDTO>> consultar(@PathVariable UUID id) {
    return ResponseEntity.ok(new ApiResponse<>(true, null, sorteoCarnetService.consultar(id)));
  }

  /**
   * Mete en el bombo una o varias fichas del usuario y las apunta al evento: es una sola acción.
   * En un multicarnet el cuerpo indica a quién se apunta; con una sola ficha puede omitirse.
   */
  @PostMapping("/solicitar")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SorteoCarnetDTO>> solicitar(@PathVariable UUID id,
      @RequestBody(required = false) SolicitudCarnetRequest request) {
    ApuntarSorteoResultado resultado = eventoService.apuntarAlSorteoCarnet(id, request);
    return ResponseEntity.ok(new ApiResponse<>(true, mensajeApuntado(resultado.inscripciones()),
        resultado.sorteo()));
  }

  /**
   * El mensaje cuenta lo que no se ve en el bombo: la plaza en el evento. Importa cuando el evento
   * está completo, porque entonces entrar al sorteo deja la plaza en lista de espera aunque el
   * carnet acabe tocando.
   */
  private String mensajeApuntado(List<SocioInscripcionDTO> inscripciones) {
    if (inscripciones.isEmpty()) {
      return "Ya estáis en el bombo. Mucha suerte.";
    }
    long confirmadas = inscripciones.stream()
        .filter(s -> s.getEstado() == EstadoInscripcion.CONFIRMADA)
        .count();
    long enEspera = inscripciones.size() - confirmadas;

    if (enEspera == 0) {
      return inscripciones.size() == 1
          ? "Ya estás en el bombo y con plaza en el evento. Mucha suerte."
          : "Ya estáis en el bombo y con plaza en el evento. Mucha suerte.";
    }
    if (confirmadas == 0) {
      return "Ya estáis en el bombo. El evento está completo, así que la plaza queda en lista de "
          + "espera; el sorteo del carnet no depende de eso.";
    }
    return "Ya estáis en el bombo. " + confirmadas + " con plaza en el evento y " + enEspera
        + " en lista de espera.";
  }

  /** Saca una ficha del bombo, mientras el sorteo no se haya celebrado. */
  @DeleteMapping("/solicitar")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SorteoCarnetDTO>> anularSolicitud(@PathVariable UUID id,
      @RequestParam(required = false) UUID socioUid) {
    SorteoCarnetDTO sorteo = sorteoCarnetService.anularSolicitud(id, socioUid);
    return ResponseEntity.ok(new ApiResponse<>(true, "Fuera del sorteo.", sorteo));
  }

  /** Devuelve un carnet que había tocado; pasa al primer suplente. */
  @PostMapping("/renunciar")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SorteoCarnetDTO>> renunciar(@PathVariable UUID id,
      @RequestParam(required = false) UUID socioUid) {
    SorteoCarnetDTO sorteo = sorteoCarnetService.renunciar(id, socioUid);
    return ResponseEntity.ok(new ApiResponse<>(true,
        "Carnet devuelto. Pasa al siguiente de la lista.", sorteo));
  }

  /**
   * Adelanta el sorteo. La semilla ya estaba comprometida desde que se programó, así que
   * adelantarlo no cambia quién sale: solo cuándo se sabe.
   */
  @PostMapping("/celebrar")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<SorteoCarnetDTO>> celebrar(@PathVariable UUID id) {
    SorteoCarnetDTO sorteo = sorteoCarnetService.celebrarAhora(id);
    return ResponseEntity.ok(new ApiResponse<>(true, "Sorteo celebrado.", sorteo));
  }
}

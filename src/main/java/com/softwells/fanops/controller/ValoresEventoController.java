package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.ValoresEventoDTO;
import com.softwells.fanops.service.ValoresEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Valores por defecto de los eventos de la peña de trabajo. Van en su propio controlador y no en
 * PenaController porque este es cosa del superadmin, y estos valores los gestiona el admin de
 * cada peña.
 */
@RestController
@RequestMapping("/api/pena/valores-evento")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ValoresEventoController {

  private final ValoresEventoService valoresEventoService;

  @GetMapping
  public ResponseEntity<ApiResponse<ValoresEventoDTO>> obtener() {
    return ResponseEntity.ok(new ApiResponse<>(true, null,
        valoresEventoService.obtenerDeMiPena()));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<ValoresEventoDTO>> guardar(
      @RequestBody ValoresEventoDTO valores) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Valores por defecto guardados",
        valoresEventoService.guardarEnMiPena(valores)));
  }
}

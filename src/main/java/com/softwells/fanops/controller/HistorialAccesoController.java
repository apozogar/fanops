package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.HistorialAccesoDto;
import com.softwells.fanops.service.HistorialAccesoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Historial de accesos a la aplicación. Es una vista transversal a todas las peñas, así que queda
 * reservada al superadmin y no al admin de cada peña.
 */
@RestController
@RequestMapping("/api/historial-accesos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class HistorialAccesoController {

  private final HistorialAccesoService historialAccesoService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<HistorialAccesoDto>>> listar() {
    return ResponseEntity.ok(new ApiResponse<>(true, null, historialAccesoService.listarUltimos()));
  }
}

package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.PenaRequestDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.service.PenaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/pena")
@RequiredArgsConstructor
// La gestión (alta/edición/baja/listado) de peñas es cosa del superadmin.
// Consultar una peña concreta la puede hacer cualquier usuario autenticado (ver más abajo).
@PreAuthorize("hasRole('SUPERADMIN')")
public class PenaController {

  private final PenaService service;

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()") // Cualquier usuario autenticado puede ver los datos de su peña
  public ResponseEntity<ApiResponse<PenaEntity>> findById(@PathVariable("id") Long id) {
    PenaEntity pena = service.findById(id);
    return ResponseEntity.ok(new ApiResponse<>(true, "Peña recuperada", pena));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PenaEntity>>> findAll() {
    return ResponseEntity.ok(new ApiResponse<>(true, "Peñas recuperadas", service.findAll()));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PenaEntity>> create(
      @Valid @RequestBody PenaRequestDTO request) {
    PenaEntity pena = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>(true, "Peña creada", pena));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PenaEntity>> update(@PathVariable("id") Long id,
      @Valid @RequestBody PenaRequestDTO request) {
    PenaEntity pena = service.update(id, request);
    return ResponseEntity.ok(new ApiResponse<>(true, "Peña actualizada", pena));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
    service.delete(id);
    return ResponseEntity.ok(new ApiResponse<>(true, "Peña eliminada", null));
  }
}

package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.PenaPublicaDto;
import com.softwells.fanops.controller.dto.PenaRequestDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.service.PenaService;
import com.softwells.fanops.service.PenaService.LogoPena;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  /**
   * Identidad de una peña por su dominio. Es el único endpoint de peñas accesible sin sesión: lo
   * consultan el login y el registro para saber en qué peña se está entrando y poder mostrar su
   * nombre, su logo y su color antes de que nadie se haya identificado.
   *
   * Devuelve un DTO reducido a propósito (ver PenaPublicaDto): la entidad completa lleva datos
   * bancarios y la lista de socios.
   */
  @GetMapping("/publica/{slug}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ApiResponse<PenaPublicaDto>> findBySlug(@PathVariable("slug") String slug) {
    PenaEntity pena = service.findBySlug(slug);
    PenaPublicaDto dto = new PenaPublicaDto(pena.getNombre(), pena.getSlug(), pena.getLogo(),
        pena.getLema(), pena.getColor());
    return ResponseEntity.ok(new ApiResponse<>(true, "Peña recuperada", dto));
  }

  /**
   * Logo de la peña como imagen HTTP real (no como data URI), para que se pueda referenciar
   * desde el HTML de los correos: los clientes de correo no cargan imágenes {@code data:}
   * embebidas, solo URLs de verdad. Ver {@link PenaService#obtenerLogo}.
   */
  @GetMapping("/publica/{slug}/logo")
  @PreAuthorize("permitAll()")
  public ResponseEntity<byte[]> logo(@PathVariable("slug") String slug) {
    LogoPena logo = service.obtenerLogo(slug);
    if (logo.esUrlExterna()) {
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(logo.url())).build();
    }
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(logo.contentType()))
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
        .body(logo.bytes());
  }

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

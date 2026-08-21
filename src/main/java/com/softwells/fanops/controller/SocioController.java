package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.controller.dto.CarnetDto;
import com.softwells.fanops.controller.dto.SocioStatsDto;
import com.softwells.fanops.model.CuotaEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.service.SepaService;
import com.softwells.fanops.service.SocioService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/socios")
@RequiredArgsConstructor
public class SocioController {

  private final SocioService socioService;

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<SocioEntity>> crear(@RequestBody SocioEntity socio) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Socio creado exitosamente",
        socioService.crear(socio)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<SocioEntity>> actualizar(@PathVariable UUID id,
      @RequestBody SocioEntity socio) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Socio actualizado exitosamente",
        socioService.actualizar(id, socio)));
  }

  @PutMapping("/me/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SocioEntity>> actualizarMiSocio(@PathVariable UUID id,
      @RequestBody SocioEntity socioData) {
    // El servicio debe verificar que el socio 'id' pertenece al usuario autenticado
    SocioEntity socioActualizado = socioService.actualizarMiSocio(id, socioData);
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Tu perfil ha sido actualizado.", socioActualizado));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
    socioService.eliminar(id);
    return ResponseEntity.ok(new ApiResponse<>(true, "Socio eliminado exitosamente", null));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Transactional(readOnly = true)
  public ResponseEntity<ApiResponse<List<SocioEntity>>> obtenerTodos(
      @RequestParam(required = false) String filtro) {
    List<SocioEntity> socios;
    if ("impagados".equals(filtro)) {
      socios = socioService.obtenerSociosConImpagos();
    } else {
      socios = socioService.obtenerTodos();
    }
    return ResponseEntity.ok(new ApiResponse<>(true, "Socios obtenidos con éxito", socios));
  }

  @GetMapping("/{id}/cuotas")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<List<CuotaEntity>>> obtenerCuotasDeSocio(
      @PathVariable UUID id) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Cuotas del socio",
        socioService.obtenerCuotasDeSocio(id)));
  }

  @GetMapping("/estadisticas")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<SocioStatsDto>> obtenerEstadisticas() {
    LocalDate fechaDesde = LocalDate.now().minusMonths(1);
    SocioStatsDto estadisticas = socioService.obtenerEstadisticas(fechaDesde);
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Estadísticas de socios recuperadas", estadisticas));
  }


  @PostMapping("/importar")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<String>> importarSociosDesdeExcel(
      @RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse<>(false, "El fichero está vacío", null));
    }

    socioService.importarSocios(file);

    return ResponseEntity.ok(new ApiResponse<>(true, "Socios importados correctamente", null));
  }

//  @GetMapping(value = "/generar-sepa", produces = MediaType.APPLICATION_XML_VALUE)
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//  public ResponseEntity<byte[]> generarFicheroSepa() {
//    LocalDateTime fechaDesde = LocalDateTime.now().minusMonths(1);
//    String sepaFile = sepaService.generarFicheroSepa(fechaDesde);
//    byte[] fileContent = sepaFile.getBytes(StandardCharsets.UTF_8);
//
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_XML);
//    headers.setContentDisposition(ContentDisposition.attachment().filename("sepa.xml").build());
//    headers.setContentLength(fileContent.length);
//
//    return ResponseEntity.ok()
//        .headers(headers)
//        .body(fileContent);
//  }

  /**
   * Endpoint para que un usuario autenticado obtenga su propia ficha de socio principal. La
   * información del usuario se extrae del token de seguridad.
   */
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<CarnetDto>> obtenerMiInformacion() {
    CarnetDto carnetData = socioService.obtenerDatosCarnetUsuarioAutenticado();
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Información del carnet recuperada", carnetData));
  }

  /**
   * Endpoint para que un usuario autenticado pueda añadir un nuevo socio asociado a su cuenta.
   */
  @PostMapping("/me/asociado")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SocioEntity>> crearSocioAsociado(
      @RequestBody SocioEntity nuevoSocioData) {
    // El servicio se encargará de obtener el usuario autenticado y asociarlo.
    SocioEntity nuevoSocio = socioService.crearSocioAsociado(nuevoSocioData);
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Nuevo socio asociado creado correctamente.", nuevoSocio));
  }
}

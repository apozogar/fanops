package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.ValoresEventoDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.ValoresEventoPenaEntity;
import com.softwells.fanops.repository.ValoresEventoPenaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valores por defecto con los que se propone un evento nuevo, uno por peña.
 *
 * <p>La fila se crea la primera vez que se guardan: una peña que nunca los ha configurado
 * devuelve un DTO con todo a null y el formulario sale vacío, como antes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ValoresEventoService {

  private final ValoresEventoPenaRepository repository;
  private final UsuarioService usuarioService;

  @Transactional(readOnly = true)
  public ValoresEventoDTO obtenerDeMiPena() {
    PenaEntity pena = usuarioService.obtenerPenaDelUsuarioAutenticado();
    return repository.findByPenaId(pena.getId())
        .map(this::toDto)
        .orElseGet(() -> ValoresEventoDTO.builder().build());
  }

  public ValoresEventoDTO guardarEnMiPena(ValoresEventoDTO valores) {
    PenaEntity pena = usuarioService.obtenerPenaDelUsuarioAutenticado();
    ValoresEventoPenaEntity entidad = repository.findByPenaId(pena.getId())
        .orElseGet(() -> {
          ValoresEventoPenaEntity nueva = new ValoresEventoPenaEntity();
          nueva.setPena(pena);
          return nueva;
        });

    entidad.setPlazas(valores.getPlazas());
    entidad.setCostePlaza(valores.getCostePlaza());
    entidad.setCarnets(valores.getCarnets());
    entidad.setCosteCarnet(valores.getCosteCarnet());
    entidad.setCosteTotalEstimado(valores.getCosteTotalEstimado());
    entidad.setDiasAntesFinInscripcion(valores.getDiasAntesFinInscripcion());
    entidad.setHoraFinInscripcion(valores.getHoraFinInscripcion());
    entidad.setDiasAntesSorteo(valores.getDiasAntesSorteo());
    entidad.setHoraSorteo(valores.getHoraSorteo());

    return toDto(repository.save(entidad));
  }

  private ValoresEventoDTO toDto(ValoresEventoPenaEntity entidad) {
    return ValoresEventoDTO.builder()
        .plazas(entidad.getPlazas())
        .costePlaza(entidad.getCostePlaza())
        .carnets(entidad.getCarnets())
        .costeCarnet(entidad.getCosteCarnet())
        .costeTotalEstimado(entidad.getCosteTotalEstimado())
        .diasAntesFinInscripcion(entidad.getDiasAntesFinInscripcion())
        .horaFinInscripcion(entidad.getHoraFinInscripcion())
        .diasAntesSorteo(entidad.getDiasAntesSorteo())
        .horaSorteo(entidad.getHoraSorteo())
        .build();
  }
}

package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.PenaRequestDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.repository.PenaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PenaService {

  private final PenaRepository repository;

  public PenaEntity findById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Pena no encontrada con ID: " + id));
  }

  public List<PenaEntity> findAll() {
    return repository.findAll();
  }

  /**
   * Peña por defecto usada al dar de alta usuarios que todavía no tienen una peña asignada
   * explícitamente (p.ej. auto-registro público, import de Excel). Devuelve la primera peña
   * creada; hoy solo puede haber una en producción, así que preserva el comportamiento previo
   * al CRUD de peñas.
   */
  public PenaEntity getDefaultPena() {
    return repository.findAll().stream()
        .min(Comparator.comparing(PenaEntity::getId))
        .orElseThrow(() -> new IllegalStateException(
            "No existe ninguna peña dada de alta todavía. Un superadmin debe crear al menos una "
                + "peña antes de poder registrar usuarios."));
  }

  public PenaEntity create(PenaRequestDTO dto) {
    PenaEntity pena = new PenaEntity();
    applyChanges(pena, dto);
    return repository.save(pena);
  }

  public PenaEntity update(Long id, PenaRequestDTO dto) {
    PenaEntity pena = findById(id);
    applyChanges(pena, dto);
    return repository.save(pena);
  }

  public void delete(Long id) {
    PenaEntity pena = findById(id);
    if (!pena.getSocios().isEmpty()) {
      throw new IllegalArgumentException(
          "No se puede eliminar la peña '" + pena.getNombre()
              + "' porque tiene socios asociados. Reasigna o elimina primero sus socios.");
    }
    repository.delete(pena);
  }

  private void applyChanges(PenaEntity pena, PenaRequestDTO dto) {
    pena.setNombre(dto.getNombre());
    pena.setIniciadorId(dto.getIniciadorId());
    pena.setDireccion1(dto.getDireccion1());
    pena.setDireccion2(dto.getDireccion2());
    pena.setCuentaIban(dto.getCuentaIban());
    pena.setCuentaBic(dto.getCuentaBic());
    pena.setCuotaAdulto(dto.getCuotaAdulto());
    pena.setCuotaMenor(dto.getCuotaMenor());
    pena.setEdadMayoria(dto.getEdadMayoria());
    pena.setEdadJubilacion(dto.getEdadJubilacion());
    pena.setLogo(dto.getLogo());
    pena.setLema(dto.getLema());
    pena.setColor(dto.getColor());
  }
}

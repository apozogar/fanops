package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.PenaRequestDTO;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.repository.PenaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PenaService {

  /** Tipos de imagen admitidos para el logo de la peña. */
  private static final Set<String> TIPOS_IMAGEN_PERMITIDOS = Set.of(
      "image/png", "image/jpeg", "image/webp", "image/gif", "image/svg+xml");

  /**
   * Tamaño máximo de la imagen del logo. Se guarda en base64 en la propia BD y viaja en cada
   * respuesta de la API (login, carnet, cabecera), así que conviene mantenerlo pequeño.
   */
  private static final int TAMANO_MAXIMO_LOGO_BYTES = 1024 * 1024; // 1 MB

  /** Logo subido por el usuario: data URI en base64, p. ej. "data:image/png;base64,iVBOR..." */
  private static final Pattern PATRON_DATA_URI =
      Pattern.compile("^data:([a-z0-9.+/-]+);base64,([A-Za-z0-9+/=\\s]+)$",
          Pattern.CASE_INSENSITIVE);

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

  /**
   * Valida el logo recibido. La imagen se sube desde el navegador ya codificada en base64
   * (data URI) y se guarda tal cual en la BD, de forma que no dependa de ninguna URL externa que
   * pueda cambiar o dejar de estar disponible. Se siguen admitiendo URLs para no romper las peñas
   * dadas de alta antes de este cambio.
   *
   * @return el logo normalizado, o {@code null} si no hay logo
   */
  private String validarLogo(String logo) {
    if (logo == null || logo.isBlank()) {
      return null;
    }

    String valor = logo.trim();
    if (!valor.startsWith("data:")) {
      // Compatibilidad: logos antiguos apuntando a una URL o a un asset del frontend.
      return valor;
    }

    var matcher = PATRON_DATA_URI.matcher(valor);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "El logo no es una imagen válida. Vuelve a seleccionar el fichero.");
    }

    String tipo = matcher.group(1).toLowerCase();
    if (!TIPOS_IMAGEN_PERMITIDOS.contains(tipo)) {
      throw new IllegalArgumentException(
          "Formato de imagen no admitido. Usa PNG, JPG, WEBP, GIF o SVG.");
    }

    byte[] imagen;
    try {
      imagen = Base64.getMimeDecoder().decode(matcher.group(2));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "El logo no es una imagen válida. Vuelve a seleccionar el fichero.");
    }
    if (imagen.length == 0) {
      throw new IllegalArgumentException("El logo está vacío. Vuelve a seleccionar el fichero.");
    }
    if (imagen.length > TAMANO_MAXIMO_LOGO_BYTES) {
      throw new IllegalArgumentException(
          "La imagen del logo no puede superar 1 MB. Reduce su tamaño e inténtalo de nuevo.");
    }

    return valor;
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
    pena.setEventosPenalizacionPorFalta(dto.getEventosPenalizacionPorFalta());
    pena.setLogo(validarLogo(dto.getLogo()));
    pena.setLema(dto.getLema());
    pena.setColor(dto.getColor());
  }
}

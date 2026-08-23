package com.softwells.fanops.controller.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

/** Petición de inscripción de uno o varios socios del usuario autenticado. */
@Data
public class InscripcionSocioRequest {

  /**
   * Socios a inscribir. Todos deben pertenecer al usuario autenticado. Si se omite y el usuario
   * tiene una única ficha de socio, se inscribe esa.
   */
  private List<UUID> socioUids;

  /**
   * Si es true y no hay plazas libres para todo el grupo, ninguno coge plaza: todos van juntos a
   * la lista de espera. Si es false, los que quepan se confirman y el resto queda en espera.
   */
  private boolean soloSiEntranTodos;
}

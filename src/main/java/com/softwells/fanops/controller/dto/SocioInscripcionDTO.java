package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.EstadoInscripcion;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Estado de un socio concreto del usuario autenticado frente a un evento. Permite que en un
 * multicarnet (por ejemplo un padre con dos hijos) se vea y se gestione a cada persona por
 * separado, en lugar de operar a ciegas sobre un "socio principal" indeterminado.
 */
@Data
@Builder
public class SocioInscripcionDTO {

  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;

  /** Estado de su inscripción en el evento, o null si no está inscrito. */
  private EstadoInscripcion estado;
}

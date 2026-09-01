package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Estado de una ficha de socio del usuario frente al sorteo. Hay una entrada por ficha, apuntada
 * o no, para que en un multicarnet se vea a quién se está metiendo en el bombo.
 */
@Data
@Builder
public class SocioSolicitudCarnetDTO {

  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;

  /** null si esa ficha no está apuntada al sorteo. */
  private EstadoSolicitudCarnet estado;

  private Integer posicion;

  /** Papeletas con las que entraría al bombo si se apuntase ahora, o con las que entró. */
  private int papeletas;
}

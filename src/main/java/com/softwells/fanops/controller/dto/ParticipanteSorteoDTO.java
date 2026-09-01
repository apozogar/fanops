package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.EstadoSolicitudCarnet;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Una bola del bombo. Antes del sorteo va en orden de solicitud; después, en orden de extracción.
 */
@Data
@Builder
public class ParticipanteSorteoDTO {

  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;

  /** Papeletas con las que entra o entró al bombo. */
  private int papeletas;

  /** Orden de salida, 1..N. Null si el sorteo todavía no se ha celebrado. */
  private Integer posicion;

  private EstadoSolicitudCarnet estado;

  /** true si la ficha pertenece al usuario que consulta, para poder resaltarla en el bombo. */
  private boolean propio;
}

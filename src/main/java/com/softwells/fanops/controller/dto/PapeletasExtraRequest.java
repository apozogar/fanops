package com.softwells.fanops.controller.dto;

import lombok.Data;

/** Papeletas extra que la gestión asigna a un participante del sorteo de carnets. */
@Data
public class PapeletasExtraRequest {

  /** Papeletas que se suman a las del historial. 0 las quita. */
  private int papeletasExtra;
}

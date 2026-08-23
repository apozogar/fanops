package com.softwells.fanops.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Datos editables de una Peña. Se usa tanto para alta como para edición desde
 * el panel de superadmin; deliberadamente no incluye la lista de socios.
 */
@Data
public class PenaRequestDTO {

  @NotBlank(message = "El nombre de la peña es obligatorio")
  private String nombre;

  private String iniciadorId;
  private String direccion1;
  private String direccion2;
  private String cuentaIban;
  private String cuentaBic;
  private Double cuotaAdulto;
  private Double cuotaMenor;
  private Integer edadMayoria;
  private Integer edadJubilacion;

  /** Inscripciones que van forzadas a lista de espera por cada falta. 0 desactiva el castigo. */
  private Integer eventosPenalizacionPorFalta;
  private String logo;
  private String lema;
  private String color;
}

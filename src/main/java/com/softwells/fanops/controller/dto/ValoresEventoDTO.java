package com.softwells.fanops.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Valores por defecto de los eventos de una peña. Todos los campos pueden venir a null: significa
 * que ese campo no se sugiere y el formulario lo deja vacío.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValoresEventoDTO {

  private Integer plazas;
  private BigDecimal costePlaza;
  private Integer carnets;
  private BigDecimal costeCarnet;
  private BigDecimal costeTotalEstimado;

  /** Días antes del evento en que cierra la inscripción, y a qué hora. */
  private Integer diasAntesFinInscripcion;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime horaFinInscripcion;

  /** Días antes del evento en que se celebra el sorteo, y a qué hora. */
  private Integer diasAntesSorteo;

  @JsonFormat(pattern = "HH:mm")
  private LocalTime horaSorteo;
}

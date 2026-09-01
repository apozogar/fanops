package com.softwells.fanops.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventoInscripcionDTO {

  private UUID uid;
  private String nombreEvento;
  private LocalDate fechaEvento;
  private LocalDateTime fechaLimiteInscripcion;
  private String ubicacion;

  /** Lo que paga cada persona por la plaza. Null si no se ha indicado. */
  private BigDecimal costePlaza;

  /** Lo que paga quien va con carnet sorteado. Null si no se ha indicado. */
  private BigDecimal costeCarnet;

  /** true si el plazo de inscripción ha finalizado (o el evento ya pasó). */
  private boolean inscripcionCerrada;

  private int plazasOcupadas;

  /** Número de plazas libres (-1 significa ilimitado). */
  private int plazasLibres;

  private int enListaEspera;

  /** true si al menos uno de los socios del usuario actual está inscrito. */
  @JsonProperty("isCurrentUserInscrito")
  private boolean isCurrentUserInscrito;

  /**
   * Estado de cada ficha de socio del usuario actual frente a este evento. En un multicarnet
   * trae una entrada por persona, de forma que se ve quién está inscrito y con qué estado.
   * Vacía en las consultas anónimas o de administración.
   */
  private List<SocioInscripcionDTO> misSocios;

  /** Resumen del sorteo de carnets del evento. Null si el evento no sortea carnets. */
  private SorteoResumenDTO sorteo;
}

package com.softwells.fanops.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwells.fanops.enums.EstadoInscripcion;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  /** true si el plazo de inscripción ha finalizado (o el evento ya pasó). */
  private boolean inscripcionCerrada;

  private int plazasOcupadas;

  /** Número de plazas libres (-1 significa ilimitado). */
  private int plazasLibres;

  private int enListaEspera;

  @JsonProperty("isCurrentUserInscrito")
  private boolean isCurrentUserInscrito;

  /** Estado de la inscripción del usuario actual (null si no está inscrito). */
  private EstadoInscripcion estadoInscripcionActual;
}

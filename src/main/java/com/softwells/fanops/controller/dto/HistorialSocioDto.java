package com.softwells.fanops.controller.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/** Historial de eventos de un socio con su recuento de faltas, para el modal de gestión. */
@Data
@Builder
public class HistorialSocioDto {

  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;

  /** Faltas de toda su historia, hayan penalizado ya o no. */
  private long faltasAcumuladas;

  /** Faltas que todavía le forzarán a lista de espera en próximas inscripciones. */
  private long faltasPendientes;

  /** Eventos en los que llegó a tener plaza. */
  private long eventosConPlaza;

  /** Eventos en los que se le pasó lista y estaba. */
  private long eventosAsistidos;

  /** Del más reciente al más antiguo. */
  private List<HistorialEventoSocioDto> eventos;
}

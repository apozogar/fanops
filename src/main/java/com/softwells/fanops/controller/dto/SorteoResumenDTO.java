package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.EstadoSorteo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Resumen del sorteo de carnets para la tarjeta de un evento. Va aparte de
 * {@link SorteoCarnetDTO} porque el listado muestra muchos eventos a la vez y no necesita la
 * lista de bolas ni la semilla: eso se pide al abrir el bombo.
 */
@Data
@Builder
public class SorteoResumenDTO {

  private int plazasCarnet;
  private LocalDateTime fechaProgramada;
  private EstadoSorteo estado;

  /** true si todavía se puede entrar (sorteo sin celebrar y plazo del evento abierto). */
  private boolean admiteSolicitudes;

  /** Cuántos hay metidos en el bombo. */
  private int participantes;

  /** Una entrada por ficha de socio del usuario, apuntada o no. */
  private List<SocioSolicitudCarnetDTO> misSocios;
}

package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.EstadoSorteo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/** Todo lo que necesita la vista del bombo de un evento. */
@Data
@Builder
public class SorteoCarnetDTO {

  private UUID eventoUid;
  private String nombreEvento;

  /** false si el evento no sortea carnets; el resto de campos van vacíos. */
  private boolean habilitado;

  private int plazasCarnet;

  /** Lo que paga quien se lleva un carnet. Null si no se ha indicado. */
  private BigDecimal costeCarnet;

  private LocalDateTime fechaProgramada;
  private LocalDateTime fechaEjecucion;
  private EstadoSorteo estado;

  /** true mientras el sorteo no se ha celebrado. */
  private boolean abierto;

  /**
   * true si todavía se puede entrar en el bombo. Es más estricto que {@code abierto}: entrar al
   * sorteo apunta también al evento, así que el plazo de inscripción tiene que estar abierto.
   */
  private boolean admiteSolicitudes;

  /** SHA-256 de la semilla. Se publica desde el principio. */
  private String hashSemilla;

  /** Semilla del sorteo. Null hasta que se celebra: antes solo se conoce su hash. */
  private String semilla;

  /** Participantes; en orden de extracción una vez celebrado el sorteo. */
  private List<ParticipanteSorteoDTO> participantes;

  /** Una entrada por ficha de socio del usuario que consulta. Vacía para un admin ajeno. */
  private List<SocioSolicitudCarnetDTO> misSocios;
}

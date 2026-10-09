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

  /** true si entrar en el bombo no da plaza de autobús: solo la consigue quien gana el carnet. */
  private boolean plazaSoloSiGana;

  /**
   * true si la gestión puede ajustar papeletas extra: la peña de quien consulta tiene la opción
   * activada y el sorteo no se ha celebrado. Con el sorteo celebrado ya no se toca nada.
   */
  private boolean ajustePapeletasPermitido;

  /** Participantes; en orden de extracción una vez celebrado el sorteo. */
  private List<ParticipanteSorteoDTO> participantes;

  /** Una entrada por ficha de socio del usuario que consulta. Vacía para un admin ajeno. */
  private List<SocioSolicitudCarnetDTO> misSocios;
}

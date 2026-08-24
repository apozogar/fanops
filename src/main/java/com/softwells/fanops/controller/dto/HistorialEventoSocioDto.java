package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.AsistenciaEvento;
import com.softwells.fanops.enums.EstadoInscripcion;
import com.softwells.fanops.enums.MotivoFalta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Un evento en el historial de un socio: si se inscribió, si llegó a tener plaza, si fue, y si le
 * quedó falta.
 *
 * <p>Los campos de inscripción son nulos cuando la fila viene solo de una falta: anular fuera de
 * plazo borra la inscripción, así que de esos eventos únicamente sobrevive la falta.
 */
@Data
@Builder
public class HistorialEventoSocioDto {

  private UUID eventoUid;
  private String nombreEvento;
  private LocalDate fechaEvento;

  /** Null si de ese evento ya no queda inscripción (cancelación tardía). */
  private EstadoInscripcion estado;

  /** Null si no hay inscripción; PENDIENTE mientras no se haya pasado lista. */
  private AsistenciaEvento asistencia;

  private LocalDateTime fechaInscripcion;

  /** Falta que arrastra de este evento, si la tiene. Es lo que se perdona. */
  private UUID faltaUid;

  private MotivoFalta motivoFalta;

  private LocalDateTime fechaFalta;

  /** Inscripciones que todavía le irán forzadas a lista de espera por esta falta. */
  private int penalizacionesRestantes;
}

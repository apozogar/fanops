package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.AsistenciaEvento;
import com.softwells.fanops.enums.EstadoInscripcion;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscripcionAdminDTO {

  private UUID uid;
  private EstadoInscripcion estado;
  private boolean socioPrioritario;
  private LocalDateTime fechaInscripcion;

  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;
  private String email;
  private String telefono;

  /** Resultado de pasar lista. PENDIENTE mientras no se haya pasado. */
  private AsistenciaEvento asistencia;

  /** Faltas acumuladas por el socio en total, no solo en este evento. */
  private long faltasAcumuladas;

  /** Falta que arrastra de este evento concreto, si la tiene, para poder retirarla desde aquí. */
  private UUID faltaUid;

  /** Inscripciones que todavía le irán forzadas a lista de espera por faltas anteriores. */
  private int penalizacionesPendientes;
}

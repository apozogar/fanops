package com.softwells.fanops.controller.dto;

import com.softwells.fanops.enums.MotivoFalta;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Falta de un socio en un evento, para el listado de fallos.
 *
 * <p>Se sirve desde las faltas y no desde las inscripciones a propósito: una cancelación tardía
 * borra la inscripción, así que quien anuló fuera de plazo no aparece en ninguna de las otras dos
 * listas y solo se ve aquí.
 */
@Data
@Builder
public class FaltaEventoDTO {

  private UUID uid;
  private UUID socioUid;
  private Integer numeroSocio;
  private String nombre;
  private MotivoFalta motivo;
  private LocalDateTime fechaRegistro;

  /** Inscripciones que todavía le irán forzadas a lista de espera por esta falta. */
  private int penalizacionesRestantes;

  /** Faltas acumuladas por el socio en total, no solo en este evento. */
  private long faltasAcumuladas;
}

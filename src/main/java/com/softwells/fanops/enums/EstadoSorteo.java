package com.softwells.fanops.enums;

/** Situación del sorteo de carnets de un evento. */
public enum EstadoSorteo {
  /** Aún no se ha celebrado: se admiten solicitudes y solo se publica el hash de la semilla. */
  PROGRAMADO,
  /** Ya celebrado: el orden de extracción está fijado y la semilla es pública. */
  EJECUTADO
}

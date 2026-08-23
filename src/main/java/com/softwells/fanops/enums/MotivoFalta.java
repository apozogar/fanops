package com.softwells.fanops.enums;

/** Por qué se le ha puesto una falta a un socio. */
public enum MotivoFalta {
  /** Tenía plaza y no se presentó. */
  NO_PRESENTADO,
  /**
   * Anuló su plaza con el plazo de inscripción ya cerrado. Se retira sola si alguien de la lista
   * de espera acaba ocupando el hueco que dejó.
   */
  CANCELACION_TARDIA
}

/**
 * Valores por defecto de los eventos de la peña. Cualquiera puede venir a null: significa que ese
 * campo no se sugiere y el formulario del evento lo deja vacío.
 */
export interface ValoresEvento {
  /** Plazas del evento (el autobús, normalmente). */
  plazas?: number | null;
  /** Lo que paga cada socio por su plaza. */
  costePlaza?: number | null;
  /** Carnets que se sortean. */
  carnets?: number | null;
  /** Lo que paga quien se lleva un carnet. */
  costeCarnet?: number | null;
  /** Coste total estimado del evento, para las cuentas de la peña. */
  costeTotalEstimado?: number | null;
  /**
   * Días antes del evento en que cierra la inscripción, y la hora de ese día ('HH:mm'). Se
   * guardan relativos porque lo que se repite de un partido a otro no es una fecha concreta sino
   * "dos días antes, a las ocho".
   */
  diasAntesFinInscripcion?: number | null;
  horaFinInscripcion?: string | null;
  /** Lo mismo para el sorteo de carnets. */
  diasAntesSorteo?: number | null;
  horaSorteo?: string | null;
}

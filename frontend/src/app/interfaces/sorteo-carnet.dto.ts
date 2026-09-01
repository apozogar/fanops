/** Situación del sorteo de carnets de un evento. */
export type EstadoSorteo = 'PROGRAMADO' | 'EJECUTADO';

/** Situación de un socio dentro del bombo. */
export type EstadoSolicitudCarnet = 'PENDIENTE' | 'GANADORA' | 'SUPLENTE' | 'RENUNCIADA';

/** Estado de una ficha de socio de la cuenta frente al sorteo. */
export interface SocioSolicitudCarnet {
  socioUid: string;
  numeroSocio?: number | null;
  nombre: string;
  /** null si esa ficha no está apuntada al sorteo. */
  estado?: EstadoSolicitudCarnet | null;
  posicion?: number | null;
  /** Papeletas con las que entra o entró al bombo. 0 cuando no se han calculado (resumen). */
  papeletas: number;
}

/** Una bola del bombo. */
export interface ParticipanteSorteo {
  socioUid: string;
  numeroSocio?: number | null;
  nombre: string;
  papeletas: number;
  /** Orden de salida, 1..N. null si el sorteo aún no se ha celebrado. */
  posicion?: number | null;
  estado: EstadoSolicitudCarnet;
  /** true si la ficha es de la cuenta que mira, para resaltarla. */
  propio: boolean;
}

/** Lo que se muestra del sorteo en la tarjeta del evento, sin la lista de bolas. */
export interface SorteoResumen {
  plazasCarnet: number;
  fechaProgramada?: string;
  estado: EstadoSorteo;
  /** true si todavía se puede entrar en el bombo (sorteo sin celebrar y plazo del evento abierto). */
  admiteSolicitudes: boolean;
  participantes: number;
  misSocios: SocioSolicitudCarnet[];
}

/** El sorteo completo, con todo lo que necesita la animación del bombo. */
export interface SorteoCarnet {
  eventoUid: string;
  nombreEvento: string;
  habilitado: boolean;
  plazasCarnet: number;
  /** Lo que paga quien se lleva un carnet. Ausente si no se ha indicado. */
  costeCarnet?: number | null;
  fechaProgramada?: string;
  fechaEjecucion?: string;
  estado: EstadoSorteo;
  /** true mientras el sorteo no se ha celebrado. */
  abierto: boolean;
  /**
   * true si todavía se puede entrar en el bombo. Más estricto que `abierto`: entrar al sorteo
   * apunta también al evento, así que el plazo de inscripción tiene que estar abierto.
   */
  admiteSolicitudes: boolean;
  /** SHA-256 de la semilla, publicado desde que se programa el sorteo. */
  hashSemilla?: string;
  /** Semilla del sorteo. null hasta que se celebra. */
  semilla?: string | null;
  /** En orden de extracción una vez celebrado el sorteo. */
  participantes: ParticipanteSorteo[];
  misSocios: SocioSolicitudCarnet[];
}

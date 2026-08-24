export type EstadoInscripcion = 'CONFIRMADA' | 'EN_ESPERA';

/** Resultado de pasar lista a quien tenía plaza. */
export type AsistenciaEvento = 'PENDIENTE' | 'ASISTIO' | 'NO_ASISTIO';

/** Estado de una ficha de socio del usuario actual frente a un evento. */
export interface SocioInscripcion {
  socioUid: string;
  numeroSocio?: number | null;
  nombre: string;
  /** null si esa persona no está inscrita. */
  estado?: EstadoInscripcion | null;
}

export interface EventoInscripcionDTO {
  uid: string;
  nombreEvento: string;
  fechaEvento: Date;
  fechaLimiteInscripcion?: Date;
  ubicacion?: string;
  inscripcionCerrada: boolean;
  plazasOcupadas: number;
  plazasLibres: number; // -1 = ilimitado
  enListaEspera: number;
  /** true si alguna de mis fichas está inscrita. */
  isCurrentUserInscrito: boolean;
  /** Una entrada por ficha de socio de la cuenta (multicarnet). */
  misSocios: SocioInscripcion[];
}

export type MotivoFalta = 'NO_PRESENTADO' | 'CANCELACION_TARDIA';

/**
 * Falta de un socio en un evento. Llega desde las faltas y no desde las inscripciones porque una
 * cancelación tardía borra la inscripción: quien anuló fuera de plazo solo aparece aquí.
 */
export interface FaltaEvento {
  uid: string;
  socioUid: string;
  numeroSocio?: number | null;
  nombre: string;
  motivo: MotivoFalta;
  fechaRegistro: Date;
  penalizacionesRestantes: number;
  faltasAcumuladas: number;
}

/** Petición de inscripción de una o varias fichas de socio. */
export interface InscripcionSocioRequest {
  socioUids: string[];
  /** Si es true y no caben todos, ninguno coge plaza: el grupo entero va a lista de espera. */
  soloSiEntranTodos: boolean;
}

export interface InscripcionPublicaRequest {
  nombre: string;
  email: string;
  telefono?: string;
}

export interface InscripcionAdmin {
  uid: string;
  estado: EstadoInscripcion;
  socioPrioritario: boolean;
  fechaInscripcion: Date;
  socioUid?: string | null;
  numeroSocio?: number | null;
  nombre: string;
  email: string;
  telefono?: string;
  asistencia?: AsistenciaEvento | null;
  /** Faltas acumuladas por el socio en total, no solo en este evento. */
  faltasAcumuladas: number;
  /** Falta que arrastra de este evento, si la tiene. */
  faltaUid?: string | null;
  /** Inscripciones futuras que le irán forzadas a lista de espera. */
  penalizacionesPendientes: number;
}
/**
 * Un evento en el historial de un socio. Los campos de inscripción son nulos cuando la fila viene
 * solo de una falta: anular fuera de plazo borra la inscripción y de ese evento solo queda ella.
 */
export interface HistorialEventoSocio {
  eventoUid: string;
  nombreEvento: string;
  fechaEvento: string;
  estado?: EstadoInscripcion | null;
  asistencia?: AsistenciaEvento | null;
  fechaInscripcion?: string | null;
  /** Falta que arrastra de este evento, si la tiene. Es lo que se perdona. */
  faltaUid?: string | null;
  motivoFalta?: MotivoFalta | null;
  fechaFalta?: string | null;
  penalizacionesRestantes: number;
}

/** Historial de eventos de un socio con su recuento de faltas. */
export interface HistorialSocio {
  socioUid: string;
  numeroSocio?: number | null;
  nombre: string;
  faltasAcumuladas: number;
  faltasPendientes: number;
  eventosConPlaza: number;
  eventosAsistidos: number;
  /** Del más reciente al más antiguo. */
  eventos: HistorialEventoSocio[];
}

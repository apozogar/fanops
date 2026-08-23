export type EstadoInscripcion = 'CONFIRMADA' | 'EN_ESPERA';

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
}
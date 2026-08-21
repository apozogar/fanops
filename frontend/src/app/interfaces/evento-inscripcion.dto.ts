export type EstadoInscripcion = 'CONFIRMADA' | 'EN_ESPERA';

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
  isCurrentUserInscrito: boolean;
  estadoInscripcionActual?: EstadoInscripcion | null;
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
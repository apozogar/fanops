import {Socio} from "@/interfaces/socio.interface";

export interface Evento {
    uid?: string;
    nombreEvento: string;
    fechaEvento: Date;
    fechaLimiteInscripcion?: Date;
    ubicacion?: string;
    descripcion?: string;
    numeroPlazas?: number;
    costeTotalEstimado?: number;
    costeTotalReal?: number;
    participantes?: Set<Socio>;
    numInscritos?: number;
    numEnEspera?: number;
    plazasLibres?: number; // -1 = ilimitado
    inscripcionCerrada?: boolean;
    isCurrentUserInscrito?: boolean;
}
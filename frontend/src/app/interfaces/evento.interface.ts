import {Socio} from "@/interfaces/socio.interface";

export interface Evento {
    uid?: string;
    nombreEvento: string;
    fechaEvento: Date;
    fechaLimiteInscripcion?: Date;
    ubicacion?: string;
    descripcion?: string;
    numeroPlazas?: number;
    /** Lo que paga cada socio por la plaza del evento. */
    costePlaza?: number;
    /** Lo que paga por la plaza quien no es socio. Vacío: lo mismo que un socio. */
    costePlazaNoSocio?: number;
    /** Lo que paga quien va con uno de los carnets sorteados. */
    costeCarnet?: number;
    /** true si el sorteo admite también a quien no es socio (enlace público). */
    sorteoAbiertoATodos?: boolean | null;
    /** true si entrar en el bombo no ocupa plaza de autobús: se reservan las de los carnets. */
    plazasCarnetReservadas?: boolean | null;
    /** Carnets que se sortean (0 o vacío = el evento no sortea carnets). */
    plazasCarnet?: number;
    /** Momento en que se celebra el sorteo de carnets. */
    fechaSorteoCarnet?: Date;
    costeTotalEstimado?: number;
    costeTotalReal?: number;
    participantes?: Set<Socio>;
    numInscritos?: number;
    numEnEspera?: number;
    plazasLibres?: number; // -1 = ilimitado
    inscripcionCerrada?: boolean;
    /** true si el sorteo de carnets ya se ha celebrado (solo en la vista de gestión). */
    sorteoCelebrado?: boolean;
    isCurrentUserInscrito?: boolean;
}
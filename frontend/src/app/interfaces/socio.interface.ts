import {Cuota} from "@/interfaces/cuota.interface";

export interface Pena {
    id: number;
    nombre: string;
    logo?: string;
    lema?: string;
    color?: string;
    iniciadorId?: string;
    direccion1?: string;
    direccion2?: string;
    cuentaIban?: string;
    cuentaBic?: string;
    cuotaAdulto?: number;
    cuotaMenor?: number;
    edadMayoria?: number;
    edadJubilacion?: number;
    /** Inscripciones que van forzadas a lista de espera por cada falta. 0 desactiva el castigo. */
    eventosPenalizacionPorFalta?: number;
}

/** Datos editables al crear/actualizar una peña (sin id, lo pone el backend). */
export type PenaRequest = Omit<Pena, 'id'>;

export interface Socio {
    uid?: string;
    numeroSocio: string;
    nombre: string;
    apellidos: string;
    fechaNacimiento: string | Date;
    dni: string;
    direccion?: string;
    poblacion?: string;
    provincia?: string;
    codigoPostal?: string;
    telefono?: string;
    email: string;
    fechaAlta: string;
    numeroCuenta: string;
    activo: boolean;
    abonadoBetis: boolean;
    accionistaBetis: boolean;
    observaciones?: string;
    cuotas: Cuota[];
}

export interface EstadisticasSocio {
    totalSocios: number;
    nuevosSocios: number;
    totalSociosJovenes: number;
    edadMayoria: number;
    totalSociosJubilados: number;
    edadJubilacion: number;
    totalImpagados: number;
}

export interface CarnetDto {
    penaInfo: Pena;
    socios: Socio[];
}

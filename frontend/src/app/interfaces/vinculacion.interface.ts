import { Socio } from '@/interfaces/socio.interface';

/**
 * Resultado del registro público. Si el email ya figuraba en el listado de socios de la peña no se
 * crea nada todavía (`requiereVerificacion`): se envía un correo con un enlace de un solo uso para
 * confirmar que es esa persona y vincular su ficha a la cuenta nueva.
 */
export interface RegisterResponse {
    requiereVerificacion: boolean;
    socio: Socio | null;
}

/** Datos de la invitación de vinculación que corresponde al token del enlace del correo. */
export interface VinculacionInfo {
    email: string;
    nombreSocio: string;
    numeroSocio: number;
    nombrePena: string | null;
    /** Número de fichas de socio que se vincularán a la cuenta (una persona puede tener varias). */
    fichas: number;
    /** true si la invitación no venía de un registro y hay que pedir la contraseña aquí. */
    requierePassword: boolean;
}

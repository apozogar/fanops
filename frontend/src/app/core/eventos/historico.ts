import { deFechaLocal } from '@/core/fechas/fechas-locales';

type FechaEvento = Date | string | null | undefined;

/**
 * true si el evento ya es histórico: su día pasó hace más de uno. El margen de un día deja el
 * evento a la vista el día siguiente (para pasar lista, cobrar o resolver lo que quede) y solo
 * después lo manda al histórico: uno del 14 sigue vigente el 15 y es histórico desde el 16.
 *
 * @param fechaEvento día del evento; un `yyyy-MM-dd` se lee como día local
 * @param ahora       momento de referencia (solo se pasa en los tests)
 */
export function esHistorico(fechaEvento: FechaEvento, ahora: Date = new Date()): boolean {
    const dia = deFechaLocal(fechaEvento);
    if (!dia || isNaN(dia.getTime())) return false;

    const limite = new Date(dia.getFullYear(), dia.getMonth(), dia.getDate() + 1);
    const hoy = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate());
    return limite < hoy;
}

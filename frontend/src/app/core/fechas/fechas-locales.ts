/**
 * Fechas tal como las espera el backend, que las guarda sin zona horaria (`LocalDate` y
 * `LocalDateTime`).
 *
 * No se puede mandar un `Date` sin más: al serializarlo a JSON se convierte a UTC, y en España el
 * 14 de octubre a las 00:00 sale como `2026-10-13T22:00:00Z`. El backend se queda con la fecha y
 * guardaba el día 13. Aquí se escribe la fecha y la hora locales, que son las que ha elegido quien
 * rellena el formulario.
 */

type FechaEntrada = Date | string | null | undefined;

/** `yyyy-MM-dd` con el día local. Para campos `LocalDate`. */
export function aFechaLocal(fecha: FechaEntrada): string | null {
    const valor = comoDate(fecha);
    if (!valor) return null;
    return `${valor.getFullYear()}-${dos(valor.getMonth() + 1)}-${dos(valor.getDate())}`;
}

/** `yyyy-MM-ddTHH:mm:ss` con la hora local. Para campos `LocalDateTime`. */
export function aFechaHoraLocal(fecha: FechaEntrada): string | null {
    const valor = comoDate(fecha);
    if (!valor) return null;
    return `${aFechaLocal(valor)}T${dos(valor.getHours())}:${dos(valor.getMinutes())}:${dos(valor.getSeconds())}`;
}

/**
 * Lee una fecha que llega del backend. Un `yyyy-MM-dd` suelto se interpreta como día local:
 * `new Date('2026-10-14')` lo tomaría como medianoche UTC, que al oeste de Greenwich ya es el 13.
 */
export function deFechaLocal(texto: FechaEntrada): Date | null {
    if (texto == null || texto === '') return null;
    if (texto instanceof Date) return texto;
    const soloFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(texto);
    if (soloFecha) {
        return new Date(Number(soloFecha[1]), Number(soloFecha[2]) - 1, Number(soloFecha[3]));
    }
    return new Date(texto);
}

function comoDate(fecha: FechaEntrada): Date | null {
    const valor = deFechaLocal(fecha);
    return valor && !isNaN(valor.getTime()) ? valor : null;
}

function dos(valor: number): string {
    return valor.toString().padStart(2, '0');
}

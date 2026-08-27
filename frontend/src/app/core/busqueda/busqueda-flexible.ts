/**
 * Búsqueda flexible para los listados del front.
 *
 * Tolera acentos, mayúsculas, signos de puntuación y, sobre todo, el orden de las
 * palabras: buscando "Alberto Pozo" aparece "Pozo Garcia, Alberto". Cada palabra
 * de la consulta puede caer en un campo distinto de la fila.
 */

/** Deja el texto en minúsculas, sin acentos ni signos, con las palabras separadas por un espacio. */
export function normalizarTexto(valor: unknown): string {
    if (valor === null || valor === undefined) {
        return '';
    }
    return String(valor)
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, ' ')
        .trim();
}

/** Trocea la consulta en palabras normalizadas; una consulta vacía no devuelve ninguna. */
export function tokenizarConsulta(consulta: string): string[] {
    const normalizada = normalizarTexto(consulta);
    return normalizada ? normalizada.split(' ') : [];
}

/**
 * Cierto si todas las palabras de la consulta aparecen en los campos indicados.
 *
 * Las palabras solo numéricas se comparan además contra los dígitos de la fila sin
 * separadores, para que "600123456" encuentre un teléfono guardado como "600 12 34 56".
 * Una consulta vacía deja pasar la fila.
 */
export function coincideBusqueda(campos: readonly unknown[], consulta: string): boolean {
    const tokens = tokenizarConsulta(consulta);
    if (!tokens.length) {
        return true;
    }

    const texto = campos.map(normalizarTexto).filter(Boolean).join(' ');
    const digitos = texto.replace(/[^0-9]/g, '');

    return tokens.every((token) => texto.includes(token) || (/^[0-9]+$/.test(token) && digitos.includes(token)));
}

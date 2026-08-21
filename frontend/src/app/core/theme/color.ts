/**
 * Utilidades de color para generar la escala de acento a partir del color de una peña.
 *
 * Se calcula aquí en lugar de usar el helper `palette` de PrimeNG por dos razones: no está
 * expuesto en sus tipos, y no queremos que nuestros tokens dependan de internos de PrimeNG.
 * Las proporciones replican las suyas (pasos lineales de tinte y sombra) para que los widgets
 * de PrimeNG que conservamos y los componentes propios generen exactamente la misma escala.
 */

export type ColorScale = Record<'50' | '100' | '200' | '300' | '400' | '500' | '600' | '700' | '800' | '900' | '950', string>;

/** Verde bético: acento por defecto cuando la peña no define color. */
export const DEFAULT_ACCENT = '#008835';

/** Proporción de mezcla con blanco para los tonos por debajo del base. */
const TINTS: ReadonlyArray<[keyof ColorScale, number]> = [
    ['50', 0.95],
    ['100', 0.76],
    ['200', 0.57],
    ['300', 0.38],
    ['400', 0.19]
];

/** Proporción de mezcla con negro para los tonos por encima del base. */
const SHADES: ReadonlyArray<[keyof ColorScale, number]> = [
    ['600', 0.15],
    ['700', 0.3],
    ['800', 0.45],
    ['900', 0.6],
    ['950', 0.75]
];

interface Rgb {
    r: number;
    g: number;
    b: number;
}

/**
 * Convierte un color hexadecimal (#rgb o #rrggbb, con o sin almohadilla) a RGB.
 * Devuelve null si la cadena no es un hexadecimal válido, de modo que quien llame pueda
 * caer en el color por defecto en lugar de pintar algo roto.
 */
export function parseHex(value: string | null | undefined): Rgb | null {
    if (!value) {
        return null;
    }

    const hex = value.trim().replace(/^#/, '');

    if (!/^([0-9a-f]{3}|[0-9a-f]{6})$/i.test(hex)) {
        return null;
    }

    const full = hex.length === 3 ? hex.replace(/./g, (c) => c + c) : hex;

    return {
        r: parseInt(full.slice(0, 2), 16),
        g: parseInt(full.slice(2, 4), 16),
        b: parseInt(full.slice(4, 6), 16)
    };
}

function toHex({ r, g, b }: Rgb): string {
    const part = (n: number) => Math.round(Math.min(255, Math.max(0, n))).toString(16).padStart(2, '0');
    return `#${part(r)}${part(g)}${part(b)}`;
}

/** Mezcla con blanco: 0 devuelve el color original, 1 devuelve blanco. */
function tint(color: Rgb, amount: number): Rgb {
    return {
        r: color.r + (255 - color.r) * amount,
        g: color.g + (255 - color.g) * amount,
        b: color.b + (255 - color.b) * amount
    };
}

/** Mezcla con negro: 0 devuelve el color original, 1 devuelve negro. */
function shade(color: Rgb, amount: number): Rgb {
    return {
        r: color.r * (1 - amount),
        g: color.g * (1 - amount),
        b: color.b * (1 - amount)
    };
}

/**
 * Construye la escala 50–950 a partir de un color base, que queda como el tono 500.
 * Si el color no es válido se usa {@link DEFAULT_ACCENT}.
 */
export function buildScale(baseHex: string | null | undefined): ColorScale {
    const base = parseHex(baseHex) ?? parseHex(DEFAULT_ACCENT)!;

    const scale = { '500': toHex(base) } as ColorScale;

    for (const [key, amount] of TINTS) {
        scale[key] = toHex(tint(base, amount));
    }

    for (const [key, amount] of SHADES) {
        scale[key] = toHex(shade(base, amount));
    }

    return scale;
}

/**
 * Luminancia relativa según WCAG, usada para decidir si el texto sobre el acento debe ser
 * claro u oscuro. Así un color de peña muy claro (por ejemplo amarillo) no acaba con texto
 * blanco ilegible encima.
 */
export function relativeLuminance(color: Rgb): number {
    const channel = (value: number) => {
        const c = value / 255;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    };

    return 0.2126 * channel(color.r) + 0.7152 * channel(color.g) + 0.0722 * channel(color.b);
}

/** Devuelve un color de texto legible (claro u oscuro) para usar sobre el color dado. */
export function readableForegroundFor(hex: string, dark = '#0f172a', light = '#ffffff'): string {
    const rgb = parseHex(hex);

    if (!rgb) {
        return light;
    }

    return relativeLuminance(rgb) > 0.45 ? dark : light;
}

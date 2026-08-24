import { Injectable, signal } from '@angular/core';
import { updatePrimaryPalette, updateSurfacePalette } from '@primeuix/themes';
import { buildScale, ColorScale, DEFAULT_ACCENT, readableForegroundFor } from './color';

/**
 * Clave de la preferencia de tema oscuro que guardaba la versión anterior. Se borra al arrancar
 * para no dejar basura en el navegador de quien ya la tenía puesta.
 */
const STORAGE_KEY_DARK_OBSOLETA = 'fanops.theme.dark';

/** Clase del tema oscuro retirado. Se limpia por si quedó puesta de una sesión anterior. */
const DARK_CLASS_OBSOLETA = 'app-dark';

/**
 * Escala neutra, espejo de --fo-neutral-* en theme.css. Se replica aquí para poder
 * alinear las superficies de los widgets de PrimeNG que conservamos (tabla, diálogo,
 * datepicker) con las nuestras y que no canten al lado de los componentes propios.
 */
const NEUTRAL_SCALE: ColorScale = {
    '50': '#f8fafc',
    '100': '#f1f5f9',
    '200': '#e2e8f0',
    '300': '#cbd5e1',
    '400': '#94a3b8',
    '500': '#64748b',
    '600': '#475569',
    '700': '#334155',
    '800': '#1e293b',
    '900': '#0f172a',
    '950': '#020817'
};

/**
 * Único punto de control del aspecto de la aplicación: el color de acento.
 *
 * El acento se toma del campo `color` de la peña, así que cada peña tiñe su propia app.
 * Escribe la escala como variables CSS en <html> (que sobreescriben los valores por defecto
 * de theme.css) y, además, la propaga a los tokens de PrimeNG para los widgets que quedan.
 *
 * La aplicación tiene un único tema, claro. El modo oscuro se retiró: mantenerlo obligaba a
 * revisar cada pantalla en dos paletas y el resultado no era bueno. Las utilidades `dark:` que
 * quedan en algunas plantillas son inertes, porque la variante que las activa
 * (`@custom-variant dark` en tailwind.css) apunta a una clase que ya nunca se aplica.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
    /** Color de acento actual en hexadecimal. */
    readonly accent = signal<string>(DEFAULT_ACCENT);

    private primeNgSynced = false;

    constructor() {
        this.limpiarRestosDelTemaOscuro();
        this.applyAccent(this.accent());
    }

    /**
     * Fija el color de acento a partir del color de una peña. Un valor nulo o no
     * hexadecimal cae al verde bético por defecto.
     */
    setAccent(color: string | null | undefined): void {
        const scale = buildScale(color);
        this.accent.set(scale['500']);
        this.applyAccent(scale['500'], scale);
    }

    private applyAccent(base: string, precomputed?: ColorScale): void {
        const scale = precomputed ?? buildScale(base);
        const root = document.documentElement;

        for (const [tone, value] of Object.entries(scale)) {
            root.style.setProperty(`--fo-accent-${tone}`, value);
        }

        // El texto sobre el acento se calcula para mantener contraste: con un color de peña
        // claro (amarillo, blanco roto…) el blanco sería ilegible.
        root.style.setProperty('--fo-accent-fg', readableForegroundFor(scale['500']));

        this.syncPrimeNg(scale);
    }

    /**
     * Propaga la paleta a los tokens de PrimeNG. Va en try/catch porque depende de que su
     * tema ya esté inicializado; si fallara, los componentes propios (que son la mayoría
     * del shell) siguen bien tematizados y solo los widgets de PrimeNG quedarían con su
     * paleta de serie.
     */
    private syncPrimeNg(accentScale: ColorScale): void {
        try {
            updatePrimaryPalette(accentScale);

            if (!this.primeNgSynced) {
                updateSurfacePalette(NEUTRAL_SCALE);
                this.primeNgSynced = true;
            }
        } catch {
            // Sin tema de PrimeNG disponible todavía; no es crítico.
        }
    }

    /**
     * Deja el navegador en el único tema que existe ahora. Quien usara la aplicación con el tema
     * oscuro activo tiene la clase puesta en <html> por el script anterior y la preferencia
     * guardada; sin esta limpieza se quedaría con una interfaz oscura a medias que ya no se
     * puede desactivar desde ningún sitio.
     */
    private limpiarRestosDelTemaOscuro(): void {
        document.documentElement.classList.remove(DARK_CLASS_OBSOLETA);

        try {
            localStorage.removeItem(STORAGE_KEY_DARK_OBSOLETA);
        } catch {
            // Modo privado o almacenamiento inaccesible: da igual, la clase ya está quitada.
        }
    }
}

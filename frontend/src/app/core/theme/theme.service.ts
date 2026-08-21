import { Injectable, signal } from '@angular/core';
import { updatePrimaryPalette, updateSurfacePalette } from '@primeuix/themes';
import { buildScale, ColorScale, DEFAULT_ACCENT, readableForegroundFor } from './color';

const DARK_CLASS = 'app-dark';
const STORAGE_KEY_DARK = 'fanops.theme.dark';

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
 * Único punto de control del aspecto de la aplicación: modo claro/oscuro y color de acento.
 *
 * El acento se toma del campo `color` de la peña, así que cada peña tiñe su propia app.
 * Escribe la escala como variables CSS en <html> (que sobreescriben los valores por defecto
 * de theme.css) y, además, la propaga a los tokens de PrimeNG para los widgets que quedan.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
    /** true si está activo el tema oscuro. */
    readonly isDark = signal<boolean>(false);

    /** Color de acento actual en hexadecimal. */
    readonly accent = signal<string>(DEFAULT_ACCENT);

    private primeNgSynced = false;

    constructor() {
        this.isDark.set(this.readStoredDarkPreference());
        this.applyDarkClass();
        this.applyAccent(this.accent());
    }

    toggleDark(): void {
        this.setDark(!this.isDark());
    }

    setDark(dark: boolean): void {
        this.isDark.set(dark);
        this.applyDarkClass();

        try {
            localStorage.setItem(STORAGE_KEY_DARK, dark ? '1' : '0');
        } catch {
            // Modo privado o almacenamiento lleno: el tema simplemente no se recuerda.
        }
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

    private applyDarkClass(): void {
        document.documentElement.classList.toggle(DARK_CLASS, this.isDark());
    }

    /** Preferencia guardada; si no hay ninguna, se respeta la del sistema operativo. */
    private readStoredDarkPreference(): boolean {
        try {
            const stored = localStorage.getItem(STORAGE_KEY_DARK);

            if (stored === '1') {
                return true;
            }

            if (stored === '0') {
                return false;
            }
        } catch {
            // Ignorado: caemos en la preferencia del sistema.
        }

        return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
    }
}

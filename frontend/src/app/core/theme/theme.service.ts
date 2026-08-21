import { Injectable, signal } from '@angular/core';
import { updatePrimaryPalette, updateSurfacePalette } from '@primeuix/themes';
import { buildScale, ColorScale, DEFAULT_ACCENT, readableForegroundFor } from './color';

const DARK_CLASS = 'app-dark';
const TRANSITION_LOCK_CLASS = 'fo-theme-switching';
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
        this.withoutTransitions(() => {
            document.documentElement.classList.toggle(DARK_CLASS, this.isDark());
        });
        this.syncBrowserThemeColor();
    }

    /**
     * Aplica un cambio de tema con las transiciones desactivadas (ver .fo-theme-switching en
     * base.scss). Al cambiar toda la paleta de golpe, las transiciones de color se quedaban
     * colgadas y dejaban botones y superficies con el color del tema anterior.
     */
    private withoutTransitions(apply: () => void): void {
        const root = document.documentElement;
        root.classList.add(TRANSITION_LOCK_CLASS);
        apply();
        // Se fuerza el recálculo de estilos para que el cambio quede ya aplicado sin
        // transición; después se vuelven a habilitar.
        void root.offsetHeight;

        // Se usa setTimeout y no requestAnimationFrame: rAF no se ejecuta mientras la pestaña
        // está oculta o no está pintando, y la clase se quedaría puesta dejando la aplicación
        // sin transiciones. El recálculo forzado de arriba ya garantiza que no queda ninguna
        // transición pendiente que pudiera arrancar al quitarla.
        setTimeout(() => root.classList.remove(TRANSITION_LOCK_CLASS), 50);
    }

    /**
     * Alinea el color de la barra del navegador en móvil con el tema activo. Sin esto, en un
     * teléfono con el sistema en oscuro la barra saldría oscura sobre una app clara.
     */
    private syncBrowserThemeColor(): void {
        const meta = document.querySelector<HTMLMetaElement>('meta[name="theme-color"]');

        if (!meta) {
            return;
        }

        // Se lee del token en lugar de fijar el color a mano para que no se desincronice
        // si cambian las superficies en theme.scss.
        const surface = getComputedStyle(document.documentElement).getPropertyValue('--fo-surface').trim();

        if (surface) {
            meta.setAttribute('content', surface);
        }
    }

    /**
     * Preferencia guardada por el usuario. Si no hay ninguna, se arranca en tema claro: es el
     * defecto de la aplicación, deliberadamente por encima de la preferencia del sistema.
     */
    private readStoredDarkPreference(): boolean {
        try {
            return localStorage.getItem(STORAGE_KEY_DARK) === '1';
        } catch {
            // Modo privado o almacenamiento inaccesible: tema claro.
            return false;
        }
    }
}

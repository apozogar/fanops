import { Directive, computed, input } from '@angular/core';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'success' | 'warning' | 'info' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg' | 'icon' | 'icon-sm';

const BASE =
    'inline-flex items-center justify-center gap-2 font-medium rounded-token border ' +
    'transition-colors select-none whitespace-nowrap cursor-pointer ' +
    'disabled:opacity-50 disabled:pointer-events-none';

/**
 * Solo el primario es sólido, y va con el color de acento de la peña: así hay una única acción
 * dominante por pantalla. El resto de intenciones (éxito, aviso, información, peligro) se
 * resuelven con fondo teñido, que informa del tono sin competir con el primario.
 */
const VARIANTS: Record<ButtonVariant, string> = {
    primary: 'bg-accent border-transparent text-accent-fg hover:bg-accent-hover',
    secondary: 'bg-surface border-line text-ink hover:bg-surface-hover',
    ghost: 'bg-transparent border-transparent text-ink-muted hover:bg-surface-hover hover:text-ink',
    success: 'bg-success-soft border-transparent text-success-soft-fg hover:brightness-95',
    warning: 'bg-warn-soft border-transparent text-warn-soft-fg hover:brightness-95',
    info: 'bg-info-soft border-transparent text-info-soft-fg hover:brightness-95',
    danger: 'bg-danger-soft border-transparent text-danger-soft-fg hover:brightness-95'
};

const SIZES: Record<ButtonSize, string> = {
    sm: 'text-sm px-2.5 py-1.5',
    md: 'text-sm px-3.5 py-2',
    lg: 'text-base px-5 py-2.5',
    // Cuadrado de 44px: el mínimo recomendado para un objetivo táctil.
    icon: 'p-0 w-11 h-11 shrink-0',
    // Variante compacta para acciones repetidas en filas de tabla, donde 44px apelmaza.
    'icon-sm': 'p-0 w-9 h-9 shrink-0'
};

/**
 * Botón propio, aplicable a cualquier `<button>` o `<a>` nativo.
 *
 * Sustituye a pButton: se estiliza por completo con los tokens de theme.scss, así que el color
 * de la peña lo tiñe sin tener que pelearse con el tema de PrimeNG.
 *
 * @example <button foButton variant="primary">Guardar</button>
 * @example <button foButton variant="ghost" size="icon" class="rounded-full">…</button>
 */
@Directive({
    selector: '[foButton]',
    standalone: true,
    host: {
        '[class]': 'classes()'
    }
})
export class UiButtonDirective {
    readonly variant = input<ButtonVariant>('secondary');
    readonly size = input<ButtonSize>('md');

    protected readonly classes = computed(() => `${BASE} ${VARIANTS[this.variant()]} ${SIZES[this.size()]}`);
}

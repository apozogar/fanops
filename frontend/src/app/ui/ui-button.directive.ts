import { Directive, computed, input } from '@angular/core';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg' | 'icon';

const BASE =
    'inline-flex items-center justify-center gap-2 font-medium rounded-token border ' +
    'transition-colors select-none whitespace-nowrap cursor-pointer ' +
    'disabled:opacity-50 disabled:pointer-events-none';

const VARIANTS: Record<ButtonVariant, string> = {
    primary: 'bg-accent border-transparent text-accent-fg hover:bg-accent-hover',
    secondary: 'bg-surface border-line text-ink hover:bg-surface-hover',
    ghost: 'bg-transparent border-transparent text-ink-muted hover:bg-surface-hover hover:text-ink',
    danger: 'bg-transparent border-line text-danger hover:bg-danger-soft'
};

const SIZES: Record<ButtonSize, string> = {
    sm: 'text-sm px-2.5 py-1.5',
    md: 'text-sm px-3.5 py-2',
    lg: 'text-base px-5 py-2.5',
    // Cuadrado y con 44px de lado: el mínimo recomendado para un objetivo táctil.
    icon: 'p-0 w-11 h-11 shrink-0'
};

/**
 * Botón propio, aplicable a cualquier `<button>` o `<a>` nativo.
 *
 * Sustituye a pButton para no depender del tema de PrimeNG en los controles básicos: se
 * estiliza por completo con los tokens de theme.css.
 *
 * @example <button foButton variant="primary" size="md">Guardar</button>
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

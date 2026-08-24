import { Directive, booleanAttribute, computed, input } from '@angular/core';

/**
 * Campo de texto propio, aplicable a cualquier `<input>`, `<textarea>` o `<select>` nativo.
 *
 * Sustituye a `pInputText`: se estiliza solo con los tokens de theme.scss, así que el color de
 * la peña tiñe el foco sin pelearse con el tema de PrimeNG, y el alto lo fija un `py` propio en
 * lugar del de la plantilla, que en móvil quedaba por debajo del objetivo táctil recomendado.
 *
 * El ancho es siempre 100% del contenedor a propósito: el `md:w-120` que traían las pantallas de
 * autenticación hacía la tarjeta más ancha que su marco y descuadraba el borde en pantallas
 * estrechas. Quien necesite limitar el ancho lo hace en el contenedor, no en el campo.
 *
 * @example <input foInput type="email" placeholder="tu@email.com" />
 * @example <input foInput [invalid]="!!error" [(ngModel)]="email" />
 */
@Directive({
    selector: 'input[foInput], textarea[foInput], select[foInput]',
    standalone: true,
    host: {
        '[class]': 'classes()',
        '[attr.aria-invalid]': 'invalid() ? true : null'
    }
})
export class UiInputDirective {
    /** Marca el campo como erróneo: borde y anillo de foco en color de peligro. */
    readonly invalid = input(false, { transform: booleanAttribute });

    /** Reserva sitio a la derecha para un botón superpuesto (el ojo de `fo-password`). */
    readonly trailingSlot = input(false, { transform: booleanAttribute });

    protected readonly classes = computed(() => {
        const base =
            'w-full rounded-token border bg-surface text-ink placeholder:text-ink-subtle ' +
            // 16px de tamaño de fuente en móvil: por debajo de eso Safari en iOS hace zoom al
            // enfocar el campo y descuadra toda la pantalla.
            // Sin transition-colors a propósito: el borde cambia de --fo-border a --fo-danger al
            // marcar el campo como erróneo, y la transición entre dos colores que vienen de
            // cadenas de var() distintas se queda colgada en Chrome. El resultado era un campo
            // con la clase border-danger puesta pero pintado del color normal hasta que algo forzaba un
            // recálculo de estilos. El estado de error debe verse al instante, así que no se anima.
            'text-base sm:text-sm py-2.5 px-3 outline-none ' +
            'disabled:opacity-50 disabled:cursor-not-allowed';

        const state = this.invalid() ? 'border-danger focus:border-danger focus:ring-2 focus:ring-danger/25' : 'border-line hover:border-line-strong focus:border-accent focus:ring-2 focus:ring-accent/25';

        return `${base} ${state} ${this.trailingSlot() ? 'pr-11' : ''}`;
    });
}

import { Component, computed, inject, input } from '@angular/core';
import { ActivePenaService } from '@/core/pena/active-pena.service';
import { IconComponent } from '@/ui/icon/icon.component';

/**
 * Selector de peña para el superadmin.
 *
 * Usa un `<select>` nativo a propósito: en móvil abre el selector del sistema (más cómodo y
 * accesible que cualquier dropdown propio) y no arrastra ningún componente de PrimeNG.
 */
@Component({
    selector: 'fo-pena-switcher',
    standalone: true,
    imports: [IconComponent],
    template: `
        <label class="relative flex items-center">
            <span class="sr-only">Peña activa</span>
            <select
                class="w-full appearance-none rounded-token border border-line bg-surface py-2 pl-3 pr-9 text-sm text-ink transition-colors hover:bg-surface-hover disabled:opacity-60"
                [class.max-w-56]="!block()"
                [disabled]="activePena.loading() || activePena.options().length === 0"
                (change)="onChange($event)"
                aria-label="Peña activa"
            >
                <option value="" [selected]="penaActivaId() === null" disabled>
                    {{ activePena.loading() ? 'Cargando peñas…' : 'Selecciona una peña' }}
                </option>
                @for (pena of activePena.options(); track pena.id) {
                    <!-- La marca de seleccionada va en la opción, no en el <select>.
                         Un [value] en el select se aplica antes de que este @for haya creado
                         las opciones (las peñas llegan por HTTP), así que el navegador lo
                         descartaba y acababa mostrando la primera de la lista. -->
                    <option [value]="pena.id" [selected]="pena.id === penaActivaId()">{{ pena.nombre }}</option>
                }
            </select>
            <fo-icon name="seleccionar" [size]="14" class="pointer-events-none absolute right-3 text-ink-muted" />
        </label>
    `
})
export class PenaSwitcherComponent {
    protected readonly activePena = inject(ActivePenaService);

    /** En true ocupa todo el ancho disponible (uso en el panel de cuenta de móvil). */
    readonly block = input<boolean>(false);

    /** Id de la peña activa, o null si todavía no hay ninguna. */
    protected readonly penaActivaId = computed(() => this.activePena.pena()?.id ?? null);

    protected onChange(event: Event): void {
        const value = (event.target as HTMLSelectElement).value;
        this.activePena.select(value ? Number(value) : null);
    }
}

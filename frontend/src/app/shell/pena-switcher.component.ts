import { Component, inject, input } from '@angular/core';
import { ActivePenaService } from '@/core/pena/active-pena.service';

/**
 * Selector de peña para el superadmin.
 *
 * Usa un `<select>` nativo a propósito: en móvil abre el selector del sistema (más cómodo y
 * accesible que cualquier dropdown propio) y no arrastra ningún componente de PrimeNG.
 */
@Component({
    selector: 'fo-pena-switcher',
    standalone: true,
    template: `
        <label class="relative flex items-center">
            <span class="sr-only">Peña activa</span>
            <select
                class="w-full appearance-none rounded-token border border-line bg-surface py-2 pl-3 pr-9 text-sm text-ink transition-colors hover:bg-surface-hover disabled:opacity-60"
                [class.max-w-56]="!block()"
                [disabled]="activePena.loading() || activePena.options().length === 0"
                [value]="activePena.pena()?.id ?? ''"
                (change)="onChange($event)"
                aria-label="Peña activa"
            >
                <option value="" disabled>
                    {{ activePena.loading() ? 'Cargando peñas…' : 'Selecciona una peña' }}
                </option>
                @for (pena of activePena.options(); track pena.id) {
                    <option [value]="pena.id">{{ pena.nombre }}</option>
                }
            </select>
            <i class="pi pi-chevron-down pointer-events-none absolute right-3 text-xs text-ink-muted" aria-hidden="true"></i>
        </label>
    `
})
export class PenaSwitcherComponent {
    protected readonly activePena = inject(ActivePenaService);

    /** En true ocupa todo el ancho disponible (uso en el panel de cuenta de móvil). */
    readonly block = input<boolean>(false);

    protected onChange(event: Event): void {
        const value = (event.target as HTMLSelectElement).value;
        this.activePena.select(value ? Number(value) : null);
    }
}

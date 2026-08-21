import { Component, computed, input } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { FO_ICONS, FoIconName } from './icon-registry';

/**
 * Icono de la aplicación.
 *
 * Sustituye a la fuente primeicons (`<i class="pi pi-...">`) por SVG en línea, que es lo que
 * hace personalizable el icono: hereda el color del texto con `currentColor`, permite ajustar
 * el grosor del trazo y el tamaño, y cualquier icono se puede reemplazar tocando solo el
 * registro. Los nombres están tipados, así que una errata no llega a ejecución.
 *
 * @example <fo-icon name="socios" />
 * @example <fo-icon name="eliminar" [size]="16" label="Eliminar socio" />
 */
@Component({
    selector: 'fo-icon',
    standalone: true,
    imports: [LucideDynamicIcon],
    host: {
        class: 'inline-flex shrink-0 items-center justify-center'
    },
    template: `
        <svg [lucideIcon]="icon()" [size]="size()" [strokeWidth]="strokeWidth()" [title]="label()"></svg>
    `
})
export class IconComponent {
    readonly name = input.required<FoIconName>();

    /** Lado del icono en píxeles. */
    readonly size = input<number>(18);

    /**
     * Grosor del trazo. Algo por debajo del 2 de Lucide: a los tamaños pequeños que usa la
     * interfaz, 1.75 se ve más limpio sin perder presencia.
     */
    readonly strokeWidth = input<number>(1.75);

    /**
     * Etiqueta accesible. Solo hace falta cuando el icono transmite información por sí mismo:
     * si se omite, Lucide lo marca como aria-hidden, que es lo correcto para un icono
     * decorativo dentro de un botón que ya tiene texto o aria-label.
     */
    readonly label = input<string | null>(null);

    protected readonly icon = computed(() => FO_ICONS[this.name()]);
}

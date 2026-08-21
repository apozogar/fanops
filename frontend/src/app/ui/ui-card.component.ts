import { Component, input } from '@angular/core';

/**
 * Tarjeta contenedora. Equivalente a la clase global `.card` que usan las páginas
 * heredadas, pero como componente, con cabecera opcional.
 */
@Component({
    selector: 'fo-card',
    standalone: true,
    template: `
        <section class="bg-surface border border-line rounded-token-lg shadow-sm">
            @if (heading() || subheading()) {
                <header class="px-4 md:px-6 pt-4 md:pt-5 pb-3 border-b border-line">
                    @if (heading()) {
                        <h2 class="text-base font-semibold">{{ heading() }}</h2>
                    }
                    @if (subheading()) {
                        <p class="text-sm text-ink-muted mt-0.5">{{ subheading() }}</p>
                    }
                </header>
            }
            <div [class]="bodyClass()">
                <ng-content />
            </div>
        </section>
    `
})
export class UiCardComponent {
    readonly heading = input<string | null>(null);
    readonly subheading = input<string | null>(null);
    /** Desactiva el relleno interior, útil cuando el contenido es una tabla a sangre. */
    readonly flush = input<boolean>(false);

    protected bodyClass() {
        return this.flush() ? '' : 'p-4 md:p-6';
    }
}

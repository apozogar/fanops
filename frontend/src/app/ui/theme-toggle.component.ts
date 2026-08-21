import { Component, inject, input } from '@angular/core';
import { ThemeService } from '@/core/theme/theme.service';
import { UiButtonDirective } from './ui-button.directive';
import { IconComponent } from './icon/icon.component';

/**
 * Botón de cambio de tema claro/oscuro.
 *
 * Reemplaza al `app-floating-configurator` de la plantilla (que además arrastraba el
 * selector de presets de PrimeNG). Con `floating` en true se posiciona fijo en la esquina,
 * que es como lo usan las pantallas de autenticación.
 */
@Component({
    selector: 'fo-theme-toggle',
    standalone: true,
    imports: [UiButtonDirective, IconComponent],
    template: `
        <div [class]="floating() ? 'fixed top-5 right-5 z-30' : 'contents'">
            <button
                type="button"
                foButton
                variant="secondary"
                size="icon"
                class="rounded-full"
                (click)="theme.toggleDark()"
                [attr.aria-label]="theme.isDark() ? 'Activar tema claro' : 'Activar tema oscuro'"
                [attr.aria-pressed]="theme.isDark()"
            >
                <fo-icon [name]="theme.isDark() ? 'tema-oscuro' : 'tema-claro'" />
            </button>
        </div>
    `
})
export class ThemeToggleComponent {
    protected readonly theme = inject(ThemeService);

    readonly floating = input<boolean>(false);
}

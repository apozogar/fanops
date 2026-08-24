import { Component, inject, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActivePenaService } from '@/core/pena/active-pena.service';
import { AuthService } from '@/pages/auth/auth.service';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { PenaSwitcherComponent } from './pena-switcher.component';

/**
 * Cabecera fija de la aplicación.
 *
 * En móvil se queda en lo imprescindible (identidad de la peña + botón de cuenta) y el resto
 * de controles viven en el panel de cuenta, para no apelotonar una barra de 3,5rem. En
 * escritorio muestra además el selector de peña y el cambio de tema.
 */
@Component({
    selector: 'fo-shell-header',
    standalone: true,
    imports: [RouterLink, UiButtonDirective, PenaSwitcherComponent],
    template: `
        <header
            class="fixed inset-x-0 top-0 z-30 flex items-center gap-3 border-b border-line bg-surface"
            style="
                height: calc(var(--fo-header-h) + var(--fo-safe-top));
                padding-top: var(--fo-safe-top);
                padding-left: max(0.75rem, var(--fo-safe-left));
                padding-right: max(0.75rem, var(--fo-safe-right));
            "
        >
            <a [routerLink]="homeRoute()" class="flex min-w-0 items-center gap-2.5" [attr.aria-label]="title()">
                <img [src]="logo()" alt="" class="h-8 w-8 shrink-0 rounded-token-sm object-contain" />
                <span class="truncate text-sm font-semibold md:text-base">{{ title() }}</span>
            </a>

            <div class="ml-auto flex items-center gap-2">
                @if (activePena.isSuperAdmin()) {
                    <!-- En móvil el selector se traslada al panel de cuenta -->
                    <div class="hidden lg:block">
                        <fo-pena-switcher />
                    </div>
                }

                <button type="button" foButton variant="ghost" size="icon" class="rounded-full" (click)="openAccount.emit()" aria-label="Abrir menú de cuenta">
                    <span class="flex h-8 w-8 items-center justify-center rounded-full bg-accent text-xs font-semibold uppercase text-accent-fg" aria-hidden="true">
                        {{ initial() }}
                    </span>
                </button>
            </div>
        </header>
    `
})
export class ShellHeaderComponent {
    protected readonly activePena = inject(ActivePenaService);
    private readonly penaPublica = inject(PenaPublicaService);
    private readonly auth = inject(AuthService);

    readonly openAccount = output<void>();

    protected title(): string {
        return this.activePena.pena()?.nombre ?? 'FanOps';
    }

    protected logo(): string {
        return this.activePena.pena()?.logo || 'assets/logo-fanops.png';
    }

    /** Inicio dentro de la peña actual: el logo nunca debe sacarte de su dominio. */
    protected homeRoute(): string[] {
        return this.penaPublica.ruta(this.activePena.isSuperAdmin() ? 'penas' : 'carnet-socio');
    }

    /** Misma inicial que muestra el panel de cuenta: la del email del usuario. */
    protected initial(): string {
        return (this.auth.getCurrentUser()?.sub?.trim()[0] ?? '?').toUpperCase();
    }
}

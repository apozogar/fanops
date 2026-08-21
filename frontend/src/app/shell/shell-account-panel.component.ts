import { Component, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@/pages/auth/auth.service';
import { ActivePenaService } from '@/core/pena/active-pena.service';
import { ThemeService } from '@/core/theme/theme.service';
import { PenaSwitcherComponent } from './pena-switcher.component';
import { IconComponent } from '@/ui/icon/icon.component';
import { NavItem } from './navigation';
import { ROLE_ADMIN, ROLE_SUPERADMIN } from '@/core/auth/roles';

/**
 * Panel de cuenta: datos del usuario, cambio de tema, selector de peña y cierre de sesión.
 *
 * Se adapta al dispositivo con el mismo marcado: hoja inferior deslizante en móvil (donde
 * llega el pulgar) y menú desplegable anclado a la cabecera en escritorio.
 */
@Component({
    selector: 'fo-shell-account-panel',
    standalone: true,
    imports: [RouterLink, PenaSwitcherComponent, IconComponent],
    host: {
        '(document:keydown.escape)': 'onEscape()'
    },
    template: `
        @if (open()) {
            <!-- Fondo oscurecido: cierra al pulsar fuera. En escritorio es transparente pero
                 sigue capturando el clic, que es lo que cierra el desplegable. -->
            <div class="fixed inset-0 z-40 bg-[var(--fo-overlay-bg)] lg:bg-transparent" (click)="close.emit()" aria-hidden="true"></div>

            <div
                class="fixed z-50 border border-line bg-surface shadow-[var(--fo-shadow-lg)]
                       inset-x-0 bottom-0 rounded-t-2xl
                       lg:inset-x-auto lg:bottom-auto lg:right-4 lg:w-80 lg:rounded-token-lg
                       lg:top-[calc(var(--fo-header-h)+var(--fo-safe-top)+0.5rem)]"
                style="padding-bottom: max(0.5rem, var(--fo-safe-bottom));"
                role="dialog"
                aria-label="Cuenta"
            >
                <!-- Asa visual de la hoja inferior, solo móvil -->
                <div class="flex justify-center pt-2.5 lg:hidden">
                    <span class="h-1 w-10 rounded-full bg-line-strong"></span>
                </div>

                <div class="flex items-center gap-3 px-4 py-4">
                    <span
                        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-accent text-sm font-semibold uppercase text-accent-fg"
                        aria-hidden="true"
                    >
                        {{ initial() }}
                    </span>
                    <div class="min-w-0">
                        <p class="truncate text-sm font-medium">{{ email() || 'Sesión iniciada' }}</p>
                        <p class="truncate text-xs text-ink-muted">{{ roleLabel() }}</p>
                    </div>
                </div>

                @if (activePena.isSuperAdmin()) {
                    <div class="border-t border-line px-4 py-3">
                        <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-ink-subtle">Peña activa</p>
                        <fo-pena-switcher [block]="true" />
                    </div>
                }

                @if (overflowItems().length > 0) {
                    <div class="border-t border-line py-2">
                        @for (item of overflowItems(); track item.id) {
                            <a
                                [routerLink]="item.route"
                                (click)="close.emit()"
                                class="flex items-center gap-3 px-4 py-3 text-sm text-ink transition-colors hover:bg-surface-hover"
                            >
                                <fo-icon [name]="item.icon" class="text-ink-muted" />
                                <span class="truncate">{{ item.label }}</span>
                            </a>
                        }
                    </div>
                }

                <div class="border-t border-line py-2">
                    <button
                        type="button"
                        (click)="theme.toggleDark()"
                        class="flex w-full items-center gap-3 px-4 py-3 text-left text-sm text-ink transition-colors hover:bg-surface-hover"
                    >
                        <fo-icon [name]="theme.isDark() ? 'tema-oscuro' : 'tema-claro'" class="text-ink-muted" />
                        <span>{{ theme.isDark() ? 'Tema claro' : 'Tema oscuro' }}</span>
                    </button>

                    <button
                        type="button"
                        (click)="logout()"
                        class="flex w-full items-center gap-3 px-4 py-3 text-left text-sm text-danger transition-colors hover:bg-danger-soft"
                    >
                        <fo-icon name="cerrar-sesion" />
                        <span>Cerrar sesión</span>
                    </button>
                </div>
            </div>
        }
    `
})
export class ShellAccountPanelComponent {
    private readonly auth = inject(AuthService);
    protected readonly activePena = inject(ActivePenaService);
    protected readonly theme = inject(ThemeService);

    readonly open = input<boolean>(false);
    /** Destinos que no caben en la barra de pestañas y se listan aquí. */
    readonly overflowItems = input<NavItem[]>([]);

    readonly close = output<void>();

    protected onEscape(): void {
        if (this.open()) {
            this.close.emit();
        }
    }

    protected email(): string {
        return this.auth.getCurrentUser()?.sub ?? '';
    }

    protected initial(): string {
        return (this.email().trim()[0] ?? '?').toUpperCase();
    }

    protected roleLabel(): string {
        const authorities = this.auth.getCurrentUser()?.authorities?.map((a) => a.authority) ?? [];

        if (authorities.includes(ROLE_SUPERADMIN)) {
            return 'Superadministrador';
        }

        if (authorities.includes(ROLE_ADMIN)) {
            return 'Administrador de la peña';
        }

        return 'Socio';
    }

    protected logout(): void {
        this.close.emit();
        this.auth.logout();
    }
}

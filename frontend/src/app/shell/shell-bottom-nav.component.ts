import { Component, inject, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { IconComponent } from '@/ui/icon/icon.component';
import { NavItem } from './navigation';

/**
 * Barra de pestañas inferior de móvil.
 *
 * Va abajo a propósito: es la zona alcanzable con el pulgar en un teléfono, al contrario que
 * una hamburguesa en la esquina superior. Respeta el safe-area inferior (barra de gestos de
 * iOS) para que las pestañas no queden debajo de ella.
 */
@Component({
    selector: 'fo-shell-bottom-nav',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, IconComponent],
    template: `
        <nav
            class="fixed inset-x-0 bottom-0 z-30 border-t border-line bg-surface lg:hidden"
            style="padding-bottom: var(--fo-safe-bottom); box-shadow: 0 -1px 3px rgb(15 23 42 / 6%);"
            aria-label="Navegación principal"
        >
            <ul class="flex items-stretch" style="height: var(--fo-tabbar-h);">
                @for (item of items(); track item.id) {
                    <li class="min-w-0 flex-1">
                        <a
                            [routerLink]="penaPublica.ruta(item.route)"
                            routerLinkActive="!text-accent"
                            #link="routerLinkActive"
                            [attr.aria-current]="link.isActive ? 'page' : null"
                            class="flex h-full flex-col items-center justify-center gap-1 px-1 text-ink-muted transition-colors active:bg-surface-hover"
                        >
                            <fo-icon [name]="item.icon" [size]="20" />
                            <span class="w-full truncate text-center text-[0.6875rem] leading-tight">{{ item.shortLabel }}</span>
                        </a>
                    </li>
                }
                @if (hasMore()) {
                    <li class="min-w-0 flex-1">
                        <button
                            type="button"
                            (click)="more.emit()"
                            class="flex h-full w-full flex-col items-center justify-center gap-1 px-1 text-ink-muted transition-colors active:bg-surface-hover"
                        >
                            <fo-icon name="mas-opciones" [size]="20" />
                            <span class="text-[0.6875rem] leading-tight">Más</span>
                        </button>
                    </li>
                }
            </ul>
        </nav>
    `
})
export class ShellBottomNavComponent {
    protected readonly penaPublica = inject(PenaPublicaService);
    readonly items = input.required<NavItem[]>();
    /** Muestra una pestaña "Más" que abre el panel con el resto de destinos. */
    readonly hasMore = input<boolean>(false);

    readonly more = output<void>();
}

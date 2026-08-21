import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '@/pages/auth/auth.service';
import { ActivePenaService } from '@/core/pena/active-pena.service';
import { MAX_TAB_ITEMS, buildNavigation, flattenNavigation } from './navigation';
import { ShellHeaderComponent } from './shell-header.component';
import { ShellSidebarComponent } from './shell-sidebar.component';
import { ShellBottomNavComponent } from './shell-bottom-nav.component';
import { ShellAccountPanelComponent } from './shell-account-panel.component';

/**
 * Shell de la aplicación: sustituye al layout de la plantilla de PrimeNG.
 *
 * En móvil, navegación por pestañas fijas abajo (zona del pulgar) y cabecera compacta arriba.
 * En escritorio, sidebar lateral persistente. El mismo modelo de navegación alimenta ambos,
 * y el punto de corte se decide con matchMedia reactivo, así que girar el dispositivo o
 * redimensionar la ventana reconfigura el shell al instante.
 */
@Component({
    selector: 'fo-shell',
    standalone: true,
    imports: [RouterOutlet, ShellHeaderComponent, ShellSidebarComponent, ShellBottomNavComponent, ShellAccountPanelComponent],
    template: `
        <div class="min-h-dvh bg-app">
            <fo-shell-header (openAccount)="accountOpen.set(true)" />

            <!-- Sidebar y barra de pestañas se renderizan siempre y se muestran por CSS
                 (hidden lg:flex / lg:hidden). Dejar la estructura en manos de las media
                 queries y no de un signal evita cualquier parpadeo o desajuste si la
                 detección de cambios llega tarde. -->
            <fo-shell-sidebar [sections]="sections()" />

            <main
                class="pt-[calc(var(--fo-header-h)+var(--fo-safe-top))]
                       pb-[calc(var(--fo-tabbar-h)+var(--fo-safe-bottom)+1rem)]
                       lg:pb-8 lg:pl-[var(--fo-sidebar-w)]"
            >
                <div
                    class="mx-auto w-full max-w-[var(--fo-content-max-w)] px-[max(1rem,var(--fo-safe-left))] py-4 md:px-6 md:py-6"
                >
                    <router-outlet />
                </div>
            </main>

            <fo-shell-bottom-nav [items]="tabItems()" [hasMore]="overflowItems().length > 0" (more)="accountOpen.set(true)" />

            <fo-shell-account-panel [open]="accountOpen()" [overflowItems]="overflowItems()" (close)="accountOpen.set(false)" />
        </div>
    `
})
export class ShellComponent {
    private readonly auth = inject(AuthService);
    private readonly router = inject(Router);
    private readonly activePena = inject(ActivePenaService);

    protected readonly accountOpen = signal(false);

    private readonly user = toSignal(this.auth.currentUser, { initialValue: null });

    protected readonly sections = computed(() => buildNavigation(this.authorities()));

    /** Destinos que caben en la barra de pestañas. */
    protected readonly tabItems = computed(() => {
        const all = flattenNavigation(this.sections());
        // Si no caben todos, se reserva la última posición para la pestaña "Más".
        return all.length <= MAX_TAB_ITEMS ? all : all.slice(0, MAX_TAB_ITEMS - 1);
    });

    /** Destinos que no caben y se listan en el panel de cuenta. */
    protected readonly overflowItems = computed(() => {
        const all = flattenNavigation(this.sections());
        return all.length <= MAX_TAB_ITEMS ? [] : all.slice(MAX_TAB_ITEMS - 1);
    });

    constructor() {
        this.activePena.init();

        // Al navegar se cierra el panel, igual que haría una app nativa.
        this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => this.accountOpen.set(false));

        // El fondo no debe desplazarse tras la hoja abierta. Que eso aplique solo en móvil
        // lo decide la media query de base.css, no este código.
        effect(() => {
            document.body.classList.toggle('fo-panel-open', this.accountOpen());
        });
    }

    private authorities(): string[] {
        return this.user()?.authorities?.map((authority) => authority.authority) ?? [];
    }
}

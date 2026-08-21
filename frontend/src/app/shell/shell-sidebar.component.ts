import { Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IconComponent } from '@/ui/icon/icon.component';
import { NavSection } from './navigation';

/** Navegación lateral de escritorio. En móvil no se renderiza: allí manda la barra de pestañas. */
@Component({
    selector: 'fo-shell-sidebar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, IconComponent],
    template: `
        <nav
            class="fixed left-0 z-20 hidden flex-col gap-6 overflow-y-auto border-r border-line bg-surface px-3 py-5 lg:flex"
            style="
                top: calc(var(--fo-header-h) + var(--fo-safe-top));
                width: var(--fo-sidebar-w);
                height: calc(100dvh - var(--fo-header-h) - var(--fo-safe-top));
                padding-left: max(0.75rem, var(--fo-safe-left));
            "
            aria-label="Navegación principal"
        >
            @for (section of sections(); track section.label) {
                <div class="flex flex-col gap-1">
                    <h2 class="px-3 pb-1 text-xs font-semibold uppercase tracking-wide text-ink-subtle">
                        {{ section.label }}
                    </h2>
                    @for (item of section.items; track item.id) {
                        <a
                            [routerLink]="item.route"
                            routerLinkActive="!bg-accent-soft !text-accent-soft-fg !font-semibold"
                            class="flex items-center gap-3 rounded-token px-3 py-2.5 text-sm text-ink-muted transition-colors hover:bg-surface-hover hover:text-ink"
                        >
                            <fo-icon [name]="item.icon" [size]="18" />
                            <span class="truncate">{{ item.label }}</span>
                        </a>
                    }
                </div>
            }
        </nav>
    `
})
export class ShellSidebarComponent {
    readonly sections = input.required<NavSection[]>();
}

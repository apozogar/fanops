import { Component, computed, inject, input } from '@angular/core';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { FoIconName } from '@/ui/icon/icon-registry';
import { IconComponent } from '@/ui/icon/icon.component';

/** Anchos del panel de formulario según el contenido que va a alojar. */
const WIDTHS = {
    /** Formularios de una columna: inicio de sesión, recuperar contraseña. */
    sm: 'max-w-sm',
    /** Formularios de dos columnas en escritorio: registro. */
    md: 'max-w-md',
    /** Fichas largas: completar perfil. */
    lg: 'max-w-2xl'
} as const;

export type AuthShellWidth = keyof typeof WIDTHS;

/**
 * Lienzo común de todas las pantallas de autenticación.
 *
 * Sustituye al marco heredado de la plantilla (una tarjeta con `border-radius: 56px` y un
 * degradado en línea) que tenía dos problemas en móvil: el relleno era de escritorio
 * (`py-20 px-8 sm:px-20`) y el degradado envolvía una tarjeta más ancha que él, así que el
 * borde se cortaba por los lados.
 *
 * En su lugar la pantalla se parte en dos: un panel de marca con el color de la peña y la
 * columna del formulario. Por debajo de `lg` el panel se colapsa a una cabecera fina, de modo
 * que en un teléfono la pantalla es solo el formulario a ancho completo.
 *
 * El panel de marca se pinta sobre los tonos 800-950 del acento y con texto blanco fijo: son
 * oscuros para cualquier color de peña, así que el contraste se mantiene incluso con un acento
 * claro (amarillo, blanco roto...), donde `--fo-accent-fg` sería un texto oscuro.
 *
 * Cuando se ha entrado por el dominio de una peña (/mi-pena/auth/login) el panel muestra su
 * nombre, su logo y su lema; el acento ya lo ha puesto PenaPublicaService al resolver el dominio.
 * Entrando por la raíz se muestra la marca genérica de FanOps.
 *
 * @example
 * <fo-auth-shell title="Inicia sesión" subtitle="Accede con tu cuenta">
 *     <form>...</form>
 *     <div authFooter>...</div>
 * </fo-auth-shell>
 */
@Component({
    selector: 'fo-auth-shell',
    standalone: true,
    imports: [IconComponent],
    template: `
        <div class="min-h-dvh bg-app lg:grid lg:grid-cols-[minmax(0,24rem)_minmax(0,1fr)] xl:grid-cols-[minmax(0,30rem)_minmax(0,1fr)]">
            <!--
                Panel de marca. En escritorio ocupa la columna izquierda a sangre; por debajo de
                lg se convierte en una cabecera de una sola línea para no comerse la pantalla del
                teléfono, donde lo que importa es ver el formulario sin desplazar. No va fija a
                propósito: en los formularios largos (registro, completar ficha) se va al
                desplazar y deja la altura entera del teléfono para los campos.
            -->
            <aside
                class="overflow-hidden text-white"
                style="
                    background:
                        radial-gradient(120% 90% at 15% 0%, var(--fo-accent-700) 0%, transparent 55%),
                        radial-gradient(90% 80% at 100% 100%, var(--fo-accent-600) 0%, transparent 50%),
                        linear-gradient(155deg, var(--fo-accent-900) 0%, var(--fo-accent-950) 100%);
                    padding-top: var(--fo-safe-top);
                    padding-left: max(0px, var(--fo-safe-left));
                "
            >
                <!-- Cabecera compacta: solo móvil y tableta. -->
                <div class="flex items-center gap-2.5 px-5 py-3.5 lg:hidden">
                    @if (logo()) {
                        <img [src]="logo()" alt="" class="h-8 w-8 shrink-0 rounded-token-sm bg-white/10 object-contain" />
                    } @else {
                        <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-token-sm bg-white/15 text-sm font-bold" aria-hidden="true">{{ inicial() }}</span>
                    }
                    <span class="truncate text-sm font-semibold tracking-tight">{{ marca() }}</span>
                </div>

                <!-- Panel completo: solo escritorio. -->
                <div class="hidden h-full flex-col justify-between p-10 lg:flex xl:p-12">
                    <div class="flex items-center gap-3">
                        @if (logo()) {
                            <img [src]="logo()" alt="" class="h-11 w-11 shrink-0 rounded-token bg-white/10 object-contain" />
                        } @else {
                            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-token bg-white/15 text-lg font-bold" aria-hidden="true">{{ inicial() }}</span>
                        }
                        <span class="truncate text-lg font-semibold tracking-tight">{{ marca() }}</span>
                    </div>

                    <div>
                        <h2 class="text-3xl font-semibold leading-tight text-white xl:text-4xl">{{ titular() }}</h2>
                        <p class="mt-3 max-w-sm text-sm leading-relaxed text-white/70">Socios, cuotas y eventos en un solo sitio, sin hojas de cálculo que nadie sabe quién tocó.</p>

                        <ul class="mt-8 space-y-3.5">
                            @for (feature of features; track feature.text) {
                                <li class="flex items-center gap-3 text-sm text-white/85">
                                    <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-token-sm bg-white/10">
                                        <fo-icon [name]="feature.icon" [size]="16" />
                                    </span>
                                    {{ feature.text }}
                                </li>
                            }
                        </ul>
                    </div>

                    <p class="text-xs text-white/40">FanOps</p>
                </div>
            </aside>

            <!-- Columna del formulario. -->
            <main
                class="flex items-start justify-center px-5 py-8 sm:px-8 sm:py-12 lg:items-center lg:px-12"
                style="
                    padding-bottom: max(2rem, var(--fo-safe-bottom));
                    padding-right: max(1.25rem, var(--fo-safe-right));
                "
            >
                <div class="w-full" [class]="widthClass()">
                    <header class="mb-7">
                        @if (icon()) {
                            <span class="mb-4 flex h-11 w-11 items-center justify-center rounded-token bg-accent-soft text-accent-soft-fg">
                                <fo-icon [name]="icon()!" [size]="22" />
                            </span>
                        }
                        <h1 class="text-2xl font-semibold tracking-tight sm:text-3xl">{{ title() }}</h1>
                        @if (subtitle()) {
                            <p class="mt-2 text-sm leading-relaxed text-ink-muted">{{ subtitle() }}</p>
                        }
                    </header>

                    <ng-content />

                    <div class="mt-7 border-t border-line pt-5 empty:mt-0 empty:border-0 empty:pt-0">
                        <ng-content select="[authFooter]" />
                    </div>
                </div>
            </main>
        </div>
    `
})
export class AuthShellComponent {
    private readonly penaPublica = inject(PenaPublicaService);

    readonly title = input.required<string>();
    readonly subtitle = input<string | null>(null);

    /** Icono opcional sobre el título, para pantallas de una sola tarea (recuperar contraseña...). */
    readonly icon = input<FoIconName | null>(null);

    readonly width = input<AuthShellWidth>('sm');

    protected readonly features: ReadonlyArray<{ icon: FoIconName; text: string }> = [
        { icon: 'socios', text: 'Altas de socios y cuotas al día' },
        { icon: 'calendario', text: 'Eventos con inscripciones y aforo' },
        { icon: 'entrada', text: 'Carnet digital en el móvil' }
    ];

    /** Nombre a mostrar: el de la peña del dominio, o el de la aplicación si no hay dominio. */
    protected readonly marca = computed(() => this.penaPublica.pena()?.nombre ?? 'FanOps');

    protected readonly logo = computed(() => this.penaPublica.pena()?.logo || null);

    /** Inicial para el hueco del logo cuando la peña no tiene ninguno. */
    protected readonly inicial = computed(() => this.marca().trim().charAt(0).toUpperCase() || 'F');

    /**
     * Titular del panel. Con dominio se usa el lema de la peña si lo tiene, que es lo que hace
     * que la pantalla se sienta suya y no de la aplicación.
     */
    protected readonly titular = computed(() => this.penaPublica.pena()?.lema?.trim() || 'Tu peña, al día.');

    protected widthClass(): string {
        return WIDTHS[this.width()];
    }
}

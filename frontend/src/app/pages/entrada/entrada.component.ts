import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { filter, of, take, timeout } from 'rxjs';
import { AuthService } from '@/pages/auth/auth.service';
import { IconComponent } from '@/ui/icon/icon.component';
import { PenaContextService } from '@/services/pena-context.service';
import { PenaService } from '@/services/pena.service';
import { ROLE_ADMIN } from '@/core/auth/roles';

/**
 * Cuánto se espera a conocer la peña del usuario antes de rendirse y mandar al login.
 * Solo se agota si la petición para recuperarla falla; en el caso normal llega al instante.
 */
const ESPERA_MAXIMA_MS = 8000;

/**
 * Tiempo mínimo que la pantalla de carga permanece visible.
 *
 * Sin él, cuando la peña ya viene en la sesión la redirección ocurre en el mismo instante y lo
 * único que se ve es un parpadeo: la pantalla aparece y desaparece antes de poder leerla, lo que
 * se percibe como un salto brusco. Con esta espera la entrada es siempre igual, tarde lo que
 * tarde el backend en contestar.
 */
const MINIMO_VISIBLE_MS = 2000;

/**
 * Mensajes que se van sucediendo mientras se espera.
 *
 * El segundo y el tercero solo llegan a verse si la espera se alarga, que es lo que pasa cuando
 * el backend está dormido (en Render se suspende por inactividad y el primer arranque cuesta
 * bastantes segundos). Contar lo que está pasando evita que parezca que se ha quedado colgado.
 */
const MENSAJES = ['Estamos preparando tu peña…', 'El servidor estaba en reposo, lo estamos despertando…', 'Ya queda menos, gracias por la paciencia.'];

/** Cada cuánto se pasa al siguiente mensaje. */
const CADENCIA_MENSAJES_MS = 4000;

/**
 * Punto de entrada de la raíz de la aplicación.
 *
 * Existe porque todas las pantallas viven ahora bajo el dominio de la peña
 * (`/mi-pena/socios`), así que `/` no es una ruta válida: hay que averiguar de qué peña se trata
 * y redirigir. Y eso no se puede resolver con un `redirectTo`, porque para el superadmin la peña
 * no sale de su cuenta (no pertenece a ninguna) sino de la lista de peñas, que llega por HTTP.
 *
 * Resuelve la peña por su cuenta en lugar de apoyarse en ActivePenaService: a ese lo arranca el
 * shell, y llamar a su `init()` aquí lo dejaría inicializado dos veces.
 */
@Component({
    selector: 'app-entrada',
    standalone: true,
    imports: [IconComponent],
    template: `
        <div class="fo-entrada">
            <div class="fo-entrada__contenido" role="status" [attr.aria-busy]="cargando" aria-live="polite">
                @if (cargando) {
                    <!-- Marca animada. Es decorativa: lo que anuncia el lector de pantalla es el texto. -->
                    <div class="fo-entrada__marca" aria-hidden="true">
                        <span class="fo-entrada__halo"></span>
                        <span class="fo-entrada__anillo"></span>
                        <span class="fo-entrada__anillo fo-entrada__anillo--interior"></span>
                        <span class="fo-entrada__nucleo"><fo-icon name="socios" [size]="26" /></span>
                    </div>
                } @else {
                    <div class="fo-entrada__marca fo-entrada__marca--aviso" aria-hidden="true">
                        <span class="fo-entrada__nucleo"><fo-icon name="aviso" [size]="26" /></span>
                    </div>
                }

                <div>
                    <h1 class="text-lg font-semibold tracking-tight">{{ titulo }}</h1>
                    <!--
                        El @for sobre un único elemento no es un listado: al ir trazado por el propio
                        texto, cambiar de mensaje recrea el párrafo y su animación de entrada se
                        vuelve a reproducir. Con una interpolación normal el texto cambiaría de
                        golpe, sin transición.
                    -->
                    @for (linea of [mensaje]; track linea) {
                        <p class="fo-entrada__mensaje">{{ linea }}</p>
                    }
                </div>

                @if (cargando) {
                    <span class="fo-entrada__barra" aria-hidden="true"></span>
                }
            </div>
        </div>
    `,
    styles: [
        `
            .fo-entrada {
                display: flex;
                align-items: center;
                justify-content: center;
                min-height: 100dvh;
                padding: 2rem 1.5rem;
                /* El resplandor superior tiñe la pantalla con el color de la peña sin competir con
                   el contenido: el acento solo aparece diluido sobre el fondo. */
                background: radial-gradient(70% 50% at 50% 0%, var(--fo-accent-50) 0%, transparent 70%), var(--fo-bg);
            }

            .fo-entrada__contenido {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 1.75rem;
                text-align: center;
                animation: fo-entrada-aparece 0.4s ease-out both;
            }

            .fo-entrada__marca {
                position: relative;
                display: grid;
                place-items: center;
                width: 6.5rem;
                height: 6.5rem;
            }

            .fo-entrada__halo {
                position: absolute;
                inset: 0;
                border-radius: var(--fo-radius-full);
                background: var(--fo-accent);
                opacity: 0.12;
                animation: fo-entrada-latido 2.4s ease-in-out infinite;
            }

            .fo-entrada__anillo {
                position: absolute;
                inset: 0;
                border: 3px solid var(--fo-accent-100);
                border-top-color: var(--fo-accent);
                border-radius: var(--fo-radius-full);
                /* fo-spin es el giro global que ya usa el indicador de los botones (base.scss). */
                animation: fo-spin 1s linear infinite;
            }

            /* Un segundo arco, más fino y girando al revés, para que la espera no sea un simple
               círculo dando vueltas. */
            .fo-entrada__anillo--interior {
                inset: 0.6rem;
                border-width: 2px;
                border-color: transparent;
                border-bottom-color: var(--fo-accent-300);
                animation-duration: 1.7s;
                animation-direction: reverse;
            }

            .fo-entrada__nucleo {
                position: relative;
                display: grid;
                place-items: center;
                width: 3.75rem;
                height: 3.75rem;
                border-radius: var(--fo-radius-full);
                background: var(--fo-surface);
                box-shadow: var(--fo-shadow-sm);
                color: var(--fo-accent);
            }

            .fo-entrada__marca--aviso .fo-entrada__nucleo {
                color: var(--fo-warn);
            }

            .fo-entrada__mensaje {
                max-width: 22rem;
                margin-top: 0.375rem;
                font-size: 0.875rem;
                color: var(--fo-text-muted);
                animation: fo-entrada-aparece 0.35s ease-out both;
            }

            /* Barra indeterminada: no hay progreso real que medir, solo movimiento que confirma que
               la aplicación sigue viva. */
            .fo-entrada__barra {
                position: relative;
                width: 11rem;
                height: 4px;
                overflow: hidden;
                border-radius: var(--fo-radius-full);
                background: var(--fo-accent-100);
            }

            .fo-entrada__barra::after {
                content: '';
                position: absolute;
                top: 0;
                bottom: 0;
                width: 40%;
                border-radius: inherit;
                background: var(--fo-accent);
                animation: fo-entrada-barra 1.4s ease-in-out infinite;
            }

            @keyframes fo-entrada-aparece {
                from {
                    opacity: 0;
                    transform: translateY(6px);
                }
                to {
                    opacity: 1;
                    transform: none;
                }
            }

            @keyframes fo-entrada-latido {
                0%,
                100% {
                    transform: scale(0.9);
                    opacity: 0.1;
                }
                50% {
                    transform: scale(1.05);
                    opacity: 0.2;
                }
            }

            @keyframes fo-entrada-barra {
                from {
                    left: -40%;
                }
                to {
                    left: 100%;
                }
            }

            /* Respeta a quien pide menos movimiento: la espera se sigue viendo, pero sin nada
               girando ni latiendo. */
            @media (prefers-reduced-motion: reduce) {
                .fo-entrada__contenido,
                .fo-entrada__mensaje,
                .fo-entrada__halo,
                .fo-entrada__anillo,
                .fo-entrada__barra::after {
                    animation: none;
                }

                .fo-entrada__anillo--interior {
                    display: none;
                }

                .fo-entrada__barra::after {
                    left: 0;
                    width: 100%;
                    opacity: 0.5;
                }
            }
        `
    ]
})
export class EntradaComponent implements OnInit, OnDestroy {
    private readonly auth = inject(AuthService);
    private readonly penaService = inject(PenaService);
    private readonly penaContext = inject(PenaContextService);
    private readonly router = inject(Router);

    protected cargando = true;
    protected titulo = 'Un momento…';
    protected mensaje = MENSAJES[0];

    /** Momento en que empezó la espera, para que la pantalla dure al menos MINIMO_VISIBLE_MS. */
    private readonly inicio = Date.now();
    private rotacion?: ReturnType<typeof setInterval>;
    private salida?: ReturnType<typeof setTimeout>;

    ngOnInit(): void {
        this.rotarMensajes();

        if (!this.auth.isLoggedIn()) {
            this.finalizar(() => this.router.navigate(['/auth/login'], { replaceUrl: true }));
            return;
        }

        if (this.auth.isSuperAdmin()) {
            this.entrarComoSuperAdmin();
            return;
        }

        /*
         * Se espera a una peña que traiga slug, no simplemente a la primera que llegue.
         *
         * Una sesión guardada antes de que existieran los dominios no lo tiene, y AuthService la
         * vuelve a pedir en cuanto arranca: con `take(1)` se cogería el valor viejo, sin slug, y
         * se mandaría al login a alguien que tiene la sesión perfectamente abierta.
         */
        this.auth.currentPena
            .pipe(
                filter((pena) => !!pena?.slug),
                take(1),
                timeout({ first: ESPERA_MAXIMA_MS, with: () => of(null) })
            )
            .subscribe((pena) => {
                if (pena?.slug) {
                    this.finalizar(() => this.router.navigate(['/', pena.slug, this.pantallaInicial()], { replaceUrl: true }));
                    return;
                }

                // Sin peña no hay dominio al que ir. Se vuelve al login, que es donde se recupera.
                this.finalizar(() => this.router.navigate(['/auth/login'], { replaceUrl: true }));
            });
    }

    ngOnDestroy(): void {
        this.pararTemporizadores();
    }

    /**
     * El superadmin entra en la peña que tuviera seleccionada, o en la primera si no había
     * ninguna. Hay que esperar a que llegue la lista: sin ella no se conoce ningún slug.
     */
    private entrarComoSuperAdmin(): void {
        this.penaService.listAll().subscribe({
            next: (respuesta) => {
                const penas = respuesta.data ?? [];
                const seleccionada = this.penaContext.getSelectedPenaId();
                const pena = penas.find((candidata) => candidata.id === seleccionada) ?? penas[0];

                if (pena?.slug) {
                    this.penaContext.setSelectedPenaId(pena.id);
                    this.finalizar(() => this.router.navigate(['/', pena.slug, 'penas'], { replaceUrl: true }));
                    return;
                }

                this.avisar('Todavía no hay peñas', 'No hay ninguna peña dada de alta. Crea una para empezar.');
            },
            error: () => {
                this.avisar('No hemos podido entrar', 'No se han podido cargar las peñas. Vuelve a intentarlo en un momento.');
            }
        });
    }

    /** Primera pantalla según el rol: gestión para un admin, área personal para un socio. */
    private pantallaInicial(): string {
        return this.auth.hasAuthority(ROLE_ADMIN) ? 'socios' : 'carnet-socio';
    }

    /**
     * Ejecuta la redirección, pero nunca antes de que la pantalla se haya visto. Si la respuesta
     * llegó al instante todavía queda espera por consumir; si tardó, sale sin retraso alguno.
     */
    private finalizar(accion: () => void): void {
        this.salida = setTimeout(() => {
            this.pararTemporizadores();
            accion();
        }, this.esperaRestante());
    }

    /** Cambia la carga por un mensaje final, respetando también el tiempo mínimo en pantalla. */
    private avisar(titulo: string, mensaje: string): void {
        this.salida = setTimeout(() => {
            this.pararTemporizadores();
            this.cargando = false;
            this.titulo = titulo;
            this.mensaje = mensaje;
        }, this.esperaRestante());
    }

    private esperaRestante(): number {
        return Math.max(0, MINIMO_VISIBLE_MS - (Date.now() - this.inicio));
    }

    /** Va pasando de mensaje mientras la espera se alarga, y se detiene en el último. */
    private rotarMensajes(): void {
        let paso = 0;

        this.rotacion = setInterval(() => {
            paso += 1;
            this.mensaje = MENSAJES[paso];

            if (paso >= MENSAJES.length - 1) {
                clearInterval(this.rotacion);
                this.rotacion = undefined;
            }
        }, CADENCIA_MENSAJES_MS);
    }

    private pararTemporizadores(): void {
        clearInterval(this.rotacion);
        clearTimeout(this.salida);
        this.rotacion = undefined;
        this.salida = undefined;
    }
}

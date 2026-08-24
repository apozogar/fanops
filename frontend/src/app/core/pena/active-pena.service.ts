import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@/pages/auth/auth.service';
import { PenaContextService } from '@/services/pena-context.service';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { PenaService } from '@/services/pena.service';
import { ThemeService } from '@/core/theme/theme.service';
import { PageReloader } from '@/core/platform/page-reloader.service';
import { Pena } from '@/interfaces/socio.interface';

/** Primer segmento de la ruta: el dominio de la peña. */
const PRIMER_SEGMENTO = /^\/[^/?#]+/;

/**
 * Peña "activa": aquella cuyos datos se están viendo.
 *
 * Para un usuario normal es su propia peña, que llega en el login. Para el superadmin, que no
 * pertenece a ninguna, es la que haya elegido en el selector de la cabecera (y que viaja al
 * backend como cabecera X-Pena-Id). Unifica ambos casos para que el shell no tenga que
 * distinguirlos, y mantiene sincronizado el color de acento del tema.
 */
@Injectable({ providedIn: 'root' })
export class ActivePenaService {
    private readonly auth = inject(AuthService);
    private readonly penaService = inject(PenaService);
    private readonly penaContext = inject(PenaContextService);
    private readonly penaPublica = inject(PenaPublicaService);
    private readonly theme = inject(ThemeService);
    private readonly reloader = inject(PageReloader);
    private readonly router = inject(Router);

    private readonly _pena = signal<Pena | null>(null);
    private readonly _options = signal<Pena[]>([]);
    private readonly _loading = signal<boolean>(false);

    /** Peña activa, o null si aún no se conoce (o el superadmin no ha elegido ninguna). */
    readonly pena = this._pena.asReadonly();

    /** Peñas entre las que se puede cambiar. Solo se puebla para el superadmin. */
    readonly options = this._options.asReadonly();

    readonly loading = this._loading.asReadonly();

    readonly isSuperAdmin = computed(() => this.auth.isSuperAdmin());

    /** true cuando el superadmin todavía no ha seleccionado ninguna peña. */
    readonly needsSelection = computed(() => this.isSuperAdmin() && this._pena() === null);

    /**
     * Arranca el seguimiento de la peña activa. Lo llama el shell una sola vez, ya con el
     * usuario autenticado disponible.
     */
    init(): void {
        this.olvidarAlCerrarSesion();

        if (this.auth.isSuperAdmin()) {
            this.loadOptionsForSuperAdmin();
            return;
        }

        this.auth.currentPena.subscribe((pena) => this.apply(pena));
    }

    /**
     * Descarta la peña activa cuando se cierra la sesión.
     *
     * Se escucha el usuario y no la peña porque para un superadmin la peña del usuario es siempre
     * nula (no pertenece a ninguna): la suya sale del selector, así que mirar `currentPena` no
     * distinguiría "sin sesión" de "superadmin". Sin esto, la peña seleccionada sobrevivía al
     * logout y quedaba a la vista en el color de la interfaz y en el título de la pestaña.
     */
    private olvidarAlCerrarSesion(): void {
        this.auth.currentUser.subscribe((usuario) => {
            if (usuario === null) {
                this._options.set([]);
                this.apply(null);
            }
        });
    }

    /**
     * Cambia la peña de trabajo del superadmin.
     *
     * Cambia también el dominio de la URL, porque el dominio ES el contexto de peña: dejarlo
     * apuntando a la anterior haría que un recargado o un enlace copiado volvieran a la peña de
     * antes. Se persiste la elección y se navega al dominio nuevo, sustituyendo solo el primer
     * segmento para quedarse en la misma pantalla.
     *
     * Si el id no corresponde a ninguna peña conocida no se hace nada, para no dejar el contexto
     * apuntando a algo inexistente.
     */
    select(penaId: number | null): void {
        const pena = penaId === null ? null : (this._options().find((candidate) => candidate.id === penaId) ?? null);

        if (penaId !== null && pena === null) {
            return;
        }

        if (penaId === this.penaContext.getSelectedPenaId()) {
            return; // Ya es la peña activa: no hay nada que cambiar ni que recargar.
        }

        this.penaContext.setSelectedPenaId(penaId);
        this.apply(pena);

        // Las páginas cargan sus datos en ngOnInit, así que un cambio de peña necesita que
        // vuelvan a pedirlos. Se navega al dominio nuevo y se recarga: tosco, pero garantiza que
        // no quede ningún dato de la peña anterior en pantalla. Es una acción poco frecuente y
        // solo del superadmin.
        if (pena?.slug) {
            this.reloader.reemplazarPrimerSegmento(pena.slug);
            return;
        }

        this.reloader.reload();
    }

    /**
     * Refresca los datos de una peña ya cargada (p. ej. tras editar su logo o su color desde la
     * pantalla de gestión) para que la cabecera y el tema reflejen el cambio sin recargar.
     */
    actualizada(pena: Pena | null | undefined): void {
        if (!pena) {
            return;
        }

        this._options.update((opciones) => opciones.map((candidate) => (candidate.id === pena.id ? pena : candidate)));

        if (this._pena()?.id === pena.id) {
            this.apply(pena);
        }
    }

    private loadOptionsForSuperAdmin(): void {
        this._loading.set(true);

        this.penaService.listAll().subscribe({
            next: (response) => {
                const penas = response.data ?? [];
                this._options.set(penas);

                // El dominio de la URL manda: es lo que hace que /otra-pena/socios muestre
                // realmente los socios de esa peña, y que un enlace compartido entre
                // superadmins abra la peña que dice y no la que cada uno tuviera guardada.
                const slugEnLaUrl = this.penaPublica.slug();
                let pena = slugEnLaUrl ? (penas.find((candidate) => candidate.slug?.toLowerCase() === slugEnLaUrl.toLowerCase()) ?? null) : null;

                // Sin dominio en la URL se cae en la que estuviera seleccionada.
                if (!pena) {
                    const selectedId = this.penaContext.getSelectedPenaId();
                    pena = penas.find((candidate) => candidate.id === selectedId) ?? null;
                }

                // Y si tampoco había, la primera, para que las pantallas de gestión funcionen
                // desde el primer momento en vez de fallar con "selecciona una peña". Se
                // persiste la elección pero NO se recarga: estamos en el arranque, y llamar a
                // select() aquí provocaría un bucle de recargas.
                if (!pena && penas.length > 0) {
                    pena = penas[0];
                }

                if (pena) {
                    this.penaContext.setSelectedPenaId(pena.id);
                }

                this.apply(pena);
                this.corregirDominioEnLaUrl(pena);
                this._loading.set(false);
            },
            error: () => {
                this._options.set([]);
                this._loading.set(false);
            }
        });
    }

    /**
     * Ajusta el dominio de la URL a la peña que se está mostrando de verdad.
     *
     * Solo hace algo cuando no coinciden, que pasa si el superadmin escribe un dominio que no
     * existe: sin esto, la URL decía una peña y la pantalla mostraba otra. Se reemplaza en el
     * historial y sin recargar, porque en este punto las pantallas aún no han pedido sus datos.
     */
    private corregirDominioEnLaUrl(pena: Pena | null): void {
        const slugCorrecto = pena?.slug;
        if (!slugCorrecto) {
            return;
        }

        const slugEnLaUrl = this.penaPublica.slug();
        if (slugEnLaUrl?.toLowerCase() === slugCorrecto.toLowerCase()) {
            return;
        }

        const urlCorregida = this.router.url.replace(PRIMER_SEGMENTO, `/${slugCorrecto}`);
        this.router.navigateByUrl(urlCorregida, { replaceUrl: true });
    }

    private apply(pena: Pena | null): void {
        this._pena.set(pena);
        this.theme.setAccent(pena?.color ?? null);
    }
}

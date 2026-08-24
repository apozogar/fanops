import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { filter, of, take, timeout } from 'rxjs';
import { AuthService } from '@/pages/auth/auth.service';
import { PenaContextService } from '@/services/pena-context.service';
import { PenaService } from '@/services/pena.service';
import { ROLE_ADMIN } from '@/core/auth/roles';

/**
 * Cuánto se espera a conocer la peña del usuario antes de rendirse y mandar al login.
 * Solo se agota si la petición para recuperarla falla; en el caso normal llega al instante.
 */
const ESPERA_MAXIMA_MS = 8000;

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
    template: `
        <div class="flex min-h-dvh items-center justify-center px-6 text-center text-sm text-ink-muted">
            {{ mensaje }}
        </div>
    `
})
export class EntradaComponent implements OnInit {
    private readonly auth = inject(AuthService);
    private readonly penaService = inject(PenaService);
    private readonly penaContext = inject(PenaContextService);
    private readonly router = inject(Router);

    protected mensaje = 'Un momento…';

    ngOnInit(): void {
        if (!this.auth.isLoggedIn()) {
            this.router.navigate(['/auth/login'], { replaceUrl: true });
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
                    this.router.navigate(['/', pena.slug, this.pantallaInicial()], { replaceUrl: true });
                    return;
                }

                // Sin peña no hay dominio al que ir. Se vuelve al login, que es donde se recupera.
                this.router.navigate(['/auth/login'], { replaceUrl: true });
            });
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
                    this.router.navigate(['/', pena.slug, 'penas'], { replaceUrl: true });
                    return;
                }

                this.mensaje = 'No hay ninguna peña dada de alta todavía. Crea una para empezar.';
            },
            error: () => {
                this.mensaje = 'No se han podido cargar las peñas. Vuelve a intentarlo en un momento.';
            }
        });
    }

    /** Primera pantalla según el rol: gestión para un admin, área personal para un socio. */
    private pantallaInicial(): string {
        return this.auth.hasAuthority(ROLE_ADMIN) ? 'socios' : 'carnet-socio';
    }
}

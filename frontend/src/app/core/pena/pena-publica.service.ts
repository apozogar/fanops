import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '@/interfaces/api-response.interface';
import { ThemeService } from '@/core/theme/theme.service';

/** Identidad de una peña, tal como la devuelve el endpoint público. */
export interface PenaPublica {
    nombre: string;
    slug: string;
    logo?: string | null;
    lema?: string | null;
    color?: string | null;
}

/**
 * Peña por cuyo dominio se ha entrado, antes de que exista sesión.
 *
 * Resuelve el primer segmento de la URL (`/mi-pena/auth/login`) contra el endpoint público de
 * peñas y guarda el resultado. Es lo que permite dos cosas que sin dominio eran imposibles:
 *
 *  - que el login y el registro muestren la peña a la que se está accediendo, con su logo y su
 *    color, en lugar de la marca genérica de FanOps;
 *  - que el registro sepa a qué peña asociar al socio nuevo. Antes no había forma de saberlo y
 *    el backend caía siempre en la peña por defecto.
 *
 * Entrar por la raíz sigue siendo válido: no hay peña, se usa la marca genérica y el registro
 * cae en la peña por defecto, igual que antes.
 */
@Injectable({ providedIn: 'root' })
export class PenaPublicaService {
    private readonly http = inject(HttpClient);
    private readonly theme = inject(ThemeService);

    private readonly apiUrl = `${environment.apiUrl}/api/pena/publica`;

    private readonly _pena = signal<PenaPublica | null>(null);

    /** Peña del dominio actual, o null si se ha entrado por la raíz. */
    readonly pena = this._pena.asReadonly();

    /** Dominio actual, o null. Lo usan las pantallas para construir sus enlaces. */
    readonly slug = computed(() => this._pena()?.slug ?? null);

    /**
     * Construye una ruta conservando el dominio actual, para no perderlo al navegar entre
     * pantallas de autenticación. Con dominio devuelve `['/', 'mi-pena', 'auth', 'register']` y
     * sin él `['/', 'auth', 'register']`, así que las plantillas no tienen que distinguir los
     * dos casos: `[routerLink]="penaPublica.ruta('auth', 'register')"`.
     */
    ruta(...segmentos: string[]): string[] {
        const slug = this.slug();
        return slug ? ['/', slug, ...segmentos] : ['/', ...segmentos];
    }

    /**
     * Carga la peña de un dominio. Cachea por slug: al navegar entre login, registro y
     * recuperación de contraseña no se vuelve a pedir.
     *
     * Un dominio inexistente devuelve `null` en lugar de propagar el error: quien llama decide
     * si eso es un 404 (el resolver de la ruta) o simplemente ausencia de marca.
     */
    cargar(slug: string | null | undefined): Observable<PenaPublica | null> {
        if (!slug) {
            this.aplicar(null);
            return of(null);
        }

        const actual = this._pena();
        if (actual && actual.slug.toLowerCase() === slug.toLowerCase()) {
            // Ya cargada: se reaplica el acento porque otra pantalla pudo haberlo cambiado.
            this.aplicar(actual);
            return of(actual);
        }

        return this.http.get<ApiResponse<PenaPublica>>(`${this.apiUrl}/${encodeURIComponent(slug)}`).pipe(
            map((response) => response.data ?? null),
            tap((pena) => this.aplicar(pena)),
            catchError(() => {
                this.aplicar(null);
                return of(null);
            })
        );
    }

    /** Olvida la peña actual y vuelve a la marca genérica. */
    limpiar(): void {
        this.aplicar(null);
    }

    private aplicar(pena: PenaPublica | null): void {
        this._pena.set(pena);
        // Sin peña se pasa null y el tema cae a su acento por defecto.
        this.theme.setAccent(pena?.color ?? null);
    }
}

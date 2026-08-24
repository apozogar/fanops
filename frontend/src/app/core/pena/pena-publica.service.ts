import { Injectable, inject, signal } from '@angular/core';
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
 * Peña del primer segmento de la URL, que es el dominio de la peña: `/mi-pena/socios`.
 *
 * Guarda dos cosas distintas, y a propósito por separado:
 *
 *  - **el slug**, que sale de la URL y está disponible siempre, sin pedir nada al backend. Es la
 *    fuente de verdad del contexto de peña, y de él se construyen todos los enlaces de la
 *    aplicación con {@link ruta}, de modo que ninguna navegación pierda el dominio.
 *  - **la identidad de la peña** (nombre, logo, color), que solo hace falta antes de tener
 *    sesión: es lo que permite que el login y el registro muestren la peña a la que se está
 *    accediendo en lugar de la marca genérica de FanOps, y que el registro sepa a qué peña
 *    asociar al socio nuevo. Con sesión iniciada esos datos los da ActivePenaService, ya
 *    autenticados, y aquí basta con el slug.
 *
 * Entrar por la raíz sigue siendo válido para las pantallas de autenticación: no hay peña, se usa
 * la marca genérica y el registro cae en la peña por defecto.
 */
@Injectable({ providedIn: 'root' })
export class PenaPublicaService {
    private readonly http = inject(HttpClient);
    private readonly theme = inject(ThemeService);

    private readonly apiUrl = `${environment.apiUrl}/api/pena/publica`;

    private readonly _slug = signal<string | null>(null);
    private readonly _pena = signal<PenaPublica | null>(null);

    /**
     * Dominio de la peña en la URL, o null si se ha entrado por la raíz.
     *
     * Es un signal propio y no algo derivado de la peña cargada: las rutas de la aplicación lo
     * fijan sin llamar al endpoint público, porque ahí la identidad de la peña ya viene con la
     * sesión y lo único que hace falta es el slug para construir los enlaces.
     */
    readonly slug = this._slug.asReadonly();

    /** Identidad de la peña, solo cuando se ha cargado (pantallas de autenticación). */
    readonly pena = this._pena.asReadonly();

    /**
     * Construye una ruta absoluta conservando el dominio actual. Con dominio devuelve
     * `['/', 'mi-pena', 'socios']` y sin él `['/', 'socios']`, así que quien la usa no tiene que
     * distinguir los dos casos: `[routerLink]="penaPublica.ruta('socios')"`.
     */
    ruta(...segmentos: string[]): string[] {
        const slug = this._slug();
        return slug ? ['/', slug, ...segmentos] : ['/', ...segmentos];
    }

    /**
     * Fija el dominio actual sin cargar nada. Lo usan las rutas de la aplicación, donde la
     * identidad de la peña llega con la sesión y solo se necesita el slug para los enlaces.
     */
    fijarSlug(slug: string | null | undefined): void {
        this._slug.set(slug || null);
    }

    /**
     * Fija el dominio y carga la identidad de la peña. Cachea por slug: al navegar entre login,
     * registro y recuperación de contraseña no se vuelve a pedir.
     *
     * Un dominio inexistente devuelve `null` en lugar de propagar el error: quien llama decide
     * si eso es un 404 (el resolver de la ruta) o simplemente ausencia de marca.
     */
    cargar(slug: string | null | undefined): Observable<PenaPublica | null> {
        if (!slug) {
            this.aplicar(null, null);
            return of(null);
        }

        const actual = this._pena();
        if (actual && actual.slug.toLowerCase() === slug.toLowerCase()) {
            // Ya cargada: se reaplica el acento porque otra pantalla pudo haberlo cambiado.
            this.aplicar(slug, actual);
            return of(actual);
        }

        return this.http.get<ApiResponse<PenaPublica>>(`${this.apiUrl}/${encodeURIComponent(slug)}`).pipe(
            map((response) => response.data ?? null),
            tap((pena) => this.aplicar(pena ? pena.slug : slug, pena)),
            catchError(() => {
                this.aplicar(slug, null);
                return of(null);
            })
        );
    }

    /** Olvida el dominio y la peña, y vuelve a la marca genérica. */
    limpiar(): void {
        this.aplicar(null, null);
    }

    private aplicar(slug: string | null, pena: PenaPublica | null): void {
        this._slug.set(slug);
        this._pena.set(pena);

        // Solo se toca el acento cuando se conoce la identidad de la peña. Con sesión iniciada el
        // acento lo pone ActivePenaService a partir de la peña autenticada, y sobreescribirlo aquí
        // con null haría parpadear la interfaz al verde por defecto en cada navegación.
        if (pena) {
            this.theme.setAccent(pena.color ?? null);
        } else if (slug === null) {
            this.theme.setAccent(null);
        }
    }
}

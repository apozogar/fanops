import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { PenaPublica, PenaPublicaService } from './pena-publica.service';

/**
 * Resuelve el dominio de peña del primer segmento de la URL antes de pintar la pantalla.
 *
 * Se resuelve antes y no dentro del componente para que el login no aparezca un instante con la
 * marca genérica de FanOps y salte después a la de la peña.
 *
 * Un dominio que no existe manda a /notfound en lugar de mostrar el login sin marca: la URL es
 * la única pista que tiene la persona de en qué peña está entrando, y si está mal hay que
 * decírselo, no dejar que se registre en la peña equivocada.
 */
export const penaPublicaResolver: ResolveFn<PenaPublica | null> = (route: ActivatedRouteSnapshot) => {
    const penaPublica = inject(PenaPublicaService);
    const router = inject(Router);

    const slug = route.paramMap.get('penaSlug');

    return penaPublica.cargar(slug).pipe(
        map((pena) => {
            if (slug && !pena) {
                router.navigate(['/notfound']);
                return null;
            }
            return pena;
        })
    );
};

/**
 * Variante para las rutas sin dominio (`/auth/login` a secas). Limpia cualquier peña que hubiera
 * quedado cargada de una visita anterior, de modo que la pantalla no muestre la marca de una peña
 * que no corresponde a la URL actual.
 */
export const sinPenaPublicaResolver: ResolveFn<null> = () => {
    inject(PenaPublicaService).limpiar();
    return null;
};

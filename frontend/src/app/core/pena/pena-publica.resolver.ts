import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { PenaPublica, PenaPublicaService } from './pena-publica.service';

/**
 * Resuelve el dominio de peña de la URL y carga su identidad, para las pantallas de
 * autenticación.
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

    const slug = slugDeLaRuta(route);

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
 * Fija el dominio de peña de la URL, sin cargar nada, para las rutas de la aplicación.
 *
 * Ahí la identidad de la peña ya llega con la sesión (ver ActivePenaService) y lo único que hace
 * falta es el slug, del que se construyen todos los enlaces para que ninguna navegación pierda el
 * dominio. Evita así una llamada al endpoint público en cada navegación.
 */
export const penaSlugResolver: ResolveFn<string | null> = (route: ActivatedRouteSnapshot) => {
    const penaPublica = inject(PenaPublicaService);
    const slug = slugDeLaRuta(route);
    penaPublica.fijarSlug(slug);
    return slug;
};

/**
 * Variante para las rutas sin dominio (`/auth/login` a secas). Limpia el dominio que hubiera
 * quedado de una visita anterior, de modo que la pantalla no muestre la marca de una peña que no
 * está en la URL ni construya enlaces hacia ella.
 */
export const sinPenaPublicaResolver: ResolveFn<null> = () => {
    inject(PenaPublicaService).limpiar();
    return null;
};

/**
 * El parámetro puede estar en la propia ruta o en un ancestro, según de qué nivel del árbol se
 * trate: las pantallas de autenticación cuelgan de `:penaSlug/auth`, y las de la aplicación de
 * `:penaSlug` directamente.
 */
function slugDeLaRuta(route: ActivatedRouteSnapshot): string | null {
    for (let actual: ActivatedRouteSnapshot | null = route; actual; actual = actual.parent) {
        const slug = actual.paramMap.get('penaSlug');
        if (slug) {
            return slug;
        }
    }
    return null;
}

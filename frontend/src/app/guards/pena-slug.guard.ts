import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs';
import { AuthService } from '@/pages/auth/auth.service';

/**
 * Mantiene honesto el dominio de la URL: impide ver los datos de tu peña bajo el nombre de otra.
 *
 * Ahora que el primer segmento de la URL es la peña, alguien puede escribir a mano el dominio de
 * una peña ajena. Los datos siguen a salvo, porque el backend filtra siempre por la peña del
 * usuario autenticado y no por lo que diga la URL, pero la pantalla mostraría los socios de tu
 * peña con la dirección de otra. Aquí se corrige sustituyendo el dominio por el correcto y
 * conservando el resto de la ruta.
 *
 * El superadmin queda fuera a propósito: no pertenece a ninguna peña, así que para él el dominio
 * de la URL no es algo que validar sino la peña que elige mirar. De eso se encarga
 * ActivePenaService cuando le llega la lista de peñas.
 */
export const penaSlugGuard: CanActivateFn = (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (auth.isSuperAdmin()) {
        return true;
    }

    return auth.currentPena.pipe(
        take(1),
        map((pena) => {
            const miSlug = pena?.slug;

            // Todavía no se conoce la peña del usuario (sesión recién restaurada, o una guardada
            // antes de que existieran los dominios): no se bloquea la navegación.
            if (!miSlug) {
                return true;
            }

            const slugEnLaUrl = route.paramMap.get('penaSlug');
            if (slugEnLaUrl?.toLowerCase() === miSlug.toLowerCase()) {
                return true;
            }

            // Se sustituye solo el primer segmento, así que se conservan la ruta, los parámetros
            // de consulta y el fragmento. replaceUrl evita dejar la URL incorrecta en el historial,
            // donde el botón de atrás volvería a caer en ella.
            router.navigateByUrl(state.url.replace(/^\/[^/?#]+/, `/${miSlug}`), { replaceUrl: true });
            return false;
        })
    );
};

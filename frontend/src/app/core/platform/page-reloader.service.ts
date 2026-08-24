import { Injectable } from '@angular/core';

/**
 * Recarga completa de la página.
 *
 * Existe como servicio, y no como una llamada directa a `window.location.reload()`, para que
 * el código que la usa se pueda probar: en un test se sustituye por un doble y se comprueba
 * que la recarga se pide en el momento adecuado, sin recargar el propio ejecutor de tests.
 */
@Injectable({ providedIn: 'root' })
export class PageReloader {
    reload(): void {
        window.location.reload();
    }

    /**
     * Sustituye el primer segmento de la ruta y recarga. Es el cambio de peña del superadmin: el
     * primer segmento de la URL es el dominio de la peña, así que cambiarlo equivale a moverse a
     * la misma pantalla de otra peña.
     *
     * Se usa una asignación de `location` y no el router a propósito: hace falta la recarga
     * completa para que las pantallas vuelvan a pedir sus datos, y navegando con el router el
     * componente se reutilizaría y se quedarían los de la peña anterior.
     */
    reemplazarPrimerSegmento(segmento: string): void {
        const { pathname, search, hash } = window.location;
        // Conserva el resto de la ruta, los parámetros de consulta y el fragmento.
        const rutaNueva = pathname.replace(/^\/[^/]*/, `/${segmento}`);
        window.location.assign(`${rutaNueva}${search}${hash}`);
    }
}

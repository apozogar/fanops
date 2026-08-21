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
}

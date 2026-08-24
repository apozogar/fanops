import { Injectable, effect, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivePenaService } from '@/core/pena/active-pena.service';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';

/** Nombre de la aplicación. Es el título cuando no se está en ninguna peña concreta. */
const BASE = 'FanOps';

/**
 * Mantiene el título de la pestaña al día: `FanOps - Nombre de la peña` cuando se está en una
 * peña concreta, y solo `FanOps` cuando no.
 *
 * Sirve para distinguir pestañas: con varias abiertas de peñas distintas (el caso del
 * superadmin, o de quien recibe el enlace de otra peña) todas se llamaban igual.
 *
 * La peña se toma de dos sitios porque se conoce en dos momentos distintos: con sesión iniciada
 * la da {@link ActivePenaService}, y antes de tener sesión la da {@link PenaPublicaService} a
 * partir del dominio de la URL. La primera tiene prioridad: si hay sesión, la peña en la que
 * realmente se está trabajando es esa, aunque se hubiera entrado por el dominio de otra.
 */
@Injectable({ providedIn: 'root' })
export class TituloPaginaService {
    private readonly title = inject(Title);
    private readonly activePena = inject(ActivePenaService);
    private readonly penaPublica = inject(PenaPublicaService);

    constructor() {
        // Un effect y no una suscripción manual: ambas fuentes son signals, así que el título se
        // recalcula solo cuando cambia cualquiera de las dos (login, logout, cambio de peña del
        // superadmin, o navegación a otro dominio) sin nada que dar de baja.
        effect(() => {
            const nombre = this.activePena.pena()?.nombre ?? this.penaPublica.pena()?.nombre ?? null;
            this.title.setTitle(nombre ? `${BASE} - ${nombre}` : BASE);
        });
    }
}

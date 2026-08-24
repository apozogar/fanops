import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {map} from 'rxjs/operators';
import {MessageService} from 'primeng/api';
import {PenaPublicaService} from '@/core/pena/pena-publica.service';
import {AuthService} from "@/pages/auth/auth.service";
import {ROLE_ADMIN, ROLE_SUPERADMIN} from "@/core/auth/roles";

/**
 * Permite el paso a quien puede gestionar la peña activa: administradores de la peña y también
 * el superadmin, que gestiona la que tenga seleccionada en la cabecera. Antes solo aceptaba
 * ROLE_ADMIN, así que rebotaba al superadmin fuera de la gestión de socios.
 */
export const adminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const messageService = inject(MessageService);
    const penaPublica = inject(PenaPublicaService);

    return authService.currentUser.pipe(
        map(user => {
            const authorities = user?.authorities?.map(auth => auth.authority) ?? [];
            const puedeGestionar = authorities.includes(ROLE_ADMIN) || authorities.includes(ROLE_SUPERADMIN);

            if (puedeGestionar) {
                return true;
            }

            messageService.add({
                severity: 'warn',
                summary: 'Acceso Denegado',
                detail: 'No tienes permisos para acceder a esta sección.'
            });

            // Un socio sin permisos de gestión va a su propio carnet.
            // Dentro de la peña actual: rebotar a una ruta sin dominio dejaría la URL sin peña.
            router.navigate(penaPublica.ruta('carnet-socio'));
            return false;
        })
    );
};

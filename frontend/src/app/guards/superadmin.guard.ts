import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {map} from 'rxjs/operators';
import {MessageService} from 'primeng/api';
import {PenaPublicaService} from '@/core/pena/pena-publica.service';
import {AuthService} from "@/pages/auth/auth.service";
import {ROLE_SUPERADMIN} from "@/core/auth/roles";

export const superAdminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const messageService = inject(MessageService);
    const penaPublica = inject(PenaPublicaService);

    return authService.currentUser.pipe(
        map(user => {
            const isSuperAdmin = user?.authorities?.some(auth => auth.authority === ROLE_SUPERADMIN);

            if (isSuperAdmin) {
                return true;
            }

            messageService.add({
                severity: 'warn',
                summary: 'Acceso Denegado',
                detail: 'Esta sección es solo para superadministradores.'
            });

            // Dentro de la peña actual: rebotar a una ruta sin dominio dejaría la URL sin peña.
            router.navigate(penaPublica.ruta('carnet-socio'));
            return false;
        })
    );
};

import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {map} from 'rxjs/operators';
import {MessageService} from 'primeng/api';
import {AuthService} from "@/pages/auth/auth.service";
import {ROLE_SUPERADMIN} from "@/core/auth/roles";

export const superAdminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const messageService = inject(MessageService);

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

            router.navigate(['/carnet-socio']);
            return false;
        })
    );
};

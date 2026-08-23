import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { ShellComponent } from '@/shell/shell.component';
import { Notfound } from '@/pages/notfound/notfound';
import { CarnetSocioComponent } from '@/pages/area-personal/carnetSocio/CarnetSocioComponent';
import { CuotasSocioComponent } from '@/pages/area-personal/cuotasSocio/CuotasSocioComponent';
import { InscripcionPublicaComponent } from '@/pages/publico/inscripcion-publica/inscripcion-publica.component';
import { InscripcionEventosComponent } from '@/pages/area-personal/inscripcion-eventos/inscripcion-eventos.component';
import { adminGuard } from '@/guards/admin.guard';
import { authGuard } from '@/guards/auth.guard';
import { superAdminGuard } from '@/guards/superadmin.guard';
import { AuthService } from '@/pages/auth/auth.service';

/**
 * Destino inicial según el rol. Antes se redirigía siempre a /socios, que el adminGuard
 * rebotaba para quien no fuera admin (y mandaba al superadmin a una pantalla que no le
 * corresponde).
 */
function homeForCurrentUser(): string {
    const auth = inject(AuthService);

    if (auth.isSuperAdmin()) {
        return '/penas';
    }

    return auth.hasAuthority('ROLE_ADMIN') ? '/socios' : '/carnet-socio';
}

export const appRoutes: Routes = [
    {
        path: '',
        component: ShellComponent,
        canActivate: [authGuard],
        children: [
            { path: '', pathMatch: 'full', redirectTo: homeForCurrentUser },
            {
                path: 'socios',
                loadComponent: () => import('@/pages/gestion/socios/SociosComponent').then((m) => m.SociosComponent),
                canActivate: [adminGuard]
            },
            {
                path: 'eventos',
                loadComponent: () => import('@/pages/gestion/eventos/EventosComponent').then((m) => m.EventosComponent)
            },
            {
                path: 'penas',
                loadComponent: () => import('@/pages/gestion/penas/PenasComponent').then((m) => m.PenasComponent),
                canActivate: [superAdminGuard]
            },
            {
                path: 'carnet-socio',
                component: CarnetSocioComponent
            },
            {
                path: 'cuotas-socio',
                component: CuotasSocioComponent
            },
            {
                path: 'inscripciones',
                component: InscripcionEventosComponent
            }
        ]
    },
    { path: 'notfound', component: Notfound },
    { path: 'auth', loadChildren: () => import('@/pages/auth/auth.routes') },
    { path: 'inscripcion/:id', component: InscripcionPublicaComponent },
    { path: '**', redirectTo: '/notfound' }
];

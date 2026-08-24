import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { ShellComponent } from '@/shell/shell.component';
import { Notfound } from '@/pages/notfound/notfound';
import { CarnetSocioComponent } from '@/pages/area-personal/carnetSocio/CarnetSocioComponent';
import { CuotasSocioComponent } from '@/pages/area-personal/cuotasSocio/CuotasSocioComponent';
import { InscripcionPublicaComponent } from '@/pages/publico/inscripcion-publica/inscripcion-publica.component';
import { InscripcionEventosComponent } from '@/pages/area-personal/inscripcion-eventos/inscripcion-eventos.component';
import { penaPublicaResolver, sinPenaPublicaResolver } from '@/core/pena/pena-publica.resolver';
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

    /*
     * Autenticación sin dominio de peña, entrando por la raíz de la aplicación. Sigue siendo
     * válido: se muestra la marca genérica de FanOps y quien se registre cae en la peña por
     * defecto, que es como funcionaba antes de existir los dominios por peña.
     *
     * El resolver descarta la peña que hubiera quedado cargada de una visita anterior, para que
     * la pantalla no muestre la marca de una peña que no está en la URL.
     */
    {
        path: 'auth',
        resolve: { penaPublica: sinPenaPublicaResolver },
        loadChildren: () => import('@/pages/auth/auth.routes')
    },
    { path: 'inscripcion/:id', component: InscripcionPublicaComponent },

    /*
     * Dominio de peña: /mi-pena/auth/login.
     *
     * Va al final a propósito, justo antes del comodín: ':penaSlug' encaja con cualquier primer
     * segmento, así que tiene que probarse después de todas las rutas concretas para que /socios
     * siga siendo la pantalla de socios y no la peña llamada "socios". El backend refuerza lo
     * mismo por el otro lado, reservando esos nombres al validar el dominio de una peña
     * (PenaService.SLUGS_RESERVADOS).
     */
    {
        path: ':penaSlug',
        resolve: { penaPublica: penaPublicaResolver },
        children: [
            { path: '', pathMatch: 'full', redirectTo: 'auth/login' },
            { path: 'auth', loadChildren: () => import('@/pages/auth/auth.routes') }
        ]
    },

    { path: '**', redirectTo: '/notfound' }
];

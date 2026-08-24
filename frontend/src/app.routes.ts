import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { ShellComponent } from '@/shell/shell.component';
import { Notfound } from '@/pages/notfound/notfound';
import { EntradaComponent } from '@/pages/entrada/entrada.component';
import { CarnetSocioComponent } from '@/pages/area-personal/carnetSocio/CarnetSocioComponent';
import { CuotasSocioComponent } from '@/pages/area-personal/cuotasSocio/CuotasSocioComponent';
import { InscripcionPublicaComponent } from '@/pages/publico/inscripcion-publica/inscripcion-publica.component';
import { InscripcionEventosComponent } from '@/pages/area-personal/inscripcion-eventos/inscripcion-eventos.component';
import { penaPublicaResolver, penaSlugResolver, sinPenaPublicaResolver } from '@/core/pena/pena-publica.resolver';
import { adminGuard } from '@/guards/admin.guard';
import { authGuard } from '@/guards/auth.guard';
import { penaSlugGuard } from '@/guards/pena-slug.guard';
import { superAdminGuard } from '@/guards/superadmin.guard';
import { AuthService } from '@/pages/auth/auth.service';

/**
 * Primera pantalla dentro de una peña, según el rol.
 *
 * El destino es relativo (sin barra inicial) a propósito: la ruta cuelga de `:penaSlug`, así que
 * un destino absoluto perdería el dominio de la peña.
 */
function pantallaInicial(): string {
    const auth = inject(AuthService);

    if (auth.isSuperAdmin()) {
        return 'penas';
    }

    return auth.hasAuthority('ROLE_ADMIN') ? 'socios' : 'carnet-socio';
}

export const appRoutes: Routes = [
    /*
     * Raíz. No hay ninguna pantalla aquí: todas viven bajo el dominio de la peña, así que lo
     * único que se hace es averiguar cuál es y redirigir (ver EntradaComponent).
     */
    { path: '', pathMatch: 'full', component: EntradaComponent },

    { path: 'notfound', component: Notfound },

    /*
     * Autenticación sin dominio de peña. Es la que reciben los enlaces de correo (restablecer
     * contraseña, vincular ficha), que se emiten sin saber la peña: se muestra la marca genérica
     * de FanOps y quien se registre cae en la peña por defecto.
     *
     * El resolver descarta el dominio que hubiera quedado de una visita anterior, para que la
     * pantalla no muestre la marca de una peña que no está en la URL ni enlace hacia ella.
     */
    {
        path: 'auth',
        resolve: { penaPublica: sinPenaPublicaResolver },
        loadChildren: () => import('@/pages/auth/auth.routes')
    },

    { path: 'inscripcion/:id', component: InscripcionPublicaComponent },

    /*
     * Autenticación dentro de una peña: /mi-pena/auth/login.
     *
     * Va antes que la ruta de la aplicación porque comparten el mismo `:penaSlug` y el router
     * prueba en orden. Aquí sí se carga la identidad de la peña (nombre, logo, color): es lo que
     * hace que el login se vea como el de esa peña y no como el de FanOps.
     */
    {
        path: ':penaSlug/auth',
        resolve: { penaPublica: penaPublicaResolver },
        loadChildren: () => import('@/pages/auth/auth.routes')
    },

    /*
     * La aplicación, dentro de una peña: /mi-pena/socios.
     *
     * Va al final, justo antes del comodín: ':penaSlug' encaja con cualquier primer segmento, así
     * que tiene que probarse después de todas las rutas concretas. El backend refuerza lo mismo
     * por el otro lado, reservando esos nombres al validar el dominio de una peña
     * (PenaService.SLUGS_RESERVADOS).
     *
     * El resolver solo fija el slug, sin pedir nada: aquí la identidad de la peña ya llega con la
     * sesión. El guard evita que la URL diga una peña y la pantalla muestre otra.
     */
    {
        path: ':penaSlug',
        component: ShellComponent,
        canActivate: [authGuard, penaSlugGuard],
        resolve: { penaSlug: penaSlugResolver },
        children: [
            { path: '', pathMatch: 'full', redirectTo: pantallaInicial },
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

    { path: '**', redirectTo: '/notfound' }
];

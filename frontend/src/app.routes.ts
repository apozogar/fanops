import {Routes} from '@angular/router';
import {AppLayout} from '@//layout/component/app.layout';
import {Notfound} from '@//pages/notfound/notfound';
import {CarnetSocioComponent} from "@/pages/area-personal/carnetSocio/CarnetSocioComponent";
import {InscripcionPublicaComponent} from "@/pages/publico/inscripcion-publica/inscripcion-publica.component";
import {adminGuard} from '@/guards/admin.guard';
import {authGuard} from '@/guards/auth.guard';
import {superAdminGuard} from '@/guards/superadmin.guard';
import {
    InscripcionEventosComponent
} from "@/pages/area-personal/inscripcion-eventos/inscripcion-eventos.component";

export const appRoutes: Routes = [
    {
        path: '',
        component: AppLayout,
        canActivate: [authGuard],
        children: [
            {path: '', redirectTo: 'socios', pathMatch: 'full'},
            {
                path: 'socios',
                loadComponent: () =>
                    import('@/pages/gestion/socios/SociosComponent').then(m => m.SociosComponent),
                canActivate: [adminGuard]
            },
            {
                path: 'eventos',
                loadComponent: () =>
                    import('@/pages/gestion/eventos/EventosComponent').then(m => m.EventosComponent)
            },
            {
                path: 'penas',
                loadComponent: () =>
                    import('@/pages/gestion/penas/PenasComponent').then(m => m.PenasComponent),
                canActivate: [superAdminGuard]
            },
            // {
            //     path: 'cuotas',
            //     loadComponent: () =>
            //         import('@/pages/cuotas/CuotasComponet').then(m => m.CuotasComponet)
            // },
            // {
            //     path: 'informes',
            //     loadComponent: () =>
            //         import('@/pages/informes/InformesComponent').then(m => m.InformesComponent)
            // },
            {
                path: 'carnet-socio',
                component: CarnetSocioComponent
            },
            {
                path: 'inscripciones',
                component: InscripcionEventosComponent
            },
        ]
    },
    {
        path: 'notfound', component:
        Notfound
    }
    ,
    {
        path: 'auth', loadChildren:
            () => import('@/pages/auth/auth.routes')
    }
    ,
    {
        path: 'inscripcion/:id',
        component: InscripcionPublicaComponent
    },
    {
        path: '**', redirectTo:
            '/notfound'
    }
];

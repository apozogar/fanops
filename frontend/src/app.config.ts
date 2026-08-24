import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withEnabledBlockingInitialNavigation, withInMemoryScrolling } from '@angular/router';
import Aura from '@primeuix/themes/aura';
import { providePrimeNG } from 'primeng/config';
import { appRoutes } from './app.routes';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AuthInterceptor, PenaContextInterceptor } from '@/config/HttpInterceptors';

export const appConfig: ApplicationConfig = {
    providers: [
        // Rutas con URL limpia (sin hash). El backend reenvía a index.html cualquier ruta que no
        // sea de la API ni un fichero estático (ver SpaForwardingController), y es lo que permite
        // que el dominio de cada peña sea un segmento de la ruta: /mi-pena/auth/login.
        provideRouter(
            appRoutes,
            withInMemoryScrolling({
                anchorScrolling: 'enabled',
                scrollPositionRestoration: 'enabled'
            }),
            withEnabledBlockingInitialNavigation()
        ),
        //provideHttpClient(withFetch()),
        provideAnimationsAsync(),
        /*
         * darkModeSelector: 'none' desactiva por completo el esquema oscuro de PrimeNG.
         *
         * Es imprescindible y no una redundancia: su valor por defecto es 'system', que resuelve a
         * @media (prefers-color-scheme: dark), así que al quitar el tema oscuro de la aplicación
         * sus componentes (tabla, diálogo, datepicker, toast) habrían seguido pintándose oscuros a
         * cualquiera que tenga el sistema en modo oscuro, dentro de una interfaz clara.
         */
        providePrimeNG({ theme: { preset: Aura, options: { darkModeSelector: 'none' } } }),
        MessageService,
        ConfirmationService,
        provideHttpClient(withInterceptorsFromDi()), // Habilita la inyección de dependencias para interceptors
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: PenaContextInterceptor,
            multi: true
        }
    ]
};

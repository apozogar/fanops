import {
    HTTP_INTERCEPTORS,
    provideHttpClient,
    withInterceptorsFromDi
} from '@angular/common/http';
import {ApplicationConfig} from '@angular/core';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {
    provideRouter,
    withEnabledBlockingInitialNavigation,
    withInMemoryScrolling,
    withHashLocation
} from '@angular/router';
import Aura from '@primeuix/themes/aura';
import {providePrimeNG} from 'primeng/config';
import {appRoutes} from './app.routes';
import {ConfirmationService, MessageService} from "primeng/api";
import {AuthInterceptor, PenaContextInterceptor} from "@/config/HttpInterceptors";

export const appConfig: ApplicationConfig = {
    providers: [
        provideRouter(appRoutes, withHashLocation(), withInMemoryScrolling({
            anchorScrolling: 'enabled',
            scrollPositionRestoration: 'enabled'
        }), withEnabledBlockingInitialNavigation()),
        //provideHttpClient(withFetch()),
        provideAnimationsAsync(),
        providePrimeNG({theme: {preset: Aura, options: {darkModeSelector: '.app-dark'}}}),
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

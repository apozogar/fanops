import {Injectable, Injector, inject} from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpErrorResponse
} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthService} from "@/pages/auth/auth.service";
import {PenaContextService} from "@/services/pena-context.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    /*
     * AuthService se resuelve al vuelo y no por constructor a propósito. Inyectarlo aquí crea un
     * ciclo de dependencias (NG0200): para construir HttpClient hay que construir sus
     * HTTP_INTERCEPTORS, y AuthService necesita a su vez HttpClient. Pidiéndolo dentro de
     * intercept() la resolución ocurre en la primera petición, cuando HttpClient ya existe.
     */
    private readonly injector = inject(Injector);

    private get authService(): AuthService {
        return this.injector.get(AuthService);
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const authService = this.authService;
        const token = authService.getToken();
        let requestToHandle = request;

        // 1. Clonar la solicitud y agregar el token si existe
        if (token) {
            requestToHandle = request.clone({
                headers: request.headers.set('Authorization', `Bearer ${token}`)
            });
        }

        // 2. Manejar la solicitud y usar catchError en el stream de respuesta
        return next.handle(requestToHandle).pipe(
            catchError((error: HttpErrorResponse) => {
                // Solo un 401 cierra la sesión: significa que el token falta, ha caducado o no
                // es válido. Un 403 es "estás identificado pero esta acción no te corresponde",
                // que debe mostrarse como error de la operación sin echar al usuario. Antes
                // ambos cerraban sesión, así que cualquier pantalla que tocara un endpoint sin
                // permiso expulsaba al login.
                if (error.status === 401) {
                    console.error('Sesión no válida o caducada. Cerrando sesión...');
                    authService.logout();
                }

                // Re-lanza el error para que sea manejado por el componente o servicio que hizo la llamada
                return throwError(() => error);
            })
        );
    }
}

/**
 * Añade la cabecera X-Pena-Id con la peña que el superadmin ha elegido en el selector de la
 * cabecera. Para un usuario normal no hay nada seleccionado (su peña sale del backend a partir
 * de su propio usuario) y la cabecera simplemente no se envía.
 */
@Injectable()
export class PenaContextInterceptor implements HttpInterceptor {

    constructor(private penaContextService: PenaContextService) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const penaId = this.penaContextService.getSelectedPenaId();

        if (penaId !== null) {
            request = request.clone({
                headers: request.headers.set('X-Pena-Id', String(penaId))
            });
        }

        return next.handle(request);
    }
}

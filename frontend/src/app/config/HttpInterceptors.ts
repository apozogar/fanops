import {Injectable} from '@angular/core';
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

    constructor(private authService: AuthService) {
    }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const token = this.authService.getToken();
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
                    this.authService.logout();
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

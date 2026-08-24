import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, map, of, switchMap, tap} from 'rxjs';
import {RegisterRequest} from '@/models/register-request.model';
import {ApiResponse} from '@/interfaces/api-response.interface';
import {RegisterResponse, VinculacionInfo} from '@/interfaces/vinculacion.interface';
import {Router} from '@angular/router';
import {environment} from "../../../environments/environment";
import {jwtDecode} from "jwt-decode";
import {User} from "@/interfaces/user";
import {PenaService} from "@/services/pena.service";
import {PenaContextService} from "@/services/pena-context.service";
import {PenaPublicaService} from "@/core/pena/pena-publica.service";
import {Pena} from "@/interfaces/socio.interface";
import {ROLE_ADMIN, ROLE_SUPERADMIN} from "@/core/auth/roles";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private http = inject(HttpClient);
    private router = inject(Router);
    private penaService = inject(PenaService);
    private penaContextService = inject(PenaContextService);
    private penaPublica = inject(PenaPublicaService);

    private baseUrl = environment.apiUrl + '/api/auth';

    private currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser = this.currentUserSubject.asObservable();
    private currentPenaSubject = new BehaviorSubject<Pena | null>(null);
    public currentPena = this.currentPenaSubject.asObservable();

    constructor() {
        const token = this.getToken();
        if (token) {
            this.decodeToken(token);
            this.loadPenaFromStorage();
        }
    }

    login(credentials: { email: string, password: string }): Observable<any> {
        return this.http.post<{ token: string }>(`${this.baseUrl}/login`, credentials).pipe(
            switchMap(response => this.iniciarSesion(response))
        );
    }

    register(registerData: RegisterRequest): Observable<ApiResponse<RegisterResponse>> {
        return this.http.post<ApiResponse<RegisterResponse>>(`${this.baseUrl}/register`, registerData);
    }

    /**
     * Registra y, si procede, deja la sesión iniciada. Cuando el email ya figuraba en el listado de
     * socios de la peña el backend no crea nada: envía un correo con un enlace para confirmar la
     * vinculación con la ficha existente, así que no hay sesión que iniciar todavía y se devuelve
     * `requiereVerificacion` para que la pantalla lo indique.
     */
    loginAfterRegister(registerData: RegisterRequest): Observable<{ requiereVerificacion: boolean }> {
        return this.register(registerData).pipe(
            switchMap(response => {
                if (response?.data?.requiereVerificacion) {
                    return of({requiereVerificacion: true});
                }
                return this.login({email: registerData.email, password: registerData.password}).pipe(
                    map(() => ({requiereVerificacion: false}))
                );
            })
        );
    }

    /** Datos de la ficha de socio a la que corresponde el token del enlace recibido por correo. */
    getVinculacion(token: string): Observable<ApiResponse<VinculacionInfo>> {
        return this.http.get<ApiResponse<VinculacionInfo>>(`${this.baseUrl}/vinculacion`, {params: {token}});
    }

    /**
     * Confirma la vinculación: el backend crea la cuenta, le asocia las fichas de socio que ya
     * existían y devuelve el JWT, con lo que la sesión queda iniciada como en un login normal.
     */
    confirmarVinculacion(token: string, password?: string): Observable<any> {
        return this.http.post<{ token: string }>(`${this.baseUrl}/vinculacion/confirmar`, {token, password}).pipe(
            switchMap(response => this.iniciarSesion(response))
        );
    }

    /**
     * Guarda el JWT recibido, decodifica el usuario y carga su peña. Lo comparten el login y la
     * confirmación de vinculación, que acaban igual: con un token recién emitido por el backend.
     */
    private iniciarSesion(response: { token: string }): Observable<any> {
        if (!response.token) {
            return of(response); // Continuar el flujo si no hay token
        }

        localStorage.setItem('token', response.token);
        this.decodeToken(response.token);

        const user = this.currentUserSubject.getValue();
        const clubId = (user as any)?.clubId;

        if (!clubId) {
            return of(response); // No hay clubId, continuar
        }

        return this.penaService.get(clubId.toString()).pipe(
            tap(penaResponse => {
                const pena = penaResponse.data;
                this.currentPenaSubject.next(pena);
                // Solo los campos que usa el frontend: la respuesta del backend arrastra la
                // colección de socios y, con el logo en base64, no cabría en localStorage.
                localStorage.setItem('currentPena', JSON.stringify({
                    id: pena.id,
                    nombre: pena.nombre,
                    // El slug se guarda porque de él sale el primer segmento de todas las URLs
                    // de la aplicación: sin él, tras recargar no se podría reconstruir la ruta.
                    slug: pena.slug,
                    logo: pena.logo,
                    lema: pena.lema,
                    color: pena.color
                } as Pena));
            }),
            switchMap(() => of(response)) // Devolver la respuesta original del login
        );
    }

    forgotPassword(email
                   :
                   string
    ):
        Observable<any> {
        return this.http.post(`${this.baseUrl}/forgot-password`, {email});
    }

    resetPassword(token: string, password: string
    ):
        Observable<any> {
        return this.http.post(`${this.baseUrl}/reset-password`, {token, password});
    }

    isLoggedIn()
        :
        boolean {
        // Aquí también podrías añadir una comprobación de la expiración del token
        return !!localStorage.getItem('token');
    }

    logout()
        :
        void {
        // El destino se calcula ANTES de limpiar: después ya no se sabría en qué peña se estaba.
        const destino = this.penaPublica.ruta('auth', 'login');

        localStorage.removeItem('token');
        localStorage.removeItem('currentPena');
        this.currentUserSubject.next(null);
        this.currentPenaSubject.next(null);
        this.penaContextService.clear();
        // Se vuelve al login DE LA PEÑA, no al genérico: quien cierra sesión en su peña espera
        // volver a entrar en la misma, con su marca. El slug se lee antes de limpiar nada.
        this.router.navigate(destino);
    }

    /** true si el usuario autenticado es superadmin (gestiona todas las peñas, no una fija). */
    isSuperAdmin(): boolean {
        return this.hasAuthority(ROLE_SUPERADMIN);
    }

    /**
     * true si puede gestionar la peña activa. Incluye al superadmin, en coherencia con la
     * jerarquía de roles del backend (SUPERADMIN implies ADMIN): gestiona la peña que tenga
     * seleccionada en el selector de la cabecera.
     */
    isAdmin(): boolean {
        return this.hasAuthority(ROLE_ADMIN) || this.isSuperAdmin();
    }

    hasAuthority(authority: string): boolean {
        return !!this.getCurrentUser()?.authorities?.some(a => a.authority === authority);
    }

    /** Usuario autenticado actual (payload del JWT), o null si no hay sesión. */
    getCurrentUser(): User | null {
        return this.currentUserSubject.getValue();
    }

    getToken()
        :
        string | null {
        return localStorage.getItem('token');
    }

    decodeToken(token
                :
                string
    ):
        void {
        try {
            const decodedToken
                :
                User = jwtDecode(token);
            this.currentUserSubject.next(decodedToken);
        } catch
            (error) {
            this.currentUserSubject.next(null);
        }
    }

    private loadPenaFromStorage(): void {
        const penaData = localStorage.getItem('currentPena');
        if (!penaData) {
            return;
        }

        try {
            const pena: Pena = JSON.parse(penaData);
            this.currentPenaSubject.next(pena);

            // Sesiones abiertas antes de que existieran los dominios por peña no tienen el slug
            // guardado, y de él sale el primer segmento de todas las URLs. Se recarga la peña en
            // lugar de obligar a volver a entrar.
            if (!pena.slug && pena.id) {
                this.refrescarPenaGuardada(pena.id);
            }
        } catch (error) {
            console.error('Error al parsear la peña desde localStorage:', error);
        }
    }

    /** Vuelve a pedir la peña y la reescribe en localStorage, ya con el slug. */
    private refrescarPenaGuardada(id: number): void {
        this.penaService.get(id.toString()).subscribe({
            next: (respuesta) => {
                const pena = respuesta.data;
                if (!pena) {
                    return;
                }
                this.currentPenaSubject.next(pena);
                localStorage.setItem('currentPena', JSON.stringify({
                    id: pena.id,
                    nombre: pena.nombre,
                    slug: pena.slug,
                    logo: pena.logo,
                    lema: pena.lema,
                    color: pena.color
                } as Pena));
            },
            // Sin conexión o con el token caducado se sigue con lo que había guardado.
            error: () => undefined
        });
    }
}

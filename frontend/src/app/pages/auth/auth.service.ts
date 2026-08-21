import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, of, switchMap, tap} from 'rxjs';
import {RegisterRequest} from '@/models/register-request.model';
import {Router} from '@angular/router';
import {environment} from "../../../environments/environment";
import {jwtDecode} from "jwt-decode";
import {User} from "@/interfaces/user";
import {PenaService} from "@/services/pena.service";
import {PenaContextService} from "@/services/pena-context.service";
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

    private baseUrl = environment.apiUrl + '/auth';

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
            switchMap(response => {
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
                        localStorage.setItem('currentPena', JSON.stringify(pena));
                    }),
                    switchMap(() => of(response)) // Devolver la respuesta original del login
                );
            })
        );
    }

    register(registerData
             :
             RegisterRequest
    ):
        Observable<any> {
        return this.http.post(`${this.baseUrl}/register`, registerData);
    }

    loginAfterRegister(registerData
                       :
                       RegisterRequest
    ):
        Observable<any> {
        return this.register(registerData).pipe(
            switchMap(() => {
                return this.login({email: registerData.email, password: registerData.password});
            })
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
        localStorage.removeItem('token');
        localStorage.removeItem('currentPena');
        this.currentUserSubject.next(null);
        this.currentPenaSubject.next(null);
        this.penaContextService.clear();
        this.router.navigate(['/auth/login']);
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
        if (penaData) {
            try {
                const pena: Pena = JSON.parse(penaData);
                this.currentPenaSubject.next(pena);
            } catch (error) {
                console.error('Error al parsear la peña desde localStorage:', error);
            }
        }
    }
}

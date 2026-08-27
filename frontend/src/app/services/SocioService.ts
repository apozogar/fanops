import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ApiResponse} from '@/interfaces/api-response.interface';
import {EstadisticasSocio, Socio} from '@/interfaces/socio.interface';
import {Role} from "@/interfaces/role.interface";
import {environment} from "../../environments/environment";


@Injectable({
    providedIn: 'root'
})
export class SocioService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {
    }

    getSocios(filtro?: string): Observable<ApiResponse<Socio[]>> {
        let params = new HttpParams();
        if (filtro) {
            params = params.append('filtro', filtro);
        }
        return this.http.get<ApiResponse<Socio[]>>(`${this.apiUrl}/api/socios`, {params});
    }

    getSociosActivos(): Observable<ApiResponse<Socio[]>> {
        return this.http.get<ApiResponse<Socio[]>>(`${this.apiUrl}/activos`);
    }

    getSocio(uid: string): Observable<ApiResponse<Socio>> {
        return this.http.get<ApiResponse<Socio>>(`${this.apiUrl}/api/socios/${uid}`);
    }

    crearSocio(socio: Socio): Observable<ApiResponse<Socio>> {
        return this.http.post<ApiResponse<Socio>>(`${this.apiUrl}/api/socios`, socio);
    }

    actualizarSocio(uid: string, socio: Socio): Observable<ApiResponse<Socio>> {
        return this.http.put<ApiResponse<Socio>>(`${this.apiUrl}/api/socios/${uid}`, socio);
    }

    eliminarSocio(uid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/api/socios/${uid}`);
    }

    /**
     * Crea la cuenta de acceso del socio con la contraseña indicada, o le cambia la contraseña si
     * ya la tenía. Es la vía manual para los socios que no se registran por su cuenta.
     */
    establecerCuenta(uid: string, password: string, admin: boolean): Observable<ApiResponse<Socio>> {
        return this.http.post<ApiResponse<Socio>>(`${this.apiUrl}/api/socios/${uid}/cuenta`, {password, admin});
    }

    getCuotasSocio(uid: string): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/api/socios/${uid}/cuotas`);
    }

    // Métodos adicionales para validación
    verificarDniExistente(dni: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/api/socios/verificar/dni/${dni}`);
    }

    verificarEmailExistente(email: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/api/socios/verificar/email/${email}`);
    }

    verificarNumeroSocioExistente(numeroSocio: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/verificar/numero/${numeroSocio}`);
    }

    obtenerEstadisticas(): Observable<ApiResponse<EstadisticasSocio>> {
        return this.http.get<ApiResponse<EstadisticasSocio>>(`${this.apiUrl}/api/socios/estadisticas`);
    }

    generarSepa(): Observable<Blob> { // Debe devolver un Observable<Blob>
        return this.http.get(`${this.apiUrl}/api/socios/generar-sepa`, {
            responseType: 'blob' // ¡Esta es la clave!
        });
    }

    importarSocios(file: File): Observable<ApiResponse<string>> {
        const formData = new FormData();
        formData.append('file', file, file.name);

        return this.http.post<ApiResponse<string>>(`${this.apiUrl}/api/socios/importar`, formData);
    }

    generarRemesaMensual(concepto: string, fechaCobro: string): Observable<any> {
        let params = new HttpParams().set('concepto', concepto).set('fechaCobro', fechaCobro);
        return this.http.get(`${environment.apiUrl}/api/cobros/generar-remesa`, {
            responseType: 'blob', params: params
        });
    }

    generarRemesaExcel(concepto: string, fechaCobro: string): Observable<any> {
        let params = new HttpParams().set('concepto', concepto).set('fechaCobro', fechaCobro);
        return this.http.get(`${environment.apiUrl}/api/cobros/generar-remesa-excel`, {
            responseType: 'blob', params: params
        });
    }

    getRoles(): Observable<ApiResponse<Role[]>> {
        return this.http.get<ApiResponse<Role[]>>(`${environment.apiUrl}/api/roles`);
    }

    procesarRetorno(file: File): Observable<ApiResponse<string>> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<ApiResponse<string>>(`${environment.apiUrl}/api/cobros/procesar-retorno`, formData);
    }

    confirmarPagos(): Observable<ApiResponse<string>> {
        return this.http.post<ApiResponse<string>>(`${environment.apiUrl}/api/cobros/confirmar-pagos`, {});
    }

}

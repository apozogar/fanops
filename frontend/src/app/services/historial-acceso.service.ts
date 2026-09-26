import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {ApiResponse} from '@/interfaces/api-response.interface';
import {HistorialAcceso} from '@/interfaces/historial-acceso.interface';

@Injectable({
    providedIn: 'root'
})
export class HistorialAccesoService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/api/historial-accesos`;

    /** Solo accesible para ROLE_SUPERADMIN. */
    listar(): Observable<ApiResponse<HistorialAcceso[]>> {
        return this.http.get<ApiResponse<HistorialAcceso[]>>(this.apiUrl);
    }
}

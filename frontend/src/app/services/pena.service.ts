import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {ApiResponse} from '@/interfaces/api-response.interface';
import {Pena, PenaRequest} from "@/interfaces/socio.interface";

@Injectable({
    providedIn: 'root'
})
export class PenaService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/api/pena`;

    get(id: string): Observable<ApiResponse<Pena>> {
        return this.http.get<ApiResponse<Pena>>(`${this.apiUrl}/${id}`);
    }

    /** Las operaciones siguientes solo son accesibles para ROLE_SUPERADMIN. */

    listAll(): Observable<ApiResponse<Pena[]>> {
        return this.http.get<ApiResponse<Pena[]>>(this.apiUrl);
    }

    crear(pena: PenaRequest): Observable<ApiResponse<Pena>> {
        return this.http.post<ApiResponse<Pena>>(this.apiUrl, pena);
    }

    actualizar(id: number, pena: PenaRequest): Observable<ApiResponse<Pena>> {
        return this.http.put<ApiResponse<Pena>>(`${this.apiUrl}/${id}`, pena);
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '@/interfaces/api-response.interface';
import { ValoresEvento } from '@/interfaces/valores-evento.dto';
import { environment } from '../../environments/environment';

/** Valores con los que se propone un evento nuevo en la peña de trabajo. */
@Injectable({ providedIn: 'root' })
export class ValoresEventoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/pena/valores-evento`;

  obtener(): Observable<ApiResponse<ValoresEvento>> {
    return this.http.get<ApiResponse<ValoresEvento>>(this.apiUrl);
  }

  guardar(valores: ValoresEvento): Observable<ApiResponse<ValoresEvento>> {
    return this.http.put<ApiResponse<ValoresEvento>>(this.apiUrl, valores);
  }
}

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '@/interfaces/api-response.interface';
import { SorteoCarnet } from '@/interfaces/sorteo-carnet.dto';
import { environment } from '../../environments/environment';

/** Sorteo de carnets de un evento. Todas las respuestas devuelven el sorteo ya actualizado. */
@Injectable({ providedIn: 'root' })
export class SorteoCarnetService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/eventos`;

  private base(eventoUid: string): string {
    return `${this.apiUrl}/${eventoUid}/sorteo-carnet`;
  }

  consultar(eventoUid: string): Observable<ApiResponse<SorteoCarnet>> {
    return this.http.get<ApiResponse<SorteoCarnet>>(this.base(eventoUid));
  }

  /**
   * Mete en el bombo las fichas indicadas de la cuenta, lo que las apunta también al evento.
   *
   * `soloSiEntranTodos` solo afecta a esa inscripción: si no caben todas, el grupo entero va a la
   * lista de espera en vez de partirse. En el bombo cada ficha entra por su cuenta.
   */
  apuntar(eventoUid: string, socioUids: string[], soloSiEntranTodos = false): Observable<ApiResponse<SorteoCarnet>> {
    return this.http.post<ApiResponse<SorteoCarnet>>(`${this.base(eventoUid)}/solicitar`, {
      socioUids,
      soloSiEntranTodos
    });
  }

  /** Saca una ficha del bombo, mientras el sorteo no se haya celebrado. */
  salir(eventoUid: string, socioUid: string): Observable<ApiResponse<SorteoCarnet>> {
    const params = new HttpParams().set('socioUid', socioUid);
    return this.http.delete<ApiResponse<SorteoCarnet>>(`${this.base(eventoUid)}/solicitar`, { params });
  }

  /** Devuelve un carnet que había tocado; pasa al primer suplente. */
  renunciar(eventoUid: string, socioUid: string): Observable<ApiResponse<SorteoCarnet>> {
    const params = new HttpParams().set('socioUid', socioUid);
    return this.http.post<ApiResponse<SorteoCarnet>>(`${this.base(eventoUid)}/renunciar`, null, { params });
  }

  /** Adelanta el sorteo (administración). */
  celebrar(eventoUid: string): Observable<ApiResponse<SorteoCarnet>> {
    return this.http.post<ApiResponse<SorteoCarnet>>(`${this.base(eventoUid)}/celebrar`, {});
  }
}

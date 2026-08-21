import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EventoInscripcionDTO, InscripcionAdmin, InscripcionPublicaRequest } from '@/interfaces/evento-inscripcion.dto';
import { Evento } from '@/interfaces/evento.interface';
import { environment } from '../../environments/environment';
import { ApiResponse } from '@/interfaces/api-response.interface';

@Injectable({
  providedIn: 'root'
})
export class EventoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/eventos`;

  /**
   * Obtiene la lista de eventos simplificada para la vista de inscripción de usuarios.
   */
  getEventosParaInscripcion(): Observable<ApiResponse<EventoInscripcionDTO[]>> {
    return this.http.get<ApiResponse<EventoInscripcionDTO[]>>(this.apiUrl);
  }

  /**
   * Obtiene la lista completa de eventos para el panel de administración.
   */
  getEventosParaGestion(): Observable<ApiResponse<Evento[]>> {
    return this.http.get<ApiResponse<Evento[]>>(`${this.apiUrl}/gestion`);
  }

  /**
   * Información pública de un evento (formulario de no socios, sin autenticación).
   */
  infoEventoPublico(uid: string): Observable<ApiResponse<EventoInscripcionDTO>> {
    return this.http.get<ApiResponse<EventoInscripcionDTO>>(`${this.apiUrl}/${uid}/info-publica`);
  }

  guardarEvento(evento: Partial<Evento>): Observable<ApiResponse<Evento>> {
    if (evento.uid) {
      return this.http.put<ApiResponse<Evento>>(`${this.apiUrl}/${evento.uid}`, evento);
    } else {
      return this.http.post<ApiResponse<Evento>>(this.apiUrl, evento);
    }
  }

  eliminarEvento(uid: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${uid}`);
  }

  inscribir(eventoId: string): Observable<ApiResponse<'CONFIRMADA' | 'EN_ESPERA'>> {
    return this.http.post<ApiResponse<'CONFIRMADA' | 'EN_ESPERA'>>(`${this.apiUrl}/${eventoId}/inscribir`, {});
  }

  anularInscripcion(eventoId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${eventoId}/anular`);
  }

  /**
   * Inscripción pública para no socios (enlace compartido, sin autenticación).
   */
  inscribirPublico(eventoId: string, data: InscripcionPublicaRequest): Observable<ApiResponse<'CONFIRMADA' | 'EN_ESPERA'>> {
    return this.http.post<ApiResponse<'CONFIRMADA' | 'EN_ESPERA'>>(`${this.apiUrl}/${eventoId}/inscripcion-publica`, data);
  }

  getInscripciones(eventoId: string): Observable<ApiResponse<InscripcionAdmin[]>> {
    return this.http.get<ApiResponse<InscripcionAdmin[]>>(`${this.apiUrl}/${eventoId}/inscripciones`);
  }

  asignarPlazas(eventoId: string): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/${eventoId}/asignar-plazas`, {});
  }
}
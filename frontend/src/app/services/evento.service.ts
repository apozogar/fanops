import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AsistenciaEvento, EventoInscripcionDTO, FaltaEvento, HistorialSocio, InscripcionAdmin, InscripcionPublicaRequest, InscripcionSocioRequest, SocioInscripcion } from '@/interfaces/evento-inscripcion.dto';
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

  /**
   * Inscribe una o varias fichas de socio de la cuenta. Devuelve el estado resultante de cada una.
   */
  inscribir(eventoId: string, data: InscripcionSocioRequest): Observable<ApiResponse<SocioInscripcion[]>> {
    return this.http.post<ApiResponse<SocioInscripcion[]>>(`${this.apiUrl}/${eventoId}/inscribir`, data);
  }

  /** Anula la inscripción de una ficha concreta de la cuenta. */
  anularInscripcion(eventoId: string, socioUid?: string): Observable<ApiResponse<void>> {
    const params = socioUid ? new HttpParams().set('socioUid', socioUid) : undefined;
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${eventoId}/anular`, { params });
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

  /**
   * Da de baja una inscripción desde gestión. Devuelve cuántas personas han pasado desde la
   * lista de espera al hueco liberado.
   */
  eliminarInscripcion(eventoId: string, inscripcionId: string): Observable<ApiResponse<number>> {
    return this.http.delete<ApiResponse<number>>(`${this.apiUrl}/${eventoId}/inscripciones/${inscripcionId}`);
  }

  /** Quienes han fallado en el evento: ausentes y cancelaciones fuera de plazo. */
  getFaltas(eventoId: string): Observable<ApiResponse<FaltaEvento[]>> {
    return this.http.get<ApiResponse<FaltaEvento[]>>(`${this.apiUrl}/${eventoId}/faltas`);
  }

  /** Marca o retira la falta de un inscrito con plaza. Devuelve sus faltas acumuladas. */
  marcarAsistencia(eventoId: string, inscripcionId: string, asistencia: AsistenciaEvento): Observable<ApiResponse<number>> {
    const params = new HttpParams().set('asistencia', asistencia);
    return this.http.put<ApiResponse<number>>(`${this.apiUrl}/${eventoId}/inscripciones/${inscripcionId}/asistencia`, null, { params });
  }

  /** Historial de eventos de un socio con sus faltas, para el modal del listado de socios. */
  getHistorialSocio(socioUid: string): Observable<ApiResponse<HistorialSocio>> {
    return this.http.get<ApiResponse<HistorialSocio>>(`${this.apiUrl}/socios/${socioUid}/historial`);
  }

  /** Retira una falta (justificada o marcada por error). */
  quitarFalta(faltaId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/faltas/${faltaId}`);
  }

  /** true si anular la plaza ahora le costaría una falta al socio. */
  avisoAnulacion(eventoId: string, socioUid?: string): Observable<ApiResponse<boolean>> {
    const params = socioUid ? new HttpParams().set('socioUid', socioUid) : undefined;
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/${eventoId}/anular/aviso`, { params });
  }

  asignarPlazas(eventoId: string): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/${eventoId}/asignar-plazas`, {});
  }
}
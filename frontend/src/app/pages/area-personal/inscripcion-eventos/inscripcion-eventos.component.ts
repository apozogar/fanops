import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {finalize, Subscription, timer} from 'rxjs';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {EventoInscripcionDTO, SocioInscripcion} from "@/interfaces/evento-inscripcion.dto";
import {EventoService} from '@/services/evento.service';
import {MessageService} from 'primeng/api';
import {CardModule} from 'primeng/card';
import {ButtonModule} from 'primeng/button';
import {CheckboxModule} from 'primeng/checkbox';
import {ToastModule} from 'primeng/toast';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {TagModule} from 'primeng/tag';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiTagComponent } from '@/ui/ui-tag.component';
@Component({
    selector: 'app-inscripcion-eventos',
    standalone: true,
    imports: [UiButtonDirective, UiTagComponent, IconComponent, CommonModule, FormsModule, CardModule, ButtonModule, CheckboxModule, ToastModule, ProgressSpinnerModule, TagModule],
    templateUrl: './inscripcion-eventos.component.html',
    styleUrls: ['./inscripcion-eventos.component.scss'],
    providers: [MessageService]
})
export class InscripcionEventosComponent implements OnInit, OnDestroy {
    /** Cada cuánto se refresca la lista en segundo plano para reflejar cambios de otros socios. */
    private static readonly INTERVALO_POLLING_MS = 20000;

    eventos: EventoInscripcionDTO[] = [];
    loading = true;

    private pollingSub?: Subscription;

    /**
     * Acciones en vuelo, con clave `eventoUid:socioUid`. Se guarda por persona, y no con una
     * única bandera, para que el indicador salga solo en el botón que se ha pulsado y no en
     * todas las tarjetas ni en todos los miembros del multicarnet.
     */
    private readonly accionesEnCurso = new Set<string>();

    /** Fichas marcadas para inscribir, por evento. */
    private readonly seleccion = new Map<string, Set<string>>();

    /** Preferencia "solo si entramos todos", por evento. */
    private readonly grupoIndivisible = new Map<string, boolean>();

    private eventoService = inject(EventoService);
    private messageService = inject(MessageService);

    ngOnInit(): void {
        this.cargarEventos();
        this.iniciarPolling();
    }

    ngOnDestroy(): void {
        this.pollingSub?.unsubscribe();
    }

    /**
     * Refresco periódico en segundo plano: las plazas libres y la lista de espera dependen de lo
     * que hagan otros socios, así que sin esto solo se ven actualizadas al recargar la página.
     * Se salta el ciclo si hay una inscripción/anulación en vuelo, para no pisar esa respuesta.
     */
    private iniciarPolling(): void {
        const intervalo = InscripcionEventosComponent.INTERVALO_POLLING_MS;
        this.pollingSub = timer(intervalo, intervalo).subscribe(() => {
            if (this.accionesEnCurso.size > 0) return;
            this.cargarEventos(false, false);
        });
    }

    /**
     * Recarga el listado. Tras inscribir o anular se vuelve a pedir en lugar de parchear la
     * tarjeta en memoria, porque cambian también las plazas libres y la lista de espera del
     * evento, que dependen de lo que hagan los demás socios.
     *
     * `notificarError` se pone a `false` en el refresco periódico para no bombardear al usuario
     * con un toast cada vez que un ciclo de polling falla; ya lo verá si la próxima carga visible
     * (inicial o tras una acción) también falla.
     */
    private cargarEventos(mostrarSpinner = true, notificarError = true) {
        this.loading = mostrarSpinner;
        this.eventoService.getEventosParaInscripcion().subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.eventos = response.data.map(evento => ({...evento, misSocios: evento.misSocios ?? []}));
                }
                this.loading = false;
            },
            error: () => {
                this.loading = false;
                if (!notificarError) return;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'No se pudieron cargar los eventos.'
                });
            }
        });
    }

    // ----------------------------------------------------------------
    // Estado del multicarnet
    // ----------------------------------------------------------------

    /** true si la cuenta tiene más de una ficha de socio (p. ej. un padre con sus hijos). */
    esMulticarnet(evento: EventoInscripcionDTO): boolean {
        return evento.misSocios.length > 1;
    }

    /** Fichas todavía sin inscribir en este evento. */
    pendientes(evento: EventoInscripcionDTO): SocioInscripcion[] {
        return evento.misSocios.filter(socio => !socio.estado);
    }

    /** Fichas ya inscritas, con plaza o en espera. */
    inscritos(evento: EventoInscripcionDTO): SocioInscripcion[] {
        return evento.misSocios.filter(socio => !!socio.estado);
    }

    enCurso(evento: EventoInscripcionDTO, socio?: SocioInscripcion): boolean {
        return this.accionesEnCurso.has(this.clave(evento, socio));
    }

    /** true si hay cualquier acción en vuelo sobre este evento. */
    eventoOcupado(evento: EventoInscripcionDTO): boolean {
        return [...this.accionesEnCurso].some(clave => clave.startsWith(evento.uid + ':'));
    }

    // ----------------------------------------------------------------
    // Selección
    // ----------------------------------------------------------------

    estaSeleccionado(evento: EventoInscripcionDTO, socio: SocioInscripcion): boolean {
        return this.seleccion.get(evento.uid)?.has(socio.socioUid) ?? false;
    }

    alternarSeleccion(evento: EventoInscripcionDTO, socio: SocioInscripcion) {
        const seleccionados = this.seleccion.get(evento.uid) ?? new Set<string>();
        if (seleccionados.has(socio.socioUid)) {
            seleccionados.delete(socio.socioUid);
        } else {
            seleccionados.add(socio.socioUid);
        }
        this.seleccion.set(evento.uid, seleccionados);
    }

    numSeleccionados(evento: EventoInscripcionDTO): number {
        return this.seleccion.get(evento.uid)?.size ?? 0;
    }

    soloSiEntranTodos(evento: EventoInscripcionDTO): boolean {
        return this.grupoIndivisible.get(evento.uid) ?? false;
    }

    alternarSoloSiEntranTodos(evento: EventoInscripcionDTO) {
        this.grupoIndivisible.set(evento.uid, !this.soloSiEntranTodos(evento));
    }

    /** La opción de no separar al grupo solo tiene sentido apuntando a dos o más a la vez. */
    puedeElegirGrupoIndivisible(evento: EventoInscripcionDTO): boolean {
        return this.numSeleccionados(evento) > 1;
    }

    puedeInscribir(evento: EventoInscripcionDTO): boolean {
        if (evento.inscripcionCerrada || this.eventoOcupado(evento)) return false;
        return this.esMulticarnet(evento)
            ? this.numSeleccionados(evento) > 0
            : this.pendientes(evento).length > 0;
    }

    /** Texto del botón principal, para que se vea a cuántas personas se va a apuntar. */
    textoBotonInscribir(evento: EventoInscripcionDTO): string {
        if (!this.esMulticarnet(evento)) return 'Inscribir';
        const seleccionados = this.numSeleccionados(evento);
        if (seleccionados === 0) return 'Selecciona a quién inscribir';
        return seleccionados === 1 ? 'Inscribir a 1 persona' : `Inscribir a ${seleccionados} personas`;
    }

    // ----------------------------------------------------------------
    // Acciones
    // ----------------------------------------------------------------

    inscribir(evento: EventoInscripcionDTO) {
        if (!this.puedeInscribir(evento)) return;

        const socioUids = this.esMulticarnet(evento)
            ? this.pendientes(evento)
                  .filter(socio => this.estaSeleccionado(evento, socio))
                  .map(socio => socio.socioUid)
            : this.pendientes(evento).map(socio => socio.socioUid);
        if (socioUids.length === 0) return;

        const clave = this.clave(evento);
        this.accionesEnCurso.add(clave);

        this.eventoService.inscribir(evento.uid, {
            socioUids,
            soloSiEntranTodos: socioUids.length > 1 && this.soloSiEntranTodos(evento)
        }).pipe(
            finalize(() => this.accionesEnCurso.delete(clave))
        ).subscribe({
            next: (resp) => {
                const alguienEnEspera = (resp.data ?? []).some(socio => socio.estado === 'EN_ESPERA');
                this.seleccion.delete(evento.uid);
                this.grupoIndivisible.delete(evento.uid);
                this.messageService.add({
                    severity: alguienEnEspera ? 'warn' : 'success',
                    summary: alguienEnEspera ? 'Lista de espera' : '¡Inscrito!',
                    detail: resp.message || 'Inscripción realizada en ' + evento.nombreEvento
                });
                this.cargarEventos(false);
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudo realizar la inscripción.'
            })
        });
    }

    anularInscripcion(evento: EventoInscripcionDTO, socio: SocioInscripcion) {
        if (this.eventoOcupado(evento)) return;

        const clave = this.clave(evento, socio);
        this.accionesEnCurso.add(clave);

        this.eventoService.anularInscripcion(evento.uid, socio.socioUid).pipe(
            finalize(() => this.accionesEnCurso.delete(clave))
        ).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'info',
                    summary: 'Anulado',
                    detail: 'Se ha anulado la inscripción de ' + socio.nombre
                });
                this.cargarEventos(false);
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudo anular la inscripción.'
            })
        });
    }

    // ----------------------------------------------------------------
    // Presentación
    // ----------------------------------------------------------------

    plazasTexto(evento: EventoInscripcionDTO): string {
        if (evento.plazasLibres < 0) {
            return 'Plazas ilimitadas';
        }
        return `${evento.plazasLibres} plazas libres de ${evento.plazasOcupadas + evento.plazasLibres}`;
    }

    etiquetaSocio(socio: SocioInscripcion): string {
        return socio.numeroSocio != null ? `${socio.nombre} (nº ${socio.numeroSocio})` : socio.nombre;
    }

    private clave(evento: EventoInscripcionDTO, socio?: SocioInscripcion): string {
        return evento.uid + ':' + (socio?.socioUid ?? '');
    }
}

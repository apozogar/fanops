import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {finalize, Subscription, timer} from 'rxjs';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {EventoInscripcionDTO, SocioInscripcion} from "@/interfaces/evento-inscripcion.dto";
import {SocioSolicitudCarnet, SorteoResumen} from '@/interfaces/sorteo-carnet.dto';
import {EventoService} from '@/services/evento.service';
import {SorteoCarnetService} from '@/services/sorteo-carnet.service';
import {ConfirmationService, MessageService} from 'primeng/api';
import {CardModule} from 'primeng/card';
import {ButtonModule} from 'primeng/button';
import {CheckboxModule} from 'primeng/checkbox';
import {ToastModule} from 'primeng/toast';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {DialogModule} from 'primeng/dialog';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {TagModule} from 'primeng/tag';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { TagTone, UiTagComponent } from '@/ui/ui-tag.component';
import { SorteoBomboComponent } from '@/components/sorteo-bombo/sorteo-bombo.component';
@Component({
    selector: 'app-inscripcion-eventos',
    standalone: true,
    imports: [UiButtonDirective, UiTagComponent, IconComponent, CommonModule, FormsModule, CardModule, ButtonModule, CheckboxModule, ConfirmDialogModule, DialogModule, ToastModule, ProgressSpinnerModule, TagModule, SorteoBomboComponent],
    templateUrl: './inscripcion-eventos.component.html',
    styleUrls: ['./inscripcion-eventos.component.scss'],
    providers: [MessageService, ConfirmationService]
})
export class InscripcionEventosComponent implements OnInit, OnDestroy {
    /** Cada cuánto se refresca la lista en segundo plano para reflejar cambios de otros socios. */
    private static readonly INTERVALO_POLLING_MS = 20000;

    eventos: EventoInscripcionDTO[] = [];
    loading = true;

    /** Evento cuyo bombo se está mirando; null con el diálogo cerrado. */
    eventoDelSorteo: EventoInscripcionDTO | null = null;

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
    private sorteoService = inject(SorteoCarnetService);
    private messageService = inject(MessageService);
    private confirmationService = inject(ConfirmationService);

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
            // Con el bombo abierto tampoco: el componente del sorteo lleva su propio estado y
            // recargar por detrás solo provocaría parpadeos en la tarjeta de debajo.
            if (this.eventoDelSorteo) return;
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

    /**
     * Fichas sobre las que actúa el botón: en un multicarnet las marcadas, y si la cuenta tiene
     * una sola ficha, esa. Lo comparten las dos formas de apuntarse.
     */
    private fichasAApuntar(evento: EventoInscripcionDTO): string[] {
        return this.esMulticarnet(evento)
            ? this.pendientes(evento)
                  .filter(socio => this.estaSeleccionado(evento, socio))
                  .map(socio => socio.socioUid)
            : this.pendientes(evento).map(socio => socio.socioUid);
    }

    inscribir(evento: EventoInscripcionDTO) {
        if (!this.puedeInscribir(evento)) return;

        const socioUids = this.fichasAApuntar(evento);
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

    /**
     * Antes de anular se pregunta al servidor si la baja costaría una falta. Se consulta en vez de
     * calcularlo aquí porque depende de si queda alguien en la lista de espera que cubra el hueco,
     * y eso cambia con lo que hagan otros socios.
     */
    anularInscripcion(evento: EventoInscripcionDTO, socio: SocioInscripcion) {
        if (this.eventoOcupado(evento)) return;

        const clave = this.clave(evento, socio);
        this.accionesEnCurso.add(clave);

        this.eventoService.avisoAnulacion(evento.uid, socio.socioUid).pipe(
            finalize(() => this.accionesEnCurso.delete(clave))
        ).subscribe({
            next: (resp) => this.confirmarAnulacion(evento, socio, resp.data === true),
            // Si el aviso falla no se bloquea la baja: se pregunta con el texto conservador.
            error: () => this.confirmarAnulacion(evento, socio, true)
        });
    }

    private confirmarAnulacion(evento: EventoInscripcionDTO, socio: SocioInscripcion, costariaFalta: boolean) {
        const mensaje = costariaFalta
            ? `El plazo de inscripción ya está cerrado y no hay nadie en lista de espera para ocupar la plaza de ${socio.nombre}. Si la anulas ahora se le registrará una falta, y su próxima inscripción irá a lista de espera. ¿Anular igualmente?`
            : `¿Anular la inscripción de ${socio.nombre}?`;

        this.confirmationService.confirm({
            message: mensaje,
            header: costariaFalta ? 'Esto supondrá una falta' : 'Confirmar baja',
            accept: () => this.ejecutarAnulacion(evento, socio)
        });
    }

    private ejecutarAnulacion(evento: EventoInscripcionDTO, socio: SocioInscripcion) {
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

    // ----------------------------------------------------------------
    // Sorteo de carnets
    // ----------------------------------------------------------------

    /** Fichas de la cuenta que ya están dentro del bombo de ese evento. */
    fichasEnSorteo(sorteo: SorteoResumen): SocioSolicitudCarnet[] {
        return sorteo.misSocios.filter(socio => !!socio.estado);
    }

    /** Fichas de la cuenta que todavía no están en el bombo. */
    fichasFueraDelSorteo(sorteo: SorteoResumen): SocioSolicitudCarnet[] {
        return sorteo.misSocios.filter(socio => !socio.estado);
    }

    /**
     * true si hay que ofrecer elegir. Apuntarse al sorteo ya apunta al evento, así que las dos
     * altas no son cosas que se sumen: son dos formas de apuntarse y hay que escoger una.
     */
    puedeElegirComoApuntarse(evento: EventoInscripcionDTO): boolean {
        return !!evento.sorteo?.admiteSolicitudes && this.pendientes(evento).length > 0;
    }

    /** true si ya está apuntado al evento y lo único que le queda es entrar en el bombo. */
    puedeEntrarSoloAlSorteo(evento: EventoInscripcionDTO): boolean {
        const sorteo = evento.sorteo;

        return !!sorteo?.admiteSolicitudes
            && this.pendientes(evento).length === 0
            && this.fichasFueraDelSorteo(sorteo).length > 0;
    }

    /**
     * Alta por la vía del sorteo: mete en el bombo y, con ello, apunta al evento. Se piden solo
     * las fichas que no estén ya dentro, para que el servidor no rechace la operación entera por
     * una que repetía.
     */
    apuntarAlSorteo(evento: EventoInscripcionDTO) {
        if (!this.puedeInscribir(evento) || !evento.sorteo) return;

        const yaEnElBombo = new Set(this.fichasEnSorteo(evento.sorteo).map(socio => socio.socioUid));
        const socioUids = this.fichasAApuntar(evento).filter(uid => !yaEnElBombo.has(uid));
        if (socioUids.length === 0) return;

        const clave = this.clave(evento);
        this.accionesEnCurso.add(clave);

        this.sorteoService.apuntar(evento.uid, socioUids,
            socioUids.length > 1 && this.soloSiEntranTodos(evento)
        ).pipe(
            finalize(() => this.accionesEnCurso.delete(clave))
        ).subscribe({
            next: (resp) => {
                this.seleccion.delete(evento.uid);
                this.grupoIndivisible.delete(evento.uid);
                this.messageService.add({
                    severity: 'success',
                    summary: '¡En el bombo!',
                    detail: resp.message || 'Ya estáis en el sorteo y apuntados al evento.'
                });
                this.cargarEventos(false);
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudo entrar en el sorteo.'
            })
        });
    }

    /** El diálogo se abre y se cierra con el evento seleccionado, sin una bandera aparte. */
    get sorteoVisible(): boolean {
        return this.eventoDelSorteo !== null;
    }

    set sorteoVisible(visible: boolean) {
        if (!visible) this.eventoDelSorteo = null;
    }

    abrirSorteo(evento: EventoInscripcionDTO) {
        this.eventoDelSorteo = evento;
    }

    cerrarSorteo() {
        this.eventoDelSorteo = null;
    }

    /** Apuntarse o devolver un carnet cambia los contadores de la tarjeta, así que se recarga. */
    sorteoCambiado() {
        this.cargarEventos(false);
    }

    tonoSorteo(socio: SocioSolicitudCarnet): TagTone {
        switch (socio.estado) {
            case 'GANADORA':
                return 'success';
            case 'SUPLENTE':
                return 'warn';
            case 'RENUNCIADA':
                return 'neutral';
            default:
                return 'accent';
        }
    }

    textoSorteo(socio: SocioSolicitudCarnet, sorteo: SorteoResumen): string {
        switch (socio.estado) {
            case 'GANADORA':
                return socio.nombre + ': ¡le ha tocado carnet!';
            case 'SUPLENTE':
                return socio.nombre + ': suplente nº ' + ((socio.posicion ?? 0) - sorteo.plazasCarnet);
            case 'RENUNCIADA':
                return socio.nombre + ': carnet devuelto';
            default:
                return socio.nombre + ': en el bombo';
        }
    }

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

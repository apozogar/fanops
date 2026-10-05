import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MessageService, ConfirmationService} from 'primeng/api';
import {Table, TableModule} from 'primeng/table';
import {Evento} from '@/interfaces/evento.interface';
import {AsistenciaEvento, FaltaEvento, InscripcionAdmin} from '@/interfaces/evento-inscripcion.dto';
import {InputTextModule} from 'primeng/inputtext';
import {InputNumberModule} from 'primeng/inputnumber';
import {ToastModule} from 'primeng/toast';
import {ToolbarModule} from 'primeng/toolbar';
import {DialogModule} from 'primeng/dialog';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {CardModule} from 'primeng/card';
import {TextareaModule} from 'primeng/textarea';
import {DatePickerModule} from 'primeng/datepicker';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {TagModule} from 'primeng/tag';
import {TooltipModule} from 'primeng/tooltip';
import {EventoService} from '@/services/evento.service';
import {SorteoCarnetService} from '@/services/sorteo-carnet.service';
import {ParticipanteSorteo, SorteoCarnet} from '@/interfaces/sorteo-carnet.dto';
import {ValoresEventoService} from '@/services/valores-evento.service';
import {ValoresEvento} from '@/interfaces/valores-evento.dto';
import {fechaRelativaAlEvento} from '@/core/eventos/fechas-por-defecto';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiTagComponent } from '@/ui/ui-tag.component';
@Component({
    selector: 'app-eventos',
    standalone: true,
    imports: [UiButtonDirective, UiTagComponent, IconComponent, 
        CommonModule,
        FormsModule,
        TableModule,
        InputTextModule,
        InputNumberModule,
        ToastModule,
        ToolbarModule,
        DialogModule,
        ConfirmDialogModule,
        CardModule,
        TextareaModule,
        DatePickerModule,
        IconFieldModule,
        InputIconModule,
        TagModule,
        TooltipModule,
    ],
    templateUrl: './EventosComponent.html',
    styleUrls: ['./EventosComponent.scss'],
    providers: [MessageService, ConfirmationService]
})

export class EventosComponent implements OnInit {
    eventos: Evento[] = [];
    evento: Partial<Evento> = {};
    eventoDialog: boolean = false;
    /** Valores con los que se propone un evento nuevo, configurables por la peña. */
    valoresPorDefecto: ValoresEvento = {};
    valoresDialog: boolean = false;
    valoresEnEdicion: ValoresEvento = {};
    /** Las horas se editan con el selector de PrimeNG, que trabaja con Date, no con 'HH:mm'. */
    horaFinInscripcionEdicion: Date | null = null;
    horaSorteoEdicion: Date | null = null;
    guardandoValores: boolean = false;
    inscripcionesDialog: boolean = false;
    inscripciones: InscripcionAdmin[] = [];
    faltas: FaltaEvento[] = [];
    eventoSeleccionado: Evento | null = null;
    pestanaInscripciones: 'confirmados' | 'espera' | 'faltas' = 'confirmados';
    filtroInscripciones = '';
    loading: boolean = false;
    asignandoPlazas: boolean = false;
    /** Evento cuyo sorteo de carnets se está adelantando. */
    celebrandoSorteo: string | null = null;
    /** Bombo abierto en el diálogo de papeletas; null mientras carga. */
    bomboDialog: boolean = false;
    bombo: SorteoCarnet | null = null;
    /** Papeletas extra tecleadas por participante, pendientes de guardar. */
    papeletasEnEdicion: Record<string, number | null> = {};
    ajustandoPapeletas: string | null = null;
    eliminandoInscripcion: string | null = null;
    /** Inscripción con un cambio de asistencia o de falta en vuelo. */
    marcandoAsistencia: string | null = null;

    private readonly eventoService = inject(EventoService);
    private readonly sorteoCarnetService = inject(SorteoCarnetService);
    private readonly valoresEventoService = inject(ValoresEventoService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    public numEventos = 0;
    public numEventosPendientes = 0;

    @ViewChild('dt') dt: Table | undefined;

    ngOnInit() {
        this.cargarEventos();
        this.cargarValoresPorDefecto();
    }

    // ----------------------------------------------------------------
    // Valores por defecto de la peña
    // ----------------------------------------------------------------

    /**
     * Se cargan al entrar y no al abrir el formulario: así el evento nuevo aparece ya relleno,
     * sin el parpadeo de unos campos que se completan solos medio segundo después.
     */
    private cargarValoresPorDefecto() {
        this.valoresEventoService.obtener().subscribe({
            next: (resp) => this.valoresPorDefecto = resp.data ?? {},
            // Un fallo aquí no rompe nada: el formulario simplemente sale vacío.
            error: () => this.valoresPorDefecto = {}
        });
    }

    abrirValoresPorDefecto() {
        this.valoresEnEdicion = {...this.valoresPorDefecto};
        this.horaFinInscripcionEdicion = this.aHora(this.valoresPorDefecto.horaFinInscripcion);
        this.horaSorteoEdicion = this.aHora(this.valoresPorDefecto.horaSorteo);
        this.valoresDialog = true;
    }

    /** 'HH:mm' a Date, que es con lo que trabaja el selector de hora. */
    private aHora(texto?: string | null): Date | null {
        if (!texto) return null;

        const [horas, minutos] = texto.split(':');
        const fecha = new Date();

        fecha.setHours(Number(horas), Number(minutos), 0, 0);

        return fecha;
    }

    private deHora(fecha?: Date | null): string | null {
        if (!fecha) return null;

        return this.dosDigitos(fecha.getHours()) + ':' + this.dosDigitos(fecha.getMinutes());
    }

    private dosDigitos(valor: number): string {
        return valor.toString().padStart(2, '0');
    }

    guardarValoresPorDefecto() {
        this.valoresEnEdicion.horaFinInscripcion = this.deHora(this.horaFinInscripcionEdicion);
        this.valoresEnEdicion.horaSorteo = this.deHora(this.horaSorteoEdicion);
        this.guardandoValores = true;
        this.valoresEventoService.guardar(this.valoresEnEdicion).subscribe({
            next: (resp) => {
                this.guardandoValores = false;
                this.valoresPorDefecto = resp.data ?? {};
                this.valoresDialog = false;
                this.messageService.add({
                    severity: 'success',
                    summary: 'Guardado',
                    detail: resp.message || 'Valores por defecto guardados'
                });
            },
            error: (err) => {
                this.guardandoValores = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudieron guardar los valores'
                });
            }
        });
    }

    cargarEventos() {
        this.loading = true;
        this.numEventosPendientes = 0;
        this.eventoService.getEventosParaGestion().subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.eventos = response.data;
                    this.numEventos = this.eventos.length;
                    this.eventos.forEach((p) => {
                        p.fechaEvento = new Date(p.fechaEvento);
                        if (p.fechaLimiteInscripcion) {
                            p.fechaLimiteInscripcion = new Date(p.fechaLimiteInscripcion);
                        }
                        if (p.fechaSorteoCarnet) {
                            p.fechaSorteoCarnet = new Date(p.fechaSorteoCarnet);
                        }
                        // Pendientes = próximos, con inscripción pendiente de cerrarse/asignarse
                        if (p.fechaEvento >= new Date() && !p.inscripcionCerrada) {
                            this.numEventosPendientes += 1;
                        }
                    });
                }
                this.loading = false;
            },
            error: () => {
                this.loading = false;
            }
        });
    }

    /** Solo tiene sentido adelantar un sorteo que existe y todavía no se ha celebrado. */
    puedeSortear(evento: Evento): boolean {
        return !!evento.plazasCarnet && !!evento.fechaSorteoCarnet && !evento.sorteoCelebrado;
    }

    /**
     * Adelanta el sorteo de carnets. No cambia el resultado (la semilla estaba comprometida desde
     * que se programó), solo el momento en que se sabe, pero sí es irreversible: por eso se
     * pregunta.
     */
    celebrarSorteo(evento: Evento) {
        if (!evento.uid) return;
        this.confirmationService.confirm({
            header: 'Celebrar el sorteo ahora',
            message: `El sorteo de carnets de '${evento.nombreEvento}' estaba previsto para el `
                + `${evento.fechaSorteoCarnet?.toLocaleString('es-ES') ?? 'futuro'}. Si lo celebras `
                + 'ahora, quien no se haya apuntado se queda fuera y no se puede deshacer. '
                + '¿Continuar?',
            accept: () => this.ejecutarSorteo(evento)
        });
    }

    /** Abre el bombo del evento: quién está dentro y con cuántas papeletas. */
    abrirBombo(evento: Evento) {
        if (!evento.uid) return;
        this.bombo = null;
        this.bomboDialog = true;
        this.sorteoCarnetService.consultar(evento.uid).subscribe({
            next: (resp) => this.mostrarBombo(resp.data),
            error: (err) => {
                this.bomboDialog = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo cargar el sorteo'
                });
            }
        });
    }

    private mostrarBombo(sorteo: SorteoCarnet) {
        this.bombo = sorteo;
        this.papeletasEnEdicion = {};
        for (const p of sorteo.participantes) {
            this.papeletasEnEdicion[p.socioUid] = p.papeletasExtra ?? 0;
        }
    }

    /** Parte del total de papeletas del bombo. Con un carnet es la probabilidad de llevárselo. */
    porcentajePapeletas(papeletas: number): number {
        const total = (this.bombo?.participantes ?? []).reduce((suma, p) => suma + p.papeletas, 0);
        return total > 0 ? (papeletas * 100) / total : 0;
    }

    ajustarPapeletas(participante: ParticipanteSorteo) {
        if (!this.bombo) return;
        const extra = this.papeletasEnEdicion[participante.socioUid] ?? 0;
        this.ajustandoPapeletas = participante.socioUid;
        this.sorteoCarnetService.ajustarPapeletasExtra(this.bombo.eventoUid, participante.socioUid, extra)
            .subscribe({
                next: (resp) => {
                    this.ajustandoPapeletas = null;
                    this.mostrarBombo(resp.data);
                },
                error: (err) => {
                    this.ajustandoPapeletas = null;
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error',
                        detail: err.error?.message || 'No se pudieron guardar las papeletas'
                    });
                }
            });
    }

    private ejecutarSorteo(evento: Evento) {
        this.celebrandoSorteo = evento.uid!;
        this.sorteoCarnetService.celebrar(evento.uid!).subscribe({
            next: (resp) => {
                this.celebrandoSorteo = null;
                const ganadores = (resp.data?.participantes ?? [])
                    .filter(p => (p.posicion ?? 0) <= (resp.data?.plazasCarnet ?? 0))
                    .map(p => p.nombre)
                    .join(', ');
                this.messageService.add({
                    severity: 'success',
                    summary: 'Sorteo celebrado',
                    detail: ganadores ? 'Carnets para: ' + ganadores : 'No había nadie en el bombo.'
                });
                this.cargarEventos();
            },
            error: (err) => {
                this.celebrandoSorteo = null;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo celebrar el sorteo'
                });
            }
        });
    }

    /**
     * Un evento nuevo nace con los valores por defecto de la peña. Casi todos los eventos se
     * parecen (mismo autobús, mismo precio, mismos carnets), así que se proponen ya rellenos;
     * son sugerencias y se pueden cambiar antes de guardar.
     */
    abrirNuevo() {
        const porDefecto = this.valoresPorDefecto;
        this.evento = {
            nombreEvento: '',
            numeroPlazas: porDefecto.plazas ?? undefined,
            costePlaza: porDefecto.costePlaza ?? undefined,
            plazasCarnet: porDefecto.carnets ?? undefined,
            costeCarnet: porDefecto.costeCarnet ?? undefined,
            costeTotalEstimado: porDefecto.costeTotalEstimado ?? undefined
        };
        this.eventoDialog = true;
    }

    /**
     * Al elegir la fecha del evento se proponen las que dependen de ella. Solo en un evento nuevo
     * y solo si el campo sigue vacío: si no, corregir la fecha del partido movería un plazo que
     * quien lo está creando ya había puesto a mano.
     */
    alCambiarFechaEvento() {
        if (this.evento.uid || !this.evento.fechaEvento) return;

        const porDefecto = this.valoresPorDefecto;

        if (!this.evento.fechaLimiteInscripcion && porDefecto.diasAntesFinInscripcion != null) {
            this.evento.fechaLimiteInscripcion = fechaRelativaAlEvento(this.evento.fechaEvento,
                porDefecto.diasAntesFinInscripcion, porDefecto.horaFinInscripcion);
        }
        if (!this.evento.fechaSorteoCarnet && porDefecto.diasAntesSorteo != null) {
            this.evento.fechaSorteoCarnet = fechaRelativaAlEvento(this.evento.fechaEvento,
                porDefecto.diasAntesSorteo, porDefecto.horaSorteo);
        }
    }

    editarEvento(evento: Evento) {
        this.evento = {...evento};
        this.eventoDialog = true;
    }

    /**
     * Abre el listado desde la tabla. La pestaña y la búsqueda solo se reinician aquí, no en
     * mostrarInscripciones, que también se llama para refrescar tras pasar lista o dar de baja y
     * no debe sacar al usuario de donde estaba.
     */
    abrirInscripciones(evento: Evento) {
        this.pestanaInscripciones = 'confirmados';
        this.filtroInscripciones = '';
        this.mostrarInscripciones(evento);
    }

    mostrarInscripciones(evento: Evento) {
        if (!evento.uid) return;
        this.eventoSeleccionado = evento;
        this.inscripciones = [];
        this.faltas = [];
        this.inscripcionesDialog = true;
        this.eventoService.getInscripciones(evento.uid).subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.inscripciones = response.data;
                }
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudieron cargar las inscripciones.'
            })
        });
        // Las faltas se piden aparte: una cancelación tardía borra la inscripción, así que hay
        // gente que ha fallado y no sale en ninguna de las otras dos listas.
        this.eventoService.getFaltas(evento.uid).subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.faltas = response.data;
                }
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudieron cargar las faltas.'
            })
        });
    }

    get inscritos(): InscripcionAdmin[] {
        return this.inscripciones.filter(i => i.estado === 'CONFIRMADA');
    }

    get enEspera(): InscripcionAdmin[] {
        return this.inscripciones.filter(i => i.estado === 'EN_ESPERA');
    }

    /** Inscripciones de la pestaña activa que casan con la búsqueda. */
    get inscripcionesVisibles(): InscripcionAdmin[] {
        const lista = this.pestanaInscripciones === 'espera' ? this.enEspera : this.inscritos;
        return lista.filter(i => this.coincideBusqueda(i.nombre, i.numeroSocio, i.email, i.telefono));
    }

    get faltasVisibles(): FaltaEvento[] {
        return this.faltas.filter(f => this.coincideBusqueda(f.nombre, f.numeroSocio));
    }

    private coincideBusqueda(...campos: (string | number | null | undefined)[]): boolean {
        const buscado = normalizarTexto(this.filtroInscripciones);
        return !buscado || campos.some(c => c != null && normalizarTexto(String(c)).includes(buscado));
    }

    /** Dos iniciales para el avatar: nombre y primer apellido. */
    iniciales(nombre: string): string {
        return (nombre ?? '').trim().split(/\s+/).slice(0, 2)
            .map(p => p.charAt(0)).join('').toUpperCase() || '?';
    }

    trackPorUid(_: number, item: { uid: string }): string {
        return item.uid;
    }

    plazasDisponibles(evento: Evento): boolean {
        if (evento.numeroPlazas == null) return true; // sin límite de plazas
        return (evento.numInscritos ?? 0) < evento.numeroPlazas;
    }

    /**
     * Copia el enlace público de inscripción. Lleva una versión (`?v=`) que cambia con los datos
     * del evento: WhatsApp guarda la vista previa de cada URL, y sin ella un enlace compartido una
     * vez con una vista previa fallida o desfasada se quedaría así para siempre.
     */
    copiarEnlacePublico(evento: Evento) {
        if (!evento.uid) return;
        const enlace = window.location.origin + '/inscripcion/' + evento.uid + '?v=' + versionEnlace(evento);
        navigator.clipboard?.writeText(enlace).then(() => {
            this.messageService.add({
                severity: 'success',
                summary: 'Enlace copiado',
                detail: 'Comparte este enlace para que no socios se apunten: ' + enlace
            });
        }).catch(() => {
            this.messageService.add({
                severity: 'warn',
                summary: 'Enlace',
                detail: enlace
            });
        });
    }

    asignarPlazas() {
        if (!this.eventoSeleccionado?.uid) return;
        this.asignandoPlazas = true;
        this.eventoService.asignarPlazas(this.eventoSeleccionado.uid).subscribe({
            next: (resp) => {
                this.asignandoPlazas = false;
                this.messageService.add({
                    severity: 'success',
                    summary: 'Plazas asignadas',
                    detail: resp.message || 'Plazas asignadas desde la lista de espera.'
                });
                this.mostrarInscripciones(this.eventoSeleccionado!);
                this.cargarEventos();
            },
            error: (err) => {
                this.asignandoPlazas = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudieron asignar las plazas.'
                });
            }
        });
    }

    // ----------------------------------------------------------------
    // Pasar lista y faltas
    // ----------------------------------------------------------------

    /**
     * Pasa lista marcando a cada inscrito como presente o ausente. Se hace por persona en lugar
     * de con un guardado global para que sea reversible al momento: si te equivocas, vuelves a
     * pulsar el botón ya activo y la marca (y la falta, si la había) desaparece.
     */
    marcarAsistencia(inscripcion: InscripcionAdmin, asistencia: AsistenciaEvento) {
        const evento = this.eventoSeleccionado;
        if (!evento?.uid || this.marcandoAsistencia) return;

        // Volver a pulsar el botón ya activo deja la persona sin pasar lista.
        const destino: AsistenciaEvento = inscripcion.asistencia === asistencia ? 'PENDIENTE' : asistencia;

        this.marcandoAsistencia = inscripcion.uid;
        this.eventoService.marcarAsistencia(evento.uid, inscripcion.uid, destino).subscribe({
            next: (resp) => {
                this.marcandoAsistencia = null;
                this.messageService.add({
                    severity: destino === 'NO_ASISTIO' ? 'warn' : 'success',
                    summary: resp.message || 'Asistencia actualizada',
                    detail: destino === 'NO_ASISTIO'
                        ? `${inscripcion.nombre} acumula ${resp.data} falta(s)`
                        : inscripcion.nombre
                });
                this.mostrarInscripciones(evento);
            },
            error: (err) => {
                this.marcandoAsistencia = null;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo registrar la asistencia.'
                });
            }
        });
    }

    /** Inscritos a los que todavía no se ha pasado lista, para saber qué queda por revisar. */
    get pendientesDeLista(): number {
        return this.inscritos.filter(i => !i.asistencia || i.asistencia === 'PENDIENTE').length;
    }

    /** Retira una falta desde la pestaña de fallos, incluida la de una cancelación tardía. */
    quitarFalta(falta: FaltaEvento) {
        const evento = this.eventoSeleccionado;
        if (!evento?.uid) return;

        this.confirmationService.confirm({
            message: `¿Retirar la falta de ${falta.nombre}? Dejará de penalizarle en sus próximas inscripciones.`,
            header: 'Retirar falta',
            accept: () => {
                this.marcandoAsistencia = falta.uid;
                this.eventoService.quitarFalta(falta.uid).subscribe({
                    next: () => {
                        this.marcandoAsistencia = null;
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Falta retirada',
                            detail: falta.nombre
                        });
                        this.mostrarInscripciones(evento);
                    },
                    error: (err) => {
                        this.marcandoAsistencia = null;
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: err.error?.message || 'No se pudo retirar la falta.'
                        });
                    }
                });
            }
        });
    }

    /** Texto del motivo para la pestaña de fallos. */
    motivoFaltaTexto(falta: FaltaEvento): string {
        return falta.motivo === 'CANCELACION_TARDIA'
            ? 'Anuló fuera de plazo'
            : 'No se presentó';
    }

    eliminarInscripcion(inscripcion: InscripcionAdmin) {
        const evento = this.eventoSeleccionado;
        if (!evento?.uid || !inscripcion.uid) return;

        const enEspera = inscripcion.estado === 'EN_ESPERA';
        this.confirmationService.confirm({
            message: enEspera
                ? `¿Quitar a ${inscripcion.nombre} de la lista de espera?`
                : `¿Dar de baja a ${inscripcion.nombre}? Su plaza pasará automáticamente al siguiente de la lista de espera.`,
            header: 'Confirmar baja',
            accept: () => {
                this.eliminandoInscripcion = inscripcion.uid;
                this.eventoService.eliminarInscripcion(evento.uid!, inscripcion.uid).subscribe({
                    next: (resp) => {
                        this.eliminandoInscripcion = null;
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Inscripción eliminada',
                            detail: resp.message || 'La inscripción se ha dado de baja.'
                        });
                        this.mostrarInscripciones(evento);
                        this.cargarEventos();
                    },
                    error: (err) => {
                        this.eliminandoInscripcion = null;
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: err.error?.message || 'No se pudo eliminar la inscripción.'
                        });
                    }
                });
            }
        });
    }

    eliminarEvento(evento: Evento) {
        this.confirmationService.confirm({
            message: '¿Está seguro que desea eliminar este evento?',
            header: 'Confirmar',
            accept: () => {
                if (!evento.uid) return;
                this.eventoService.eliminarEvento(evento.uid).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Éxito',
                            detail: 'Evento eliminado'
                        });
                        this.cargarEventos();
                    },
                    error: (err) => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: err.error?.message || 'No se pudo eliminar el evento'
                        });
                    }
                });
            }
        });
    }

    guardarEvento() {
        this.eventoService.guardarEvento(this.evento).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Éxito',
                    detail: 'Evento guardado correctamente'
                });
                this.eventoDialog = false;
                this.cargarEventos();
            },
            error: (err) => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo guardar el evento'
                });
            }
        });
    }
}

/** Versión del formato de la vista previa; subirla da URLs nuevas a todos los enlaces. */
const FORMATO_VISTA_PREVIA = 2;

/** Huella corta de lo que sale en la vista previa del enlace (djb2 en base 36). */
function versionEnlace(evento: Evento): string {
    const datos = [
        FORMATO_VISTA_PREVIA,
        evento.nombreEvento,
        evento.fechaEvento ? new Date(evento.fechaEvento).getTime() : '',
        evento.ubicacion ?? '',
        evento.fechaLimiteInscripcion ? new Date(evento.fechaLimiteInscripcion).getTime() : '',
        evento.costePlaza ?? ''
    ].join('|');
    let hash = 5381;
    for (let i = 0; i < datos.length; i++) {
        hash = ((hash << 5) + hash + datos.charCodeAt(i)) | 0;
    }
    return (hash >>> 0).toString(36);
}

/** Texto comparable en la búsqueda: sin tildes ni mayúsculas. */
function normalizarTexto(texto: string): string {
    return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}

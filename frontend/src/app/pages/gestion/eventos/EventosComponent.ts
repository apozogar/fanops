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
import {AccordionModule} from 'primeng/accordion';
import {TooltipModule} from 'primeng/tooltip';
import {EventoService} from '@/services/evento.service';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
@Component({
    selector: 'app-eventos',
    standalone: true,
    imports: [UiButtonDirective, IconComponent, 
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
        AccordionModule,
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
    inscripcionesDialog: boolean = false;
    inscripciones: InscripcionAdmin[] = [];
    faltas: FaltaEvento[] = [];
    eventoSeleccionado: Evento | null = null;
    loading: boolean = false;
    asignandoPlazas: boolean = false;
    eliminandoInscripcion: string | null = null;
    /** Inscripción con un cambio de asistencia o de falta en vuelo. */
    marcandoAsistencia: string | null = null;

    private eventoService = inject(EventoService);
    private messageService = inject(MessageService);
    private confirmationService = inject(ConfirmationService);

    public numEventos = 0;
    public numEventosPendientes = 0;

    @ViewChild('dt') dt: Table | undefined;

    ngOnInit() {
        this.cargarEventos();
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

    abrirNuevo() {
        this.evento = {nombreEvento: '', numeroPlazas: 0};
        this.eventoDialog = true;
    }

    editarEvento(evento: Evento) {
        this.evento = {...evento};
        this.eventoDialog = true;
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

    plazasDisponibles(evento: Evento): boolean {
        if (evento.numeroPlazas == null) return true; // sin límite de plazas
        return (evento.numInscritos ?? 0) < evento.numeroPlazas;
    }

    copiarEnlacePublico(evento: Evento) {
        if (!evento.uid) return;
        const enlace = window.location.origin + '/#/inscripcion/' + evento.uid;
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
     * Pone o retira la falta de un inscrito. Solo se marca a quien ha fallado: no hace falta
     * confirmar uno a uno a los que sí vinieron, que son la mayoría.
     */
    alternarFalta(inscripcion: InscripcionAdmin) {
        const evento = this.eventoSeleccionado;
        if (!evento?.uid || this.marcandoAsistencia) return;

        const tieneFalta = inscripcion.asistencia === 'NO_ASISTIO';
        const destino: AsistenciaEvento = tieneFalta ? 'PENDIENTE' : 'NO_ASISTIO';

        this.marcandoAsistencia = inscripcion.uid;
        this.eventoService.marcarAsistencia(evento.uid, inscripcion.uid, destino).subscribe({
            next: (resp) => {
                this.marcandoAsistencia = null;
                this.messageService.add({
                    severity: tieneFalta ? 'success' : 'warn',
                    summary: tieneFalta ? 'Falta retirada' : 'Falta registrada',
                    detail: tieneFalta
                        ? inscripcion.nombre
                        : `${inscripcion.nombre} acumula ${resp.data} falta(s)`
                });
                this.mostrarInscripciones(evento);
            },
            error: (err) => {
                this.marcandoAsistencia = null;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo registrar la falta.'
                });
            }
        });
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
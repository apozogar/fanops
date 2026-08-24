import {Component, OnInit} from '@angular/core';
import {finalize} from 'rxjs';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MessageService, ConfirmationService} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {InputTextModule} from 'primeng/inputtext';
import {ToastModule} from 'primeng/toast';
import {ToolbarModule} from 'primeng/toolbar';
import {DialogModule} from 'primeng/dialog';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {TagModule} from 'primeng/tag';
import {SocioService} from '@/services/SocioService';
import {CheckboxModule} from 'primeng/checkbox';
import {DatePickerModule} from 'primeng/datepicker';
import {Textarea} from 'primeng/textarea';
import {EstadisticasSocio} from "@/interfaces/socio.interface";
import {IconField} from "primeng/iconfield";
import {InputIcon} from "primeng/inputicon";
import {Tooltip} from "primeng/tooltip";
import {
    CuotasSocioTableComponent
} from "@/components/cuotas-socio-table/cuotas-socio-table.component";
import {Role} from "@/interfaces/role.interface";
import {EventoService} from "@/services/evento.service";
import {HistorialEventoSocio, HistorialSocio} from "@/interfaces/evento-inscripcion.dto";
import {GestionCobrosComponent} from "@/components/gestion-cobros/gestion-cobros.component";
import {UiButtonDirective} from "@/ui/ui-button.directive";
import {IconComponent} from "@/ui/icon/icon.component";

@Component({
    selector: 'app-socios',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TableModule,
        InputTextModule,
        ToastModule,
        ToolbarModule,
        DialogModule,
        ConfirmDialogModule,
        TagModule,
        CheckboxModule,
        DatePickerModule,
        Textarea, IconField, InputIcon, Tooltip, CuotasSocioTableComponent, GestionCobrosComponent,
        UiButtonDirective, IconComponent

    ],
    templateUrl: './SociosComponent.html'
})
export class SociosComponent implements OnInit {
    socios: any[] = [];
    socio: any = {};
    socioDialog: boolean = false;
    loading: boolean = false;
    submitted: boolean = false;

    /** Guardado en vuelo: alimenta el indicador del botón y evita un doble envío. */
    guardando = false;

    filtroActivo: string | null = null;
    cobrosDialog: boolean = false;
    cuotasDialog: boolean = false;
    cuotasSocio: any[] = [];

    estadistica?: EstadisticasSocio;

    /** Modal de historial de eventos y faltas del socio. */
    historialDialog: boolean = false;
    historial?: HistorialSocio;
    historialCargando: boolean = false;
    /** Falta con el perdón en vuelo: alimenta el indicador del botón y evita el doble envío. */
    perdonando: string | null = null;

    // Para manejar el checkbox de admin
    isAdmin: boolean = false;
    private adminRole?: Role;
    private userRole?: Role;

    constructor(
        private readonly socioService: SocioService,
        private readonly eventoService: EventoService,
        private readonly messageService: MessageService,
        private readonly confirmationService: ConfirmationService
    ) {
    }

    ngOnInit(): void {
        this.cargarSocios();
        this.obtenerEstatidicas();
        this.cargarRoles();
    }

    cargarSocios(filtro?: string): void {
        this.loading = true;
        this.socioService.getSocios(filtro).subscribe({
            next: (response) => {
                this.filtroActivo = filtro || null;
                this.socios = response.data; // Asumiendo que la respuesta ya viene procesada
                this.loading = false;
            },
            error: () => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Error al cargar los socios'
                });
                this.loading = false;
            }
        });
    }

    limpiarFiltros(): void {
        this.filtroActivo = null;
        this.cargarSocios();
    }

    cargarRoles(): void {
        this.socioService.getRoles().subscribe(response => {
            this.adminRole = response?.data?.find(r => r.name === 'ROLE_ADMIN');
            this.userRole = response?.data?.find(r => r.name === 'ROLE_USER');
        });
    }

    abrirNuevo(): void {
        this.socio = {};
        this.isAdmin = false; // Por defecto, un nuevo usuario no es admin
        this.socioDialog = true;
    }

    editarSocio(socio: any): void {
        this.socio = {...socio};
        // Comprobamos si el socio tiene el rol de admin para marcar el checkbox
        if (this.adminRole && this.socio.usuario?.roles) {
            this.isAdmin = this.socio.usuario.roles.some((role: Role) => role.id === this.adminRole!.id);
        } else {
            this.isAdmin = false;
        }
        this.socioDialog = true;
    }

    guardarSocio(): void {
        this.submitted = true;

        if (this.guardando) {
            return;
        }
        this.guardando = true;

        // Preparamos los roles basados en el checkbox
        const rolesParaGuardar: Role[] = [];
        if (this.isAdmin && this.adminRole) {
            rolesParaGuardar.push(this.adminRole);
        } else if (this.userRole) {
            rolesParaGuardar.push(this.userRole);
        }
        // Los roles viven en la cuenta de usuario, y una ficha nueva todavía no tiene cuenta: se
        // crea cuando la persona se registra con ese correo y confirma el enlace de vinculación.
        // Al crear no se manda "usuario" para no enviar una cuenta que no existe.
        if (this.socio.uid) {
            this.socio.usuario = {...this.socio.usuario, roles: rolesParaGuardar};
        }

        if (this.socio.uid) {
            this.socioService.actualizarSocio(this.socio.uid, this.socio).pipe(finalize(() => (this.guardando = false))).subscribe({
                next: () => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Éxito',
                        detail: 'Socio actualizado'
                    });
                    this.cargarSocios();
                    this.socioDialog = false;
                }
            });
        } else {
            this.socioService.crearSocio(this.socio).pipe(finalize(() => (this.guardando = false))).subscribe({
                next: () => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Éxito',
                        detail: 'Socio creado'
                    });
                    this.cargarSocios();
                    this.socioDialog = false;
                }
            });
        }
    }

    eliminarSocio(socio: any): void {
        this.confirmationService.confirm({
            message: '¿Está seguro que desea eliminar este socio?',
            header: 'Confirmar',
            accept: () => {
                this.socioService.eliminarSocio(socio.uid).subscribe({
                    next: () => {
                        this.cargarSocios();
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Éxito',
                            detail: 'Socio eliminado'
                        });
                    }
                });
            }
        });
    }

    mostrarCuotas(socio: any): void {
        this.socioService.getCuotasSocio(socio.uid).subscribe({
            next: (response) => {
                this.cuotasSocio = response.data;
                this.cuotasDialog = true;
            },
            error: () => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'No se pudieron cargar las cuotas del socio.'
                });
            }
        });
    }


    // ----------------------------------------------------------------
    // Acceso a la aplicación
    // ----------------------------------------------------------------

    /**
     * Texto de la columna "Acceso". Distingue tres situaciones que se confunden con facilidad:
     * no tener cuenta, tenerla bloqueada, y tenerla pero no haber entrado nunca. La última es la
     * interesante para gestión: son los socios a los que hay que echar una mano para entrar.
     */
    accesoTexto(socio: any): string {
        if (!socio.tieneUsuario) return 'Sin cuenta';
        if (!socio.usuarioActivo) return 'Cuenta bloqueada';
        if (!socio.ultimoAcceso) return 'Nunca ha entrado';
        return 'Ha entrado';
    }

    accesoSeverity(socio: any): 'success' | 'warn' | 'danger' | 'secondary' {
        if (!socio.tieneUsuario) return 'secondary';
        if (!socio.usuarioActivo) return 'danger';
        if (!socio.ultimoAcceso) return 'warn';
        return 'success';
    }

    // ----------------------------------------------------------------
    // Faltas e historial de eventos
    // ----------------------------------------------------------------

    /** Faltas del socio, señalando entre paréntesis las que todavía le penalizan. */
    faltasTexto(socio: any): string {
        const total = socio.faltasAcumuladas ?? 0;
        const pendientes = socio.faltasPendientes ?? 0;
        return pendientes > 0 ? `${total} (${pendientes} activa${pendientes > 1 ? 's' : ''})` : `${total}`;
    }

    abrirHistorial(socio: any): void {
        this.historial = undefined;
        this.historialCargando = true;
        this.historialDialog = true;
        this.eventoService.getHistorialSocio(socio.uid).subscribe({
            next: (response) => {
                this.historial = response.data;
                this.historialCargando = false;
            },
            error: (err) => {
                this.historialCargando = false;
                this.historialDialog = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo cargar el historial del socio.'
                });
            }
        });
    }

    /**
     * Perdona la falta de un evento. Además de dejar de penalizar, devuelve la asistencia a
     * pendiente, para que no quede marcado como ausente alguien a quien se le ha perdonado.
     */
    perdonarFalta(fila: HistorialEventoSocio): void {
        if (!fila.faltaUid || this.perdonando) {
            return;
        }
        const faltaUid = fila.faltaUid;
        this.confirmationService.confirm({
            message: `¿Perdonar la falta de "${fila.nombreEvento}"? Dejará de penalizarle en sus próximas inscripciones.`,
            header: 'Perdonar falta',
            accept: () => {
                this.perdonando = faltaUid;
                this.eventoService.quitarFalta(faltaUid).pipe(finalize(() => (this.perdonando = null))).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Falta perdonada',
                            detail: fila.nombreEvento
                        });
                        // El listado muestra el contador de faltas, así que hay que refrescarlo
                        // junto con el propio historial.
                        if (this.historial) {
                            this.abrirHistorial({uid: this.historial.socioUid});
                        }
                        this.cargarSocios(this.filtroActivo ?? undefined);
                    },
                    error: (err) => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: err.error?.message || 'No se pudo perdonar la falta.'
                        });
                    }
                });
            }
        });
    }

    /** Estado de la falta de un evento: si todavía castiga y con cuántas inscripciones. */
    faltaTexto(fila: HistorialEventoSocio): string {
        return fila.penalizacionesRestantes > 0 ? `Penaliza (${fila.penalizacionesRestantes})` : 'Ya cumplida';
    }

    /** Cómo le fue al socio en ese evento, en una sola etiqueta. */
    resultadoEventoTexto(fila: HistorialEventoSocio): string {
        if (fila.motivoFalta === 'CANCELACION_TARDIA') return 'Anuló fuera de plazo';
        if (fila.asistencia === 'NO_ASISTIO') return 'No se presentó';
        if (fila.asistencia === 'ASISTIO') return 'Asistió';
        if (fila.estado === 'EN_ESPERA') return 'En lista de espera';
        if (fila.estado === 'CONFIRMADA') return 'Con plaza, sin pasar lista';
        return '—';
    }

    resultadoEventoSeverity(fila: HistorialEventoSocio): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
        if (fila.motivoFalta === 'CANCELACION_TARDIA' || fila.asistencia === 'NO_ASISTIO') return 'danger';
        if (fila.asistencia === 'ASISTIO') return 'success';
        if (fila.estado === 'EN_ESPERA') return 'warn';
        if (fila.estado === 'CONFIRMADA') return 'info';
        return 'secondary';
    }

    obtenerEstatidicas(): void {
        this.socioService.obtenerEstadisticas().subscribe((data) => {
            this.estadistica = data.data;
        });
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            const file = input.files[0];
            this.subirFichero(file);
        }
    }

    private subirFichero(file: File): void {
        this.loading = true; // Mostramos el spinner de la tabla
        this.socioService.importarSocios(file).subscribe({
            next: (response) => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Éxito',
                    detail: response.message
                });
                this.cargarSocios(); // Recargamos la lista de socios para ver los nuevos
            },
            error: (err) => {
                this.loading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Hubo un error al importar el fichero.' + err.message
                });
            }
        });
    }
}

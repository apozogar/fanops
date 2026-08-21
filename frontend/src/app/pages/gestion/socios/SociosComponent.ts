import {Component, OnInit} from '@angular/core';
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

    filtroActivo: string | null = null;
    cobrosDialog: boolean = false;
    cuotasDialog: boolean = false;
    cuotasSocio: any[] = [];

    estadistica?: EstadisticasSocio;

    // Para manejar el checkbox de admin
    isAdmin: boolean = false;
    private adminRole?: Role;
    private userRole?: Role;

    constructor(
        private readonly socioService: SocioService,
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

        // Preparamos los roles basados en el checkbox
        const rolesParaGuardar: Role[] = [];
        if (this.isAdmin && this.adminRole) {
            rolesParaGuardar.push(this.adminRole);
        } else if (this.userRole) {
            rolesParaGuardar.push(this.userRole);
        }
        this.socio.usuario = {...this.socio.usuario, roles: rolesParaGuardar};

        if (this.socio.uid) {
            this.socioService.actualizarSocio(this.socio.uid, this.socio).subscribe({
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
            this.socioService.crearSocio(this.socio).subscribe({
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

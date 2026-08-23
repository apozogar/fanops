import {Component, OnInit} from '@angular/core';
import {finalize} from 'rxjs';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MessageService, ConfirmationService} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {InputTextModule} from 'primeng/inputtext';
import {InputNumberModule} from 'primeng/inputnumber';
import {ToastModule} from 'primeng/toast';
import {ToolbarModule} from 'primeng/toolbar';
import {DialogModule} from 'primeng/dialog';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {FileUploadModule} from 'primeng/fileupload';
import {Tooltip} from 'primeng/tooltip';
import {PenaService} from '@/services/pena.service';
import {ActivePenaService} from '@/core/pena/active-pena.service';
import {Pena, PenaRequest} from '@/interfaces/socio.interface';

import { UiButtonDirective } from '@/ui/ui-button.directive';
import { IconComponent } from '@/ui/icon/icon.component';
@Component({
    selector: 'app-penas',
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
        FileUploadModule,
        Tooltip
    ],
    templateUrl: './PenasComponent.html'
})
export class PenasComponent implements OnInit {
    /** Formatos e imagen máxima admitidos para el logo (el backend valida lo mismo). */
    readonly formatosLogo = 'image/png,image/jpeg,image/webp,image/gif,image/svg+xml';
    readonly tamanoMaximoLogo = 1024 * 1024; // 1 MB

    penas: Pena[] = [];
    pena: PenaRequest & { id?: number } = this.penaVacia();
    penaDialog: boolean = false;
    loading: boolean = false;
    submitted: boolean = false;

    /** Guardado en vuelo: alimenta el indicador del botón y evita un doble envío. */
    guardando = false;

    constructor(
        private readonly penaService: PenaService,
        private readonly messageService: MessageService,
        private readonly confirmationService: ConfirmationService,
        private readonly activePena: ActivePenaService
    ) {
    }

    ngOnInit(): void {
        this.cargarPenas();
    }

    cargarPenas(): void {
        this.loading = true;
        this.penaService.listAll().subscribe({
            next: (response) => {
                this.penas = response.data ?? [];
                this.loading = false;
            },
            error: () => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'No se pudieron cargar las peñas.'
                });
                this.loading = false;
            }
        });
    }

    abrirNueva(): void {
        this.pena = this.penaVacia();
        this.submitted = false;
        this.penaDialog = true;
    }

    editarPena(pena: Pena): void {
        this.pena = {...pena};
        this.submitted = false;
        this.penaDialog = true;
    }

    guardarPena(): void {
        this.submitted = true;
        if (!this.pena.nombre || this.guardando) {
            return;
        }

        this.guardando = true;

        const {id, ...datos} = this.pena;

        if (id) {
            this.penaService.actualizar(id, datos).pipe(finalize(() => (this.guardando = false))).subscribe({
                next: (response) => {
                    this.messageService.add({severity: 'success', summary: 'Éxito', detail: 'Peña actualizada'});
                    this.activePena.actualizada(response.data);
                    this.cargarPenas();
                    this.penaDialog = false;
                },
                error: (err) => this.mostrarError(err, 'No se pudo actualizar la peña.')
            });
        } else {
            this.penaService.crear(datos).pipe(finalize(() => (this.guardando = false))).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Éxito', detail: 'Peña creada'});
                    this.cargarPenas();
                    this.penaDialog = false;
                },
                error: (err) => this.mostrarError(err, 'No se pudo crear la peña.')
            });
        }
    }

    /**
     * Lee la imagen seleccionada y la deja en el formulario codificada en base64 (data URI), de
     * modo que el logo se guarda en la BD y no depende de una URL externa que pueda cambiar.
     */
    seleccionarLogo(event: any): void {
        const file: File = event?.files?.[0];
        if (!file) {
            return;
        }
        if (file.size > this.tamanoMaximoLogo) {
            this.messageService.add({
                severity: 'warn',
                summary: 'Imagen demasiado grande',
                detail: 'El logo no puede superar 1 MB.'
            });
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            this.pena.logo = reader.result as string;
        };
        reader.onerror = () => {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'No se ha podido leer la imagen seleccionada.'
            });
        };
        reader.readAsDataURL(file);
    }

    quitarLogo(): void {
        this.pena.logo = '';
    }

    eliminarPena(pena: Pena): void {
        this.confirmationService.confirm({
            message: `¿Seguro que quieres eliminar la peña "${pena.nombre}"? Esta acción no se puede deshacer.`,
            header: 'Confirmar eliminación',
            accept: () => {
                this.penaService.eliminar(pena.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Éxito', detail: 'Peña eliminada'});
                        this.cargarPenas();
                    },
                    error: (err) => this.mostrarError(err, 'No se pudo eliminar la peña.')
                });
            }
        });
    }

    private mostrarError(err: any, fallback: string): void {
        const detail = err?.error?.message || fallback;
        this.messageService.add({severity: 'error', summary: 'Error', detail});
    }

    private penaVacia(): PenaRequest & { id?: number } {
        return {
            nombre: '',
            iniciadorId: '',
            direccion1: '',
            direccion2: '',
            cuentaIban: '',
            cuentaBic: '',
            cuotaAdulto: undefined,
            cuotaMenor: undefined,
            edadMayoria: undefined,
            edadJubilacion: undefined,
            logo: '',
            lema: '',
            color: ''
        };
    }
}

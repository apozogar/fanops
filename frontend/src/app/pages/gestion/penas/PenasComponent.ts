import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MessageService, ConfirmationService} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {InputNumberModule} from 'primeng/inputnumber';
import {ToastModule} from 'primeng/toast';
import {ToolbarModule} from 'primeng/toolbar';
import {DialogModule} from 'primeng/dialog';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {Ripple} from 'primeng/ripple';
import {Tooltip} from 'primeng/tooltip';
import {PenaService} from '@/services/pena.service';
import {Pena, PenaRequest} from '@/interfaces/socio.interface';

@Component({
    selector: 'app-penas',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TableModule,
        ButtonModule,
        InputTextModule,
        InputNumberModule,
        ToastModule,
        ToolbarModule,
        DialogModule,
        ConfirmDialogModule,
        Ripple,
        Tooltip
    ],
    templateUrl: './PenasComponent.html'
})
export class PenasComponent implements OnInit {
    penas: Pena[] = [];
    pena: PenaRequest & { id?: number } = this.penaVacia();
    penaDialog: boolean = false;
    loading: boolean = false;
    submitted: boolean = false;

    constructor(
        private readonly penaService: PenaService,
        private readonly messageService: MessageService,
        private readonly confirmationService: ConfirmationService
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
        if (!this.pena.nombre) {
            return;
        }

        const {id, ...datos} = this.pena;

        if (id) {
            this.penaService.actualizar(id, datos).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Éxito', detail: 'Peña actualizada'});
                    this.cargarPenas();
                    this.penaDialog = false;
                },
                error: (err) => this.mostrarError(err, 'No se pudo actualizar la peña.')
            });
        } else {
            this.penaService.crear(datos).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Éxito', detail: 'Peña creada'});
                    this.cargarPenas();
                    this.penaDialog = false;
                },
                error: (err) => this.mostrarError(err, 'No se pudo crear la peña.')
            });
        }
    }

    eliminarPena(pena: Pena): void {
        this.confirmationService.confirm({
            message: `¿Seguro que quieres eliminar la peña "${pena.nombre}"? Esta acción no se puede deshacer.`,
            header: 'Confirmar eliminación',
            icon: 'pi pi-exclamation-triangle',
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

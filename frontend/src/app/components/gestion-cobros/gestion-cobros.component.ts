import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { SocioService } from '@/services/SocioService';
import { Select } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';

@Component({
    selector: 'app-gestion-cobros',
    standalone: true,
    imports: [DialogModule, ButtonModule, FileUploadModule, ToastModule, Select, FormsModule, InputTextModule, DatePickerModule],
    templateUrl: './gestion-cobros.component.html',
    providers: [MessageService] // Proveedor local para no interferir con otros toasts
})
export class GestionCobrosComponent {
    @Input() visible: boolean = false;
    @Output() visibleChange = new EventEmitter<boolean>();

    loadingGenerar: boolean = false;
    loadingSubir: boolean = false;
    loadingConfirmar: boolean = false;

    concepto: string = '';
    fechaCobro: Date = new Date();
    // Opciones para el formato de la remesa
    formatosRemesa = [
        { label: 'XML (SEPA)', value: 'xml' },
        { label: 'Excel', value: 'excel' }
    ];
    formatoSeleccionado: 'xml' | 'excel' = 'xml';

    // Opciones para el tipo de proceso a realizar
    tiposProceso = [
        { label: 'Proceso Completo SEPA', value: 'sepa' },
        {
            label: 'Generar solo Excel',
            value: 'excel'
        }
    ];
    tipoProceso: 'sepa' | 'excel' = 'sepa';

    constructor(
        private socioService: SocioService,
        private messageService: MessageService
    ) {}

    generarYDescargarRemesa() {
        this.loadingGenerar = true;
        if (!this.concepto || !this.fechaCobro) {
            this.messageService.add({
                severity: 'warn',
                summary: 'Aviso',
                detail: 'Por favor, introduce un concepto y una fecha de cobro.'
            });
            this.loadingGenerar = false;
            return;
        }

        // Formateamos la fecha a YYYY-MM-DD para enviarla al backend
        const fechaCobroStr = this.fechaCobro.toISOString().split('T')[0];

        // Si el tipo de proceso es 'excel', forzamos el formato a 'excel'.
        const formatoFinal = this.tipoProceso === 'excel' ? 'excel' : this.formatoSeleccionado;

        const peticion = formatoFinal === 'xml' ? this.socioService.generarRemesaMensual(this.concepto, fechaCobroStr) : this.socioService.generarRemesaExcel(this.concepto, fechaCobroStr);

        peticion.subscribe({
            next: (response) => {
                if (formatoFinal === 'xml' && response) {
                    this.descargarFichero(response, `remesa-sepa-${this.getFormattedDate()}.xml`, 'application/xml');
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Éxito',
                        detail: 'Remesa Excel generada correctamente.'
                    });
                } else if (formatoFinal === 'excel' && response instanceof Blob && response.size > 0) {
                    this.descargarFichero(response, `remesa-excel-${this.getFormattedDate()}.xlsx`, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Éxito',
                        detail: 'Remesa Excel generada correctamente.'
                    });
                } else {
                    this.messageService.add({
                        severity: 'warn',
                        summary: 'Aviso',
                        detail: 'No se generaron nuevas cuotas para la remesa.'
                    });
                }
                this.loadingGenerar = false;
            },
            error: (err) => {
                const detail = err.error?.message || 'No se pudo generar la remesa.';
                this.messageService.add({ severity: 'error', summary: 'Error', detail: detail });
                this.loadingGenerar = false;
            }
        });
    }

    subirFicheroRetorno(event: any) {
        const file: File = event.files[0];
        if (!file) return;

        this.loadingSubir = true;
        this.socioService.procesarRetorno(file).subscribe({
            next: (response) => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Éxito',
                    detail: response.message
                });
                this.loadingSubir = false;
                this.cerrarDialogo(); // Cerramos al finalizar
            },
            error: (err) => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Error al procesar el fichero de retorno.'
                });
                this.loadingSubir = false;
            }
        });
    }

    confirmarPagosPendientes() {
        this.loadingConfirmar = true;
        this.socioService.confirmarPagos().subscribe({
            next: (response) => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Éxito',
                    detail: response.message
                });
                this.loadingConfirmar = false;
                this.cerrarDialogo();
            },
            error: (err) => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Error al confirmar los pagos.'
                });
                this.loadingConfirmar = false;
            }
        });
    }

    private descargarFichero(data: BlobPart, nombreFichero: string, tipo: string) {
        const blob = data instanceof Blob ? data : new Blob([data], { type: tipo });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nombreFichero;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }

    private getFormattedDate(): string {
        return new Date().toISOString().slice(0, 10);
    }

    cerrarDialogo() {
        this.visible = false;
        this.visibleChange.emit(this.visible);
    }
}

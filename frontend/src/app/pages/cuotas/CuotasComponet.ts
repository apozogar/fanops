// src/app/components/cuotas/cuotas.component.ts
import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MessageService} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {InputNumberModule} from 'primeng/inputnumber';
// import { CalendarModule } from 'primeng/calendar';
import {SelectModule} from 'primeng/select';
import {ToastModule} from 'primeng/toast';
import {DialogModule} from 'primeng/dialog';
import {TagModule} from 'primeng/tag';
import {CardModule} from 'primeng/card';
import {Ripple} from "primeng/ripple";
import {Toolbar} from "primeng/toolbar";

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
@Component({
    selector: 'app-cuotas',
    standalone: true,
    imports: [UiButtonDirective, IconComponent, 
        CommonModule,
        FormsModule,
        TableModule,
        InputNumberModule,
        // CalendarModule,
        SelectModule,
        ToastModule,
        DialogModule,
        TagModule,
        CardModule,
        Toolbar
    ],
    templateUrl: './CuotasComponent.html'
})
export class CuotasComponet implements OnInit {
    cuotas: any[] = [];
    loading: boolean = false;

    constructor(private messageService: MessageService) {
    }

    ngOnInit() {
        this.cargarCuotas();
    }

    cargarCuotas() {
        this.loading = true;
        // Implementar carga de cuotas
        this.loading = false;
    }

    getEstadoSeverity(estado: string) {
        switch (estado) {
            case 'PAGADA':
                return 'success';
            case 'PENDIENTE':
                return 'warning';
            case 'VENCIDA':
                return 'danger';
            default:
                return 'info';
        }
    }

    marcarComoPagada(cuota: any) {
        // Implementar marcar como pagada
        this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Cuota marcada como pagada'
        });
    }

    editarCuota(cuota: any) {
        // Implementar edición de cuota
    }

    abrirNuevo(): void {
        // this.socio = {};
        // this.socioDialog = true;
    }
}

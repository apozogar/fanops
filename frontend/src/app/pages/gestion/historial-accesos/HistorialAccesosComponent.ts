import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MessageService} from 'primeng/api';
import {TableModule} from 'primeng/table';
import {ToastModule} from 'primeng/toast';
import {ToolbarModule} from 'primeng/toolbar';
import {TagModule} from 'primeng/tag';
import {Tooltip} from 'primeng/tooltip';
import {HistorialAccesoService} from '@/services/historial-acceso.service';
import {HistorialAcceso} from '@/interfaces/historial-acceso.interface';

import {UiButtonDirective} from '@/ui/ui-button.directive';
import {IconComponent} from '@/ui/icon/icon.component';

@Component({
    selector: 'app-historial-accesos',
    standalone: true,
    imports: [UiButtonDirective, IconComponent,
        CommonModule,
        TableModule,
        ToastModule,
        ToolbarModule,
        TagModule,
        Tooltip
    ],
    templateUrl: './HistorialAccesosComponent.html'
})
export class HistorialAccesosComponent implements OnInit {
    accesos: HistorialAcceso[] = [];
    loading = false;

    constructor(
        private readonly historialAccesoService: HistorialAccesoService,
        private readonly messageService: MessageService
    ) {
    }

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {
        this.loading = true;
        this.historialAccesoService.listar().subscribe({
            next: (response) => {
                this.accesos = response.data ?? [];
                this.loading = false;
            },
            error: () => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'No se pudo cargar el historial de accesos.'
                });
                this.loading = false;
            }
        });
    }
}

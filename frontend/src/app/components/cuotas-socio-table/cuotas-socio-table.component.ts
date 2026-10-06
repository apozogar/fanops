import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TableModule} from 'primeng/table';
import {BadgeModule} from 'primeng/badge';
import {CardModule} from "primeng/card";
import {Cuota} from "@/interfaces/cuota.interface";
import {TagModule} from 'primeng/tag';
import {IconComponent} from '@/ui/icon/icon.component';

@Component({
  selector: 'app-cuotas-socio-table',
  standalone: true,
  imports: [CommonModule, TableModule, BadgeModule, CardModule, TagModule, IconComponent],
  templateUrl: './cuotas-socio-table.component.html',
})
export class CuotasSocioTableComponent {
  @Input() cuotas: Cuota[] = [];

  getSeverity(estado: string): 'success' | 'danger' | 'warn' {
    switch (estado) {
      case 'PAGADA':
        return 'success';
      case 'PENDIENTE':
        return 'warn';
      default:
        return 'danger';
    }
  }
}

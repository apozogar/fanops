import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EventoInscripcionDTO} from "@/interfaces/evento-inscripcion.dto";
import {EventoService} from '@/services/evento.service';
import {MessageService} from 'primeng/api';
import {CardModule} from 'primeng/card';
import {ButtonModule} from 'primeng/button';
import {ToastModule} from 'primeng/toast';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {TagModule} from 'primeng/tag';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiTagComponent } from '@/ui/ui-tag.component';
@Component({
    selector: 'app-inscripcion-eventos',
    standalone: true,
    imports: [UiButtonDirective, UiTagComponent, IconComponent, CommonModule, CardModule, ButtonModule, ToastModule, ProgressSpinnerModule, TagModule],
    templateUrl: './inscripcion-eventos.component.html',
    styleUrls: ['./inscripcion-eventos.component.scss'],
    providers: [MessageService]
})
export class InscripcionEventosComponent implements OnInit {
    eventos: EventoInscripcionDTO[] = [];
    loading = true;

    private eventoService = inject(EventoService);
    private messageService = inject(MessageService);

    ngOnInit(): void {
        this.eventoService.getEventosParaInscripcion().subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.eventos = response.data;
                }
                this.loading = false;
            },
            error: () => {
                this.loading = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'No se pudieron cargar los eventos.'
                });
            }
        });
    }

    inscribir(evento: EventoInscripcionDTO) {
        if (!evento.uid) return;

        this.eventoService.inscribir(evento.uid).subscribe({
            next: (resp) => {
                evento.isCurrentUserInscrito = true;
                evento.estadoInscripcionActual = resp.data;
                this.messageService.add({
                    severity: resp.data === 'EN_ESPERA' ? 'warn' : 'success',
                    summary: resp.data === 'EN_ESPERA' ? 'Lista de espera' : '¡Inscrito!',
                    detail: resp.message || `Te has inscrito a ${evento.nombreEvento}`
                });
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudo realizar la inscripción.'
            })
        });
    }

    anularInscripcion(evento: EventoInscripcionDTO) {
        if (!evento.uid) return;

        this.eventoService.anularInscripcion(evento.uid).subscribe({
            next: () => {
                evento.isCurrentUserInscrito = false;
                evento.estadoInscripcionActual = null;
                this.messageService.add({
                    severity: 'info',
                    summary: 'Anulado',
                    detail: `Has anulado tu inscripción a ${evento.nombreEvento}`
                });
            },
            error: (err) => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudo anular la inscripción.'
            })
        });
    }

    plazasTexto(evento: EventoInscripcionDTO): string {
        if (evento.plazasLibres < 0) {
            return 'Plazas ilimitadas';
        }
        return `${evento.plazasLibres} plazas libres de ${evento.plazasOcupadas + evento.plazasLibres}`;
    }

    enEspera(evento: EventoInscripcionDTO): boolean {
        return evento.isCurrentUserInscrito && evento.estadoInscripcionActual === 'EN_ESPERA';
    }
}
import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, RouterModule} from '@angular/router';
import {MessageService} from 'primeng/api';
import {ToastModule} from 'primeng/toast';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {CardModule} from 'primeng/card';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {TagModule} from 'primeng/tag';
import {EventoInscripcionDTO} from '@/interfaces/evento-inscripcion.dto';
import {EventoService} from '@/services/evento.service';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiTagComponent } from '@/ui/ui-tag.component';
@Component({
    selector: 'app-inscripcion-publica',
    standalone: true,
    imports: [UiButtonDirective, UiTagComponent, IconComponent, CommonModule, FormsModule, RouterModule, ToastModule, ButtonModule, InputTextModule,
        CardModule, ProgressSpinnerModule, TagModule],
    templateUrl: './inscripcion-publica.component.html',
    providers: [MessageService]
})
export class InscripcionPublicaComponent implements OnInit {
    evento: EventoInscripcionDTO | null = null;
    loading = true;
    enviando = false;
    enviado = false;
    errorCarga = false;

    nombre = '';
    email = '';
    telefono = '';

    private route = inject(ActivatedRoute);
    private eventoService = inject(EventoService);
    private messageService = inject(MessageService);

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (!id) {
            this.errorCarga = true;
            this.loading = false;
            return;
        }
        this.eventoService.infoEventoPublico(id).subscribe({
            next: (response) => {
                this.evento = response.success ? response.data : null;
                if (response.data?.inscripcionCerrada) {
                    this.messageService.add({
                        severity: 'warn',
                        summary: 'Plazo cerrado',
                        detail: 'El plazo de inscripción para este evento ya ha finalizado.'
                    });
                }
                this.loading = false;
            },
            error: () => {
                this.errorCarga = true;
                this.loading = false;
            }
        });
    }

    get formularioValido(): boolean {
        return !!this.nombre.trim() && !!this.email.trim() && /^\S+@\S+\.\S+$/.test(this.email.trim());
    }

    fechaLimiteTexto(): string {
        if (!this.evento?.fechaLimiteInscripcion) return 'Inscripción abierta';
        const f = new Date(this.evento.fechaLimiteInscripcion);
        return 'Inscripción abierta hasta el ' + f.toLocaleString('es-ES', {
            day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    }

    inscribirse(): void {
        if (!this.evento || !this.formularioValido) return;
        this.enviando = true;
        this.eventoService.inscribirPublico(this.evento.uid, {
            nombre: this.nombre.trim(),
            email: this.email.trim(),
            telefono: this.telefono.trim() || undefined
        }).subscribe({
            next: (resp) => {
                this.enviado = true;
                this.enviando = false;
                this.messageService.add({
                    severity: 'success',
                    summary: '¡Apuntado!',
                    detail: resp.message || 'Te hemos apuntado a la lista de espera.'
                });
            },
            error: (err) => {
                this.enviando = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err.error?.message || 'No se pudo completar la inscripción.'
                });
            }
        });
    }
}
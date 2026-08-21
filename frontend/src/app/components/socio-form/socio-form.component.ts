import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';

import { Socio } from '@/interfaces/socio.interface';
import { FormsModule, NgForm } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { ButtonModule } from 'primeng/button';

@Component({
    selector: 'app-socio-form',
    standalone: true,
    imports: [FormsModule, InputTextModule, DatePickerModule, ButtonModule],
    templateUrl: './socio-form.component.html'
})
export class SocioFormComponent {
    @Input() socio: Partial<Socio> = {};
    @Output() save = new EventEmitter<Partial<Socio>>();

    @ViewChild('socioForm') socioForm!: NgForm;

    onSave(): void {
        if (this.socioForm.invalid) {
            // Marcar todos los campos como "touched" para mostrar errores
            Object.values(this.socioForm.controls).forEach((control) => {
                control.markAsDirty();
            });
            return;
        }
        this.save.emit(this.socio);
    }

    // Método para que el padre pueda acceder al estado del formulario
    isFormInvalid(): any {
        return this.socioForm.invalid;
    }
}

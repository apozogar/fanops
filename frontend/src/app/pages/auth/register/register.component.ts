import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { RegisterRequest } from '@/models/register-request.model';
import { FormsModule } from '@angular/forms';

import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { AuthService } from '@/pages/auth/auth.service';
import { ThemeToggleComponent } from '@/ui/theme-toggle.component';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
@Component({
    selector: 'app-register',
    standalone: true,
    imports: [UiButtonDirective, IconComponent, RouterLink, FormsModule, ButtonModule, CheckboxModule, InputTextModule, PasswordModule, MessageModule, ThemeToggleComponent],
    templateUrl: './register.component.html',
    styles: ``
})
export class RegisterComponent implements OnInit {
    registerData: RegisterRequest = {
        nombre: '',
        email: '',
        password: ''
    };
    confirmPassword = '';
    error: string | null = null;
    /**
     * true cuando el email ya figuraba en el listado de socios de la peña: no se ha creado ninguna
     * ficha nueva y hay que confirmar la vinculación desde el enlace enviado a ese correo.
     */
    verificacionEnviada = false;

    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit(): void {
        localStorage.removeItem('token');
    }

    register(): void {
        this.error = null;

        if (!this.registerData.nombre || !this.registerData.email || !this.registerData.password) {
            this.error = 'Todos los campos son obligatorios.';
            return;
        }

        if (this.registerData.password !== this.confirmPassword) {
            this.error = 'Las contraseñas no coinciden.';
            return;
        }

        this.authService.loginAfterRegister(this.registerData).subscribe({
            next: (resultado) => {
                if (resultado.requiereVerificacion) {
                    // Ese correo ya está en el listado de socios: la cuenta se crea al confirmar
                    // el enlace que se acaba de enviar, no aquí.
                    this.verificacionEnviada = true;
                    return;
                }
                // Registro y login exitosos. Redirigimos a completar el perfil.
                this.router.navigate(['/auth/complete-profile']);
            },
            error: (err) => {
                // Manejo de errores del backend
                if (err.status === 409) {
                    // Conflict
                    this.error = 'El email ya está registrado.';
                } else {
                    this.error = 'Ocurrió un error durante el registro. Por favor, inténtalo de nuevo.';
                }
                console.error(err);
            }
        });
    }
}

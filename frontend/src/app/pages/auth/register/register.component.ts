import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { RegisterRequest } from '@/models/register-request.model';
import { FormsModule } from '@angular/forms';
import { AuthService } from '@/pages/auth/auth.service';
import { AuthShellComponent } from '@/pages/auth/auth-shell.component';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiInputDirective } from '@/ui/ui-input.directive';
import { UiPasswordComponent } from '@/ui/ui-password.component';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, UiInputDirective, UiPasswordComponent, IconComponent, RouterLink, FormsModule],
    templateUrl: './register.component.html'
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

    protected readonly penaPublica = inject(PenaPublicaService);

    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit(): void {
        localStorage.removeItem('token');
    }

    /**
     * Aviso en el propio campo de confirmación, en lugar de esperar al envío. Solo avisa cuando ya
     * hay algo escrito en la confirmación: si no, marcaría en rojo un campo que aún no se ha tocado.
     */
    passwordsNoCoinciden(): boolean {
        return !!this.confirmPassword && this.confirmPassword !== this.registerData.password;
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

        // El dominio por el que se ha entrado decide la peña del socio nuevo. Sin él, el backend
        // cae en la peña por defecto, que es lo que hacía antes con todo el mundo.
        this.authService.loginAfterRegister({ ...this.registerData, penaSlug: this.penaPublica.slug() }).subscribe({
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
                if (err.status === 409) {
                    this.error = 'El email ya está registrado.';
                } else {
                    // Cuando el backend explica el motivo (por ejemplo, que no se ha podido
                    // enviar el correo de vinculación) se muestra tal cual.
                    this.error = err?.error?.message ?? 'Ocurrió un error durante el registro. Por favor, inténtalo de nuevo.';
                }
                console.error(err);
            }
        });
    }
}

import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { AuthService } from './auth.service';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [ButtonModule, InputTextModule, PasswordModule, FormsModule, RouterModule, RippleModule, AppFloatingConfigurator],
    template: `
        <app-floating-configurator />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-screen overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                        <div class="text-center mb-8">
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Restablecer Contraseña</div>
                            <span class="text-muted-color font-medium">Introduce tu nueva contraseña</span>
                        </div>

                        @if (!submitted) {
                            <div>
                                <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Nueva Contraseña</label>
                                <p-password id="password" [(ngModel)]="password" placeholder="Nueva Contraseña" [toggleMask]="true" styleClass="mb-4" [fluid]="true"></p-password>
                                <label for="confirmPassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Confirmar Contraseña</label>
                                <p-password id="confirmPassword" [(ngModel)]="confirmPassword" placeholder="Confirmar Contraseña" [toggleMask]="true" styleClass="mb-4" [fluid]="true"></p-password>
                                @if (error) {
                                    <div class="p-error text-center mb-4">{{ error }}</div>
                                }
                                <p-button label="Restablecer" styleClass="w-full" (click)="resetPassword()"></p-button>
                            </div>
                        }

                        @if (submitted) {
                            <div class="text-center">
                                <i class="pi pi-check-circle text-primary" style="font-size: 3rem"></i>
                                <h2 class="mt-4">¡Contraseña actualizada!</h2>
                                <p>Tu contraseña ha sido actualizada correctamente. Ahora puedes iniciar sesión con tu nueva contraseña.</p>
                                <p-button label="Ir a Iniciar Sesión" styleClass="w-full mt-4" routerLink="/auth/login"></p-button>
                            </div>
                        }
                    </div>
                </div>
            </div>
        </div>
    `
})
export class ResetPassword implements OnInit {
    password: string = '';
    confirmPassword: string = '';
    error: string | null = null;
    submitted: boolean = false;
    token: string | null = null;

    private authService = inject(AuthService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    ngOnInit(): void {
        this.route.queryParams.subscribe((params) => {
            this.token = params['token'];
            if (!this.token) {
                this.error = 'Token no válido o caducado.';
            }
        });
    }

    resetPassword(): void {
        this.error = null;
        if (!this.password || !this.confirmPassword) {
            this.error = 'Por favor, introduce y confirma tu nueva contraseña.';
            return;
        }
        if (this.password !== this.confirmPassword) {
            this.error = 'Las contraseñas no coinciden.';
            return;
        }
        if (this.token) {
            this.authService.resetPassword(this.token, this.password).subscribe({
                next: () => {
                    this.submitted = true;
                },
                error: (err) => {
                    console.error(err);
                    this.error = 'El enlace ha caducado o no es válido. Por favor, solicita uno nuevo.';
                }
            });
        } else {
            this.error = 'Token no válido o caducado.';
        }
    }
}

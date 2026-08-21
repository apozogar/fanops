import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { RippleModule } from 'primeng/ripple';
import { ThemeToggleComponent } from '@/ui/theme-toggle.component';
import { AuthService } from './auth.service';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [UiButtonDirective, IconComponent, ButtonModule, InputTextModule, FormsModule, RouterModule, RippleModule, ThemeToggleComponent],
    template: `
        <fo-theme-toggle [floating]="true" />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-screen overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                        <div class="text-center mb-8">
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">¿Olvidaste tu contraseña?</div>
                            <span class="text-muted-color font-medium">Introduce tu email para recibir un enlace de recuperación</span>
                        </div>

                        @if (!submitted) {
                            <div>
                                <label for="email1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">Email</label>
                                <input pInputText id="email1" type="text" placeholder="Dirección de email" class="w-full md:w-120 mb-8" [(ngModel)]="email" />
                                @if (error) {
                                    <div class="p-error text-center mb-4">{{ error }}</div>
                                }
                                <button foButton variant="primary" class="w-full" (click)="sendLink()">Enviar enlace</button>
                                <button foButton variant="secondary" class="w-full mt-4" routerLink="/auth/login">Volver a Iniciar Sesión</button>
                            </div>
                        }

                        @if (submitted) {
                            <div class="text-center">
                                <fo-icon name="check-circulo" class="text-primary" style="font-size: 3rem" />
                                <h2 class="mt-4">¡Enlace enviado!</h2>
                                <p>Si existe una cuenta con el email proporcionado, recibirás un correo con las instrucciones para recuperar tu contraseña.</p>
                                <button foButton variant="primary" class="w-full mt-4" routerLink="/auth/login">Volver a Iniciar Sesión</button>
                            </div>
                        }
                    </div>
                </div>
            </div>
        </div>
    `
})
export class ForgotPassword {
    email: string = '';
    error: string | null = null;
    submitted: boolean = false;

    private authService = inject(AuthService);

    sendLink(): void {
        this.error = null;
        if (this.email) {
            this.authService.forgotPassword(this.email).subscribe({
                next: () => {
                    this.submitted = true;
                },
                error: (err) => {
                    console.error(err);
                    // El backend explica el motivo cuando lo sabe (por ejemplo, que el proveedor
                    // de correo ha rechazado el envío), y es más útil que un mensaje genérico.
                    this.error = err?.error?.message ?? 'Ha ocurrido un error. Por favor, inténtalo de nuevo más tarde.';
                }
            });
        } else {
            this.error = 'El email es obligatorio.';
        }
    }
}

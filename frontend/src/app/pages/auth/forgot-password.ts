import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthShellComponent } from './auth-shell.component';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiInputDirective } from '@/ui/ui-input.directive';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { AuthService } from './auth.service';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, UiInputDirective, IconComponent, FormsModule, RouterModule],
    template: `
        @if (submitted) {
            <fo-auth-shell icon="correo" title="Enlace enviado" subtitle="Ya puedes cerrar esta pantalla.">
                <p class="text-sm leading-relaxed text-ink-muted">
                    Si existe una cuenta con <strong class="font-medium text-ink">{{ email }}</strong
                    >, recibirás un correo con las instrucciones para elegir una contraseña nueva.
                </p>
                <a foButton variant="primary" size="lg" class="mt-6 w-full" [routerLink]="penaPublica.ruta('auth', 'login')">Volver a iniciar sesión</a>
            </fo-auth-shell>
        } @else {
            <fo-auth-shell icon="llave" title="Recuperar contraseña" subtitle="Escribe tu email y te enviamos un enlace para elegir una nueva.">
                <form (ngSubmit)="sendLink()">
                    <div>
                        <label for="email" class="mb-1.5 block text-sm font-medium text-ink">Email</label>
                        <input foInput id="email" name="email" type="email" inputmode="email" autocomplete="email" autocapitalize="none" spellcheck="false" placeholder="tu@email.com" [invalid]="!!error" [(ngModel)]="email" />
                    </div>

                    @if (error) {
                        <p class="mt-4 flex items-start gap-2 rounded-token bg-danger-soft px-3 py-2.5 text-sm text-danger-soft-fg" role="alert">
                            <fo-icon name="error" [size]="17" class="mt-0.5" />
                            <span>{{ error }}</span>
                        </p>
                    }

                    <button foButton variant="primary" size="lg" type="submit" class="mt-6 w-full" [loading]="enviando" [disabled]="enviando">
                        <span>Enviar enlace</span>
                    </button>
                </form>

                <a authFooter [routerLink]="penaPublica.ruta('auth', 'login')" class="inline-flex items-center gap-1.5 text-sm font-medium text-ink-muted transition-colors hover:text-ink">
                    <fo-icon name="atras" [size]="16" />
                    Volver a iniciar sesión
                </a>
            </fo-auth-shell>
        }
    `
})
export class ForgotPassword {
    email: string = '';
    error: string | null = null;
    submitted: boolean = false;
    enviando: boolean = false;

    protected readonly penaPublica = inject(PenaPublicaService);
    private authService = inject(AuthService);

    sendLink(): void {
        this.error = null;

        if (!this.email) {
            this.error = 'El email es obligatorio.';
            return;
        }

        this.enviando = true;
        this.authService.forgotPassword(this.email).subscribe({
            next: () => {
                this.enviando = false;
                this.submitted = true;
            },
            error: (err) => {
                this.enviando = false;
                console.error(err);
                // El backend explica el motivo cuando lo sabe (por ejemplo, que el proveedor
                // de correo ha rechazado el envío), y es más útil que un mensaje genérico.
                this.error = err?.error?.message ?? 'Ha ocurrido un error. Por favor, inténtalo de nuevo más tarde.';
            }
        });
    }
}

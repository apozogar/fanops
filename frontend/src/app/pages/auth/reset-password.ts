import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AuthShellComponent } from './auth-shell.component';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiPasswordComponent } from '@/ui/ui-password.component';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { AuthService } from './auth.service';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, UiPasswordComponent, IconComponent, FormsModule, RouterModule],
    template: `
        @if (submitted) {
            <fo-auth-shell icon="check-circulo" title="Contraseña actualizada" subtitle="Ya puedes entrar con tu contraseña nueva.">
                <a foButton variant="primary" size="lg" class="w-full" [routerLink]="penaPublica.ruta('auth', 'login')">Ir a iniciar sesión</a>
            </fo-auth-shell>
        } @else {
            <fo-auth-shell icon="llave" title="Elige una contraseña nueva" subtitle="Escríbela dos veces para evitar erratas.">
                <form (ngSubmit)="resetPassword()">
                    <div class="space-y-4">
                        <div>
                            <label for="password" class="mb-1.5 block text-sm font-medium text-ink">Contraseña nueva</label>
                            <fo-password inputId="password" name="password" placeholder="Mínimo 8 caracteres" autocomplete="new-password" [(ngModel)]="password" />
                        </div>

                        <div>
                            <label for="confirmPassword" class="mb-1.5 block text-sm font-medium text-ink">Repetir contraseña</label>
                            <fo-password inputId="confirmPassword" name="confirmPassword" placeholder="Repite la contraseña" autocomplete="new-password" [invalid]="passwordsNoCoinciden()" [(ngModel)]="confirmPassword" />
                            @if (passwordsNoCoinciden()) {
                                <p class="mt-1.5 text-xs text-danger">Las contraseñas no coinciden.</p>
                            }
                        </div>
                    </div>

                    @if (error) {
                        <p class="mt-4 flex items-start gap-2 rounded-token bg-danger-soft px-3 py-2.5 text-sm text-danger-soft-fg" role="alert">
                            <fo-icon name="error" [size]="17" class="mt-0.5" />
                            <span>{{ error }}</span>
                        </p>
                    }

                    <!-- Sin token no hay nada que restablecer: el botón se desactiva en lugar de
                         dejar enviar para fallar después. -->
                    <button foButton variant="primary" size="lg" type="submit" class="mt-6 w-full" [loading]="guardando" [disabled]="guardando || !token">
                        <span>Guardar contraseña</span>
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
export class ResetPassword implements OnInit {
    password: string = '';
    confirmPassword: string = '';
    error: string | null = null;
    submitted: boolean = false;
    guardando: boolean = false;
    token: string | null = null;

    protected readonly penaPublica = inject(PenaPublicaService);
    private authService = inject(AuthService);
    private route = inject(ActivatedRoute);

    ngOnInit(): void {
        this.route.queryParams.subscribe((params) => {
            this.token = params['token'];
            if (!this.token) {
                this.error = 'El enlace no es válido o ha caducado. Solicita uno nuevo.';
            }
        });
    }

    passwordsNoCoinciden(): boolean {
        return !!this.confirmPassword && this.confirmPassword !== this.password;
    }

    resetPassword(): void {
        this.error = null;

        if (!this.token) {
            this.error = 'El enlace no es válido o ha caducado. Solicita uno nuevo.';
            return;
        }
        if (!this.password || !this.confirmPassword) {
            this.error = 'Introduce y confirma tu nueva contraseña.';
            return;
        }
        if (this.password !== this.confirmPassword) {
            this.error = 'Las contraseñas no coinciden.';
            return;
        }

        this.guardando = true;
        this.authService.resetPassword(this.token, this.password).subscribe({
            next: () => {
                this.guardando = false;
                this.submitted = true;
            },
            error: (err) => {
                this.guardando = false;
                console.error(err);
                this.error = 'El enlace ha caducado o no es válido. Por favor, solicita uno nuevo.';
            }
        });
    }
}

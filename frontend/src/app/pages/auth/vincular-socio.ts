import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { RippleModule } from 'primeng/ripple';
import { ThemeToggleComponent } from '@/ui/theme-toggle.component';
import { VinculacionInfo } from '@/interfaces/vinculacion.interface';
import { AuthService } from './auth.service';

/**
 * Confirmación de la vinculación de una cuenta con una ficha de socio que ya existía en el listado
 * de la peña. Se llega aquí desde el enlace del correo, que es la prueba de que quien confirma
 * controla ese buzón: hasta este paso no se ha creado ninguna cuenta ni se ha tocado la ficha.
 */
@Component({
    selector: 'app-vincular-socio',
    standalone: true,
    imports: [ButtonModule, InputTextModule, PasswordModule, ProgressSpinnerModule, FormsModule, RouterModule, RippleModule, ThemeToggleComponent],
    template: `
        <fo-theme-toggle [floating]="true" />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-screen overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px; max-width: 34rem">
                        @if (cargando) {
                            <div class="text-center">
                                <p-progressSpinner />
                            </div>
                        } @else if (error) {
                            <div class="text-center">
                                <i class="pi pi-times-circle text-red-500" style="font-size: 3rem"></i>
                                <h2 class="mt-4 text-surface-900 dark:text-surface-0">No se puede vincular</h2>
                                <p class="text-muted-color">{{ error }}</p>
                                <p-button label="Ir a Iniciar Sesión" styleClass="w-full mt-4" routerLink="/auth/login"></p-button>
                            </div>
                        } @else if (vinculado) {
                            <div class="text-center">
                                <i class="pi pi-check-circle text-primary" style="font-size: 3rem"></i>
                                <h2 class="mt-4 text-surface-900 dark:text-surface-0">¡Cuenta vinculada!</h2>
                                <p class="text-muted-color">Tu ficha de socio ya está asociada a esta cuenta. Te llevamos a la aplicación...</p>
                            </div>
                        } @else if (info) {
                            <div class="text-center mb-8">
                                <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Vincula tu ficha de socio</div>
                                <span class="text-muted-color font-medium">Confirma que esta ficha es tuya</span>
                            </div>

                            <div class="mb-6 text-surface-700 dark:text-surface-200">
                                <div><span class="text-muted-color">Socio:</span> <strong>{{ info.nombreSocio }}</strong> (nº {{ info.numeroSocio }})</div>
                                @if (info.nombrePena) {
                                    <div><span class="text-muted-color">Peña:</span> <strong>{{ info.nombrePena }}</strong></div>
                                }
                                <div><span class="text-muted-color">Email:</span> {{ info.email }}</div>
                                @if (info.fichas > 1) {
                                    <div class="mt-2 text-muted-color">Se vincularán también las {{ info.fichas - 1 }} ficha(s) restante(s) asociada(s) a este email.</div>
                                }
                            </div>

                            @if (info.requierePassword) {
                                <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Contraseña</label>
                                <p-password id="password" [(ngModel)]="password" placeholder="Contraseña" [toggleMask]="true" styleClass="mb-4" [fluid]="true"></p-password>
                                <label for="confirmPassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Confirmar Contraseña</label>
                                <p-password id="confirmPassword" [(ngModel)]="confirmPassword" placeholder="Confirmar Contraseña" [toggleMask]="true" styleClass="mb-4" [fluid]="true"></p-password>
                            }

                            @if (errorConfirmacion) {
                                <div class="p-error text-center mb-4">{{ errorConfirmacion }}</div>
                            }

                            <p-button label="Vincular mi ficha" styleClass="w-full" [loading]="confirmando" (click)="confirmar()"></p-button>
                        }
                    </div>
                </div>
            </div>
        </div>
    `
})
export class VincularSocio implements OnInit {
    token: string | null = null;
    info: VinculacionInfo | null = null;
    password = '';
    confirmPassword = '';
    cargando = true;
    confirmando = false;
    vinculado = false;
    /** Error que invalida el enlace entero (token inexistente, caducado o ya usado). */
    error: string | null = null;
    /** Error recuperable del formulario de confirmación. */
    errorConfirmacion: string | null = null;

    private authService = inject(AuthService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    ngOnInit(): void {
        // Si hubiera una sesión anterior en el navegador sobraría: esta pantalla crea una cuenta.
        localStorage.removeItem('token');

        this.token = this.route.snapshot.queryParamMap.get('token');
        if (!this.token) {
            this.cargando = false;
            this.error = 'El enlace no es válido. Vuelve a registrarte para recibir uno nuevo.';
            return;
        }

        this.authService.getVinculacion(this.token).subscribe({
            next: (response) => {
                this.info = response.data;
                this.cargando = false;
            },
            error: (err) => {
                this.cargando = false;
                this.error = err?.error?.message ?? 'El enlace no es válido o ha caducado.';
            }
        });
    }

    confirmar(): void {
        if (!this.token || !this.info) {
            return;
        }
        this.errorConfirmacion = null;

        if (this.info.requierePassword) {
            if (!this.password || !this.confirmPassword) {
                this.errorConfirmacion = 'Introduce y confirma tu contraseña.';
                return;
            }
            if (this.password !== this.confirmPassword) {
                this.errorConfirmacion = 'Las contraseñas no coinciden.';
                return;
            }
        }

        this.confirmando = true;
        this.authService.confirmarVinculacion(this.token, this.info.requierePassword ? this.password : undefined).subscribe({
            next: () => {
                this.confirmando = false;
                this.vinculado = true;
                // La ficha ya tiene los datos que cargó la peña, así que no pasamos por
                // "completar perfil": directo a la aplicación.
                setTimeout(() => this.router.navigate(['/']), 1500);
            },
            error: (err) => {
                this.confirmando = false;
                this.errorConfirmacion = err?.error?.message ?? 'No se pudo vincular la ficha. Inténtalo de nuevo.';
            }
        });
    }
}

import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthShellComponent } from './auth-shell.component';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiPasswordComponent } from '@/ui/ui-password.component';
import { VinculacionInfo } from '@/interfaces/vinculacion.interface';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { AuthService } from './auth.service';

/**
 * Confirmación de la vinculación de una cuenta con una ficha de socio que ya existía en el listado
 * de la peña. Se llega aquí desde el enlace del correo, que es la prueba de que quien confirma
 * controla ese buzón: hasta este paso no se ha creado ninguna cuenta ni se ha tocado la ficha.
 */
@Component({
    selector: 'app-vincular-socio',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, UiPasswordComponent, IconComponent, FormsModule, RouterModule],
    template: `
        @if (cargando) {
            <fo-auth-shell title="Comprobando el enlace" subtitle="Un momento, estamos verificando tu invitación.">
                <div class="flex items-center gap-3 text-sm text-ink-muted">
                    <span class="fo-spinner" aria-hidden="true"></span>
                    Cargando...
                </div>
            </fo-auth-shell>
        } @else if (error) {
            <fo-auth-shell icon="error" title="No se puede vincular" [subtitle]="error">
                <a foButton variant="primary" size="lg" class="w-full" [routerLink]="penaPublica.ruta('auth', 'login')">Ir a iniciar sesión</a>
            </fo-auth-shell>
        } @else if (vinculado) {
            <fo-auth-shell icon="check-circulo" title="¡Cuenta vinculada!" subtitle="Tu ficha de socio ya está asociada a esta cuenta. Te llevamos a la aplicación..."> </fo-auth-shell>
        } @else if (info) {
            <fo-auth-shell title="Vincula tu ficha de socio" subtitle="Confirma que esta ficha es tuya.">
                <!-- Resumen de la ficha. Va en una lista de definición porque son pares
                     etiqueta/valor, no un formulario. -->
                <dl class="divide-y divide-line rounded-token border border-line bg-surface-2 px-3.5 text-sm">
                    <div class="flex items-baseline justify-between gap-4 py-2.5">
                        <dt class="text-ink-muted">Socio</dt>
                        <dd class="text-right font-medium">
                            {{ info.nombreSocio }} <span class="font-normal text-ink-muted">(nº {{ info.numeroSocio }})</span>
                        </dd>
                    </div>
                    @if (info.nombrePena) {
                        <div class="flex items-baseline justify-between gap-4 py-2.5">
                            <dt class="text-ink-muted">Peña</dt>
                            <dd class="text-right font-medium">{{ info.nombrePena }}</dd>
                        </div>
                    }
                    <div class="flex items-baseline justify-between gap-4 py-2.5">
                        <dt class="text-ink-muted">Email</dt>
                        <dd class="min-w-0 break-all text-right font-medium">{{ info.email }}</dd>
                    </div>
                </dl>

                @if (info.fichas > 1) {
                    <p class="mt-3 flex items-start gap-2 rounded-token bg-info-soft px-3 py-2.5 text-sm text-info-soft-fg">
                        <fo-icon name="info" [size]="17" class="mt-0.5" />
                        <span>Se vincularán también las {{ info.fichas - 1 }} ficha(s) restante(s) asociada(s) a este email.</span>
                    </p>
                }

                <form (ngSubmit)="confirmar()">
                    @if (info.requierePassword) {
                        <div class="mt-5 space-y-4">
                            <div>
                                <label for="password" class="mb-1.5 block text-sm font-medium text-ink">Contraseña</label>
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
                    }

                    @if (errorConfirmacion) {
                        <p class="mt-4 flex items-start gap-2 rounded-token bg-danger-soft px-3 py-2.5 text-sm text-danger-soft-fg" role="alert">
                            <fo-icon name="error" [size]="17" class="mt-0.5" />
                            <span>{{ errorConfirmacion }}</span>
                        </p>
                    }

                    <button foButton variant="primary" size="lg" type="submit" class="mt-6 w-full" [loading]="confirmando" [disabled]="confirmando">
                        <span>Vincular mi ficha</span>
                    </button>
                </form>
            </fo-auth-shell>
        }
    `,
    styles: `
        /* Indicador de carga de la comprobación del enlace. Reutiliza la animación global
           fo-spin definida en base.scss para el estado de carga de los botones. */
        .fo-spinner {
            display: inline-block;
            width: 1.1rem;
            height: 1.1rem;
            border: 2px solid currentColor;
            border-right-color: transparent;
            border-radius: var(--fo-radius-full);
            animation: fo-spin 0.6s linear infinite;
        }

        @media (prefers-reduced-motion: reduce) {
            .fo-spinner {
                animation: none;
                border-right-color: currentColor;
                opacity: 0.4;
            }
        }
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

    protected readonly penaPublica = inject(PenaPublicaService);
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

    passwordsNoCoinciden(): boolean {
        return !!this.confirmPassword && this.confirmPassword !== this.password;
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

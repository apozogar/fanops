import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthShellComponent } from '../auth-shell.component';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiInputDirective } from '@/ui/ui-input.directive';
import { UiPasswordComponent } from '@/ui/ui-password.component';
import { PenaPublicaService } from '@/core/pena/pena-publica.service';
import { AuthService } from '../auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, UiInputDirective, UiPasswordComponent, IconComponent, FormsModule, RouterModule],
    templateUrl: './login.component.html'
})
export class LoginComponent {
    email: string = '';
    password: string = '';
    error: string | null = null;
    loading: boolean = false;

    protected readonly penaPublica = inject(PenaPublicaService);
    private authService = inject(AuthService);
    private router = inject(Router);

    login(): void {
        this.error = null;

        if (!this.email || !this.password) {
            this.error = 'El email y la contraseña son obligatorios.';
            return;
        }

        this.loading = true;
        this.authService.login({ email: this.email, password: this.password }).subscribe({
            next: () => {
                this.router.navigate(['/']);
            },
            error: (error) => {
                this.loading = false;
                console.error('Login error:', error);
                this.error = 'Email o contraseña incorrectos. Por favor, inténtalo de nuevo.';
            }
        });
    }
}

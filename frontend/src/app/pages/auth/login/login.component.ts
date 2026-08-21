import {Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {ButtonModule} from 'primeng/button';
import {CheckboxModule} from 'primeng/checkbox';
import {InputTextModule} from 'primeng/inputtext';
import {PasswordModule} from 'primeng/password';
import {RippleModule} from 'primeng/ripple';
import { ThemeToggleComponent } from '@/ui/theme-toggle.component';
import {AuthService} from '../auth.service';
import {CommonModule} from '@angular/common';
import {Message} from "primeng/message";

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ButtonModule, CheckboxModule, InputTextModule, PasswordModule, FormsModule, RouterModule, RippleModule, ThemeToggleComponent, Message],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {
    email: string = '';
    password: string = '';
    checked: boolean = false;
    error: string | null = null;
    loading: boolean = false;

    private authService = inject(AuthService);
    private router = inject(Router);

    login(): void {
        this.error = null;
        if (this.email && this.password) {
            this.loading = true;
            this.authService.login({email: this.email, password: this.password})
            .subscribe({
                next: () => {
                    this.router.navigate(['/']);
                },
                error: (error) => {
                    this.loading = false;
                    console.error('Login error:', error);
                    this.error = 'Email o contraseña incorrectos. Por favor, inténtalo de nuevo.';
                },
            });
        } else {
            this.error = 'El email y la contraseña son obligatorios.';
        }
    }
}

import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthShellComponent } from './auth-shell.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';

/**
 * Error genérico. Antes venía de la plantilla: en inglés y con una ilustración cargada del CDN
 * de PrimeFaces, que rompía la pantalla sin conexión a internet.
 */
@Component({
    selector: 'app-error',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, RouterModule],
    template: `
        <fo-auth-shell icon="error" title="Algo ha ido mal" subtitle="No hemos podido cargar lo que buscabas. Vuelve a intentarlo en un momento.">
            <a foButton variant="primary" size="lg" class="w-full" routerLink="/">Volver al inicio</a>
        </fo-auth-shell>
    `
})
export class Error {}

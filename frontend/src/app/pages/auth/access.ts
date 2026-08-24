import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthShellComponent } from './auth-shell.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';

/**
 * Acceso denegado. Antes venía de la plantilla: en inglés y con una ilustración cargada del CDN
 * de PrimeFaces, que rompía la pantalla sin conexión a internet.
 */
@Component({
    selector: 'app-access',
    standalone: true,
    imports: [AuthShellComponent, UiButtonDirective, RouterModule],
    template: `
        <fo-auth-shell icon="no-permitido" title="Acceso denegado" subtitle="Tu cuenta no tiene permisos para ver esta pantalla. Si crees que es un error, habla con la junta de tu peña.">
            <a foButton variant="primary" size="lg" class="w-full" routerLink="/">Volver al inicio</a>
        </fo-auth-shell>
    `
})
export class Access {}

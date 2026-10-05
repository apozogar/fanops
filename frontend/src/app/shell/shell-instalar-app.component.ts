import { Component, inject } from '@angular/core';
import { InstalarAppService } from '@/core/platform/instalar-app.service';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';

/**
 * Aviso para instalar la aplicación en el móvil. Va dentro del contenido, no fijo, para no
 * tapar la navegación; se puede descartar y no vuelve a salir en un mes.
 */
@Component({
    selector: 'fo-shell-instalar-app',
    standalone: true,
    imports: [IconComponent, UiButtonDirective],
    template: `
        @if (instalar.mostrarAviso()) {
            <section class="mb-4 flex items-start gap-3 rounded-token-sm border border-line bg-surface p-3" aria-label="Instalar la aplicación">
                <img src="icons/icon-192.png" alt="" class="h-10 w-10 shrink-0 rounded-token-sm" />
                <div class="min-w-0 flex-1 text-sm">
                    <p class="font-semibold">Instala FanOps en tu móvil</p>
                    @if (instalar.puedeInstalarDirecto()) {
                        <p class="mt-0.5 text-ink-muted">Ábrela como una app, desde tu pantalla de inicio y sin barra del navegador.</p>
                        <button type="button" foButton variant="primary" class="mt-2" (click)="instalar.instalar()">
                            <fo-icon name="descargar" [size]="16" />
                            Instalar
                        </button>
                    } @else {
                        <p class="mt-0.5 text-ink-muted">Pulsa <fo-icon name="compartir" [size]="14" label="Compartir" class="align-text-bottom" /> <strong>Compartir</strong> en Safari y elige <strong>Añadir a pantalla de inicio</strong>.</p>
                    }
                </div>
                <button type="button" foButton variant="ghost" size="icon" (click)="instalar.descartar()" aria-label="Ocultar aviso">
                    <fo-icon name="cerrar" [size]="16" />
                </button>
            </section>
        }
    `
})
export class ShellInstalarAppComponent {
    protected readonly instalar = inject(InstalarAppService);
}

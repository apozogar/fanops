import {Injectable, effect, inject, signal} from '@angular/core';
import {SwUpdate} from '@angular/service-worker';
import {MessageService} from 'primeng/api';

/**
 * Servicio que detecta actualizaciones del service worker y notifica al usuario.
 *
 * Revisa periódicamente si hay una nueva versión disponible. Cuando la hay, recargaremos
 * la aplicación automáticamente después de unos segundos o el usuario puede recargar.
 */
@Injectable({providedIn: 'root'})
export class SwUpdateService {
    private readonly swUpdate = inject(SwUpdate);
    private readonly messageService = inject(MessageService);

    private readonly updateAvailable = signal(false);

    constructor() {
        // No hacer nada si el navegador no soporta service workers
        if (!this.swUpdate.isEnabled) {
            return;
        }

        // Revisar si hay una actualización al iniciar
        this.checkForUpdates();

        // Revisar cada 10 segundos (el usuario notará el cambio casi al instante)
        setInterval(() => {
            this.checkForUpdates();
        }, 10000);

        // Mostrar notificación cuando hay una actualización disponible
        effect(() => {
            if (this.updateAvailable()) {
                this.messageService.add({
                    severity: 'info',
                    summary: 'Nueva versión disponible',
                    detail: 'Se ha detectado una actualización. Recargando en 5 segundos...',
                    sticky: false,
                    life: 10000,
                    key: 'sw-update'
                });

                // Recargar después de 5 segundos
                setTimeout(() => {
                    this.reload();
                }, 5000);
            }
        });
    }

    private checkForUpdates(): void {
        this.swUpdate.checkForUpdate().then(
            updated => {
                if (updated) {
                    this.updateAvailable.set(true);
                }
            },
            err => {
                // Errores de red o del service worker: ignorar silenciosamente
                // (la revisión siguiente puede tener éxito)
            }
        );
    }

    reload(): void {
        this.swUpdate.activateUpdate().then(
            () => {
                window.location.reload();
            },
            err => {
                console.error('No se pudo activar la actualización:', err);
                // Recargar de todas formas
                window.location.reload();
            }
        );
    }
}

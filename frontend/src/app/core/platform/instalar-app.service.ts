import { Injectable, computed, signal } from '@angular/core';

/** Evento no estándar de Chromium con el que el navegador cede el control de la instalación. */
interface BeforeInstallPromptEvent extends Event {
    prompt(): Promise<void>;
    userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

const CLAVE_DESCARTADO = 'fanops.instalar-app.descartado';
/** Tras descartar el aviso no se vuelve a enseñar hasta pasado este tiempo. */
const DIAS_SIN_INSISTIR = 30;

/**
 * Decide si se ofrece instalar la aplicación (PWA) y cómo.
 *
 * Hay dos caminos porque los navegadores no se comportan igual:
 * - Chrome/Edge/Samsung Internet en Android lanzan `beforeinstallprompt`; se guarda el evento y
 *   se dispara al pulsar el botón, con el diálogo nativo de instalación.
 * - Safari en iOS no tiene ese evento ni ninguna API de instalación: lo único posible es explicar
 *   los pasos (Compartir → Añadir a pantalla de inicio).
 *
 * Solo se ofrece en móvil y nunca si la app ya se abrió instalada.
 */
@Injectable({ providedIn: 'root' })
export class InstalarAppService {
    private readonly eventoInstalacion = signal<BeforeInstallPromptEvent | null>(null);
    private readonly descartado = signal(this.leerDescartado());
    private readonly instalada = signal(this.estaInstalada());

    /** Plataforma que necesita instrucciones manuales: iPhone/iPad con Safari. */
    readonly esIos = this.detectarIosSafari();

    /** El navegador permite instalar con un solo toque. */
    readonly puedeInstalarDirecto = computed(() => this.eventoInstalacion() !== null);

    /** Hay que enseñar el aviso: instalable, móvil, no instalada y no descartada. */
    readonly mostrarAviso = computed(() => !this.instalada() && !this.descartado() && this.esMovil() && (this.puedeInstalarDirecto() || this.esIos));

    constructor() {
        window.addEventListener('beforeinstallprompt', (evento) => {
            // Se evita la mini-barra automática de Chrome para ofrecerlo en el momento y sitio elegidos.
            evento.preventDefault();
            this.eventoInstalacion.set(evento as BeforeInstallPromptEvent);
        });
        window.addEventListener('appinstalled', () => {
            this.instalada.set(true);
            this.eventoInstalacion.set(null);
        });
    }

    async instalar(): Promise<void> {
        const evento = this.eventoInstalacion();
        if (!evento) {
            return;
        }
        await evento.prompt();
        const { outcome } = await evento.userChoice;
        // El evento solo se puede usar una vez, haya aceptado o no.
        this.eventoInstalacion.set(null);
        if (outcome === 'dismissed') {
            this.descartar();
        }
    }

    descartar(): void {
        this.descartado.set(true);
        try {
            localStorage.setItem(CLAVE_DESCARTADO, String(Date.now()));
        } catch {
            // Sin almacenamiento (modo privado) el aviso sigue oculto solo durante la sesión.
        }
    }

    private leerDescartado(): boolean {
        try {
            const marca = Number(localStorage.getItem(CLAVE_DESCARTADO));
            return marca > 0 && Date.now() - marca < DIAS_SIN_INSISTIR * 24 * 60 * 60 * 1000;
        } catch {
            return false;
        }
    }

    private estaInstalada(): boolean {
        // iOS expone su propia propiedad no estándar además del display-mode.
        return window.matchMedia('(display-mode: standalone)').matches || (navigator as Navigator & { standalone?: boolean }).standalone === true;
    }

    private esMovil(): boolean {
        return window.matchMedia('(max-width: 1023px)').matches;
    }

    private detectarIosSafari(): boolean {
        const ua = navigator.userAgent;
        // Desde iPadOS 13 el iPad se presenta como Mac; se distingue por tener pantalla táctil.
        const esIos = /iPhone|iPad|iPod/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
        // En iOS solo Safari puede añadir a pantalla de inicio con esta ruta; Chrome/Firefox/Edge
        // (CriOS, FxiOS, EdgiOS) no ofrecen el mismo menú en versiones antiguas, así que se excluyen.
        return esIos && !/CriOS|FxiOS|EdgiOS|OPiOS/.test(ua);
    }
}

import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { MessageService } from 'primeng/api';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { finalize } from 'rxjs';
import { ParticipanteSorteo, SocioSolicitudCarnet, SorteoCarnet } from '@/interfaces/sorteo-carnet.dto';
import { SorteoCarnetService } from '@/services/sorteo-carnet.service';
import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
import { UiTagComponent } from '@/ui/ui-tag.component';

/**
 * El bombo de carnets de un evento: quién está dentro antes del sorteo y cómo salieron las bolas
 * después.
 *
 * La animación no decide nada. El orden de extracción lo fijó el servidor al celebrar el sorteo
 * y aquí solo se representa, así que la repetición siempre enseña lo mismo por muchas veces que
 * se pulse.
 */
@Component({
    selector: 'fo-sorteo-bombo',
    standalone: true,
    imports: [CommonModule, ProgressSpinnerModule, IconComponent, UiButtonDirective, UiTagComponent],
    templateUrl: './sorteo-bombo.component.html',
    styleUrls: ['./sorteo-bombo.component.scss']
})
export class SorteoBomboComponent implements OnInit, OnDestroy {
    /** Bolas que se dibujan como mucho: con más se amontonan y no se lee ninguna. */
    private static readonly MAX_BOLAS_VISIBLES = 24;

    readonly eventoUid = input.required<string>();

    /** Avisa al listado de eventos de que el sorteo ha cambiado, para que refresque su tarjeta. */
    readonly cambiado = output<void>();

    protected readonly sorteo = signal<SorteoCarnet | null>(null);
    protected readonly cargando = signal(true);
    /** Ficha con una acción en vuelo, para poner el indicador solo en su botón. */
    protected readonly accionEnCurso = signal<string | null>(null);

    /** Ganadores ya extraídos en la animación en curso. */
    protected readonly revelados = signal(0);
    protected readonly girando = signal(false);
    protected readonly terminada = signal(false);

    protected readonly cuentaAtras = signal('');

    private readonly temporizadores: ReturnType<typeof setTimeout>[] = [];
    private intervaloCuentaAtras?: ReturnType<typeof setInterval>;

    private readonly sorteoService = inject(SorteoCarnetService);
    private readonly messageService = inject(MessageService);

    protected readonly ganadores = computed(() =>
        (this.sorteo()?.participantes ?? []).filter(p => this.esPremio(p))
    );

    protected readonly suplentes = computed(() =>
        (this.sorteo()?.participantes ?? []).filter(p => p.posicion != null && !this.esPremio(p))
    );

    /** Bolas que se pintan dentro del bombo, recortadas para que se sigan viendo. */
    protected readonly bolas = computed(() =>
        (this.sorteo()?.participantes ?? []).slice(0, SorteoBomboComponent.MAX_BOLAS_VISIBLES)
    );

    protected readonly bolasOcultas = computed(() =>
        Math.max(0, (this.sorteo()?.participantes.length ?? 0) - SorteoBomboComponent.MAX_BOLAS_VISIBLES)
    );

    /** Huecos de premio, uno por carnet, se hayan cubierto o no. */
    protected readonly huecos = computed(() =>
        Array.from({ length: this.sorteo()?.plazasCarnet ?? 0 }, (_, i) => i)
    );

    ngOnInit(): void {
        this.cargar(true);
    }

    ngOnDestroy(): void {
        this.pararAnimacion();
        if (this.intervaloCuentaAtras) clearInterval(this.intervaloCuentaAtras);
    }

    // ----------------------------------------------------------------
    // Carga
    // ----------------------------------------------------------------

    private cargar(primeraVez = false): void {
        this.cargando.set(true);
        this.sorteoService.consultar(this.eventoUid()).pipe(
            finalize(() => this.cargando.set(false))
        ).subscribe({
            next: resp => {
                this.aplicar(resp.data ?? null);
                if (primeraVez) this.arrancarAnimacionSiProcede();
            },
            error: () => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'No se pudo cargar el sorteo.'
            })
        });
    }

    private aplicar(sorteo: SorteoCarnet | null): void {
        this.sorteo.set(sorteo);
        this.prepararCuentaAtras();
        // Un sorteo ya celebrado se muestra resuelto: la animación solo corre cuando se pide.
        if (sorteo && !sorteo.abierto && !this.girando()) {
            this.revelados.set(this.ganadores().length);
        }
    }

    /**
     * La primera vez que se ve un sorteo ya celebrado la animación arranca sola: es el momento en
     * que la gente quiere ver salir las bolas. A partir de ahí queda el botón de repetición, para
     * que volver a abrir el evento no obligue a esperar otra vez.
     */
    private arrancarAnimacionSiProcede(): void {
        const sorteo = this.sorteo();
        if (!sorteo || sorteo.abierto || this.ganadores().length === 0) return;
        if (this.yaVisto()) return;
        this.marcarVisto();
        this.reproducir();
    }

    private clavePreferencia(): string {
        return `fanops.sorteo.visto.${this.eventoUid()}`;
    }

    private yaVisto(): boolean {
        // En una ventana privada o con el almacenamiento bloqueado esto lanza; entonces se anima
        // otra vez, que es mejor que romper la vista.
        try {
            return localStorage.getItem(this.clavePreferencia()) === '1';
        } catch {
            return false;
        }
    }

    private marcarVisto(): void {
        try {
            localStorage.setItem(this.clavePreferencia(), '1');
        } catch {
            // sin persistencia la animación volverá a arrancar sola; no es grave
        }
    }

    // ----------------------------------------------------------------
    // Animación
    // ----------------------------------------------------------------

    protected reproducir(): void {
        const total = this.ganadores().length;
        this.pararAnimacion();
        this.terminada.set(false);
        this.revelados.set(0);

        if (total === 0) {
            this.terminada.set(true);
            return;
        }
        if (this.animacionReducida()) {
            this.revelados.set(total);
            this.terminada.set(true);
            return;
        }

        this.girando.set(true);
        for (let i = 0; i < total; i++) {
            this.programar(() => this.revelados.set(i + 1), 1300 + i * 1500);
        }
        this.programar(() => {
            this.girando.set(false);
            this.terminada.set(true);
        }, 1300 + (total - 1) * 1500 + 900);
    }

    /** Respeta a quien tiene desactivadas las animaciones: se le enseña el resultado y ya. */
    private animacionReducida(): boolean {
        return typeof matchMedia === 'function'
            && matchMedia('(prefers-reduced-motion: reduce)').matches;
    }

    private programar(accion: () => void, retardo: number): void {
        this.temporizadores.push(setTimeout(accion, retardo));
    }

    private pararAnimacion(): void {
        this.temporizadores.forEach(clearTimeout);
        this.temporizadores.length = 0;
        this.girando.set(false);
    }

    // ----------------------------------------------------------------
    // Cuenta atrás
    // ----------------------------------------------------------------

    private prepararCuentaAtras(): void {
        if (this.intervaloCuentaAtras) clearInterval(this.intervaloCuentaAtras);
        const sorteo = this.sorteo();
        if (!sorteo?.abierto || !sorteo.fechaProgramada) {
            this.cuentaAtras.set('');
            return;
        }
        this.refrescarCuentaAtras();
        this.intervaloCuentaAtras = setInterval(() => this.refrescarCuentaAtras(), 1000);
    }

    private refrescarCuentaAtras(): void {
        const sorteo = this.sorteo();
        if (!sorteo?.fechaProgramada) return;

        const restante = new Date(sorteo.fechaProgramada).getTime() - Date.now();
        if (restante <= 0) {
            this.cuentaAtras.set('');
            if (this.intervaloCuentaAtras) clearInterval(this.intervaloCuentaAtras);
            // Pasada la hora el servidor ya lo ha celebrado: se pide el resultado y se anima.
            this.cargar(true);
            return;
        }

        const segundos = Math.floor(restante / 1000);
        const dias = Math.floor(segundos / 86400);
        const horas = Math.floor((segundos % 86400) / 3600);
        const minutos = Math.floor((segundos % 3600) / 60);
        const resto = segundos % 60;
        this.cuentaAtras.set(dias > 0
            ? `${dias}d ${horas}h ${minutos}m`
            : `${this.dosDigitos(horas)}:${this.dosDigitos(minutos)}:${this.dosDigitos(resto)}`);
    }

    private dosDigitos(valor: number): string {
        return valor.toString().padStart(2, '0');
    }

    // ----------------------------------------------------------------
    // Acciones
    // ----------------------------------------------------------------

    protected apuntar(socio: SocioSolicitudCarnet): void {
        this.ejecutar(socio.socioUid,
            this.sorteoService.apuntar(this.eventoUid(), [socio.socioUid]),
            `${socio.nombre} ya está en el bombo y apuntado al evento.`);
    }

    protected salir(socio: SocioSolicitudCarnet): void {
        this.ejecutar(socio.socioUid,
            this.sorteoService.salir(this.eventoUid(), socio.socioUid),
            `${socio.nombre} ya no participa en el sorteo.`);
    }

    protected renunciar(socio: SocioSolicitudCarnet): void {
        this.ejecutar(socio.socioUid,
            this.sorteoService.renunciar(this.eventoUid(), socio.socioUid),
            'Carnet devuelto. Pasa al siguiente de la lista.');
    }

    private ejecutar(socioUid: string, peticion: ReturnType<SorteoCarnetService['apuntar']>,
                     exito: string): void {
        this.accionEnCurso.set(socioUid);
        peticion.pipe(
            finalize(() => this.accionEnCurso.set(null))
        ).subscribe({
            next: resp => {
                this.aplicar(resp.data ?? null);
                this.cambiado.emit();
                // El servidor sabe cosas que aquí no se ven, como si la plaza del evento ha
                // quedado en lista de espera, así que su mensaje manda sobre el de respaldo.
                this.messageService.add({
                    severity: 'success',
                    summary: 'Hecho',
                    detail: resp.message || exito
                });
            },
            error: err => this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: err.error?.message || 'No se pudo completar la operación.'
            })
        });
    }

    // ----------------------------------------------------------------
    // Presentación
    // ----------------------------------------------------------------

    /** true si esa posición está dentro de los carnets que se reparten. */
    protected esPremio(participante: ParticipanteSorteo): boolean {
        const plazas = this.sorteo()?.plazasCarnet ?? 0;
        return participante.posicion != null && participante.posicion <= plazas;
    }

    /** Ganador que ocupa un hueco de premio, si ya se ha revelado en la animación. */
    protected ganadorDe(hueco: number): ParticipanteSorteo | null {
        return hueco < this.revelados() ? (this.ganadores()[hueco] ?? null) : null;
    }

    /** La bola sale del bombo cuando le toca en la animación. */
    protected bolaExtraida(participante: ParticipanteSorteo): boolean {
        const posicion = this.ganadores().indexOf(participante);
        return posicion >= 0 && posicion < this.revelados();
    }

    protected etiqueta(socio: { nombre: string; numeroSocio?: number | null }): string {
        return socio.numeroSocio != null ? `${socio.nombre} (nº ${socio.numeroSocio})` : socio.nombre;
    }

    /** Iniciales para la bola: el nombre completo no cabe. */
    protected iniciales(nombre: string): string {
        return nombre.split(/\s+/).filter(Boolean).slice(0, 2).map(p => p[0]).join('').toUpperCase();
    }

    protected puestoSuplente(participante: ParticipanteSorteo): number {
        return (participante.posicion ?? 0) - (this.sorteo()?.plazasCarnet ?? 0);
    }

    protected textoPapeletas(papeletas: number): string {
        return papeletas === 1 ? '1 papeleta' : `${papeletas} papeletas`;
    }
}

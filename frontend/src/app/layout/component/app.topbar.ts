import { booleanAttribute, Component, Input, OnInit, OnDestroy } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StyleClassModule } from 'primeng/styleclass';
import { Select } from 'primeng/select';
import { AppConfigurator } from './app.configurator';
import { LayoutService } from '../service/layout.service';
import { AuthService } from '../../pages/auth/auth.service';
import { PenaService } from '@/services/pena.service';
import { PenaContextService } from '@/services/pena-context.service';
import { Pena } from '@/interfaces/socio.interface';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-topbar',
    standalone: true,
    imports: [RouterModule, CommonModule, FormsModule, StyleClassModule, Select, AppConfigurator],
    styles: `
        /* topbar.component.css */
        .layout-topbar {
            //background: #008835;
            //color: white;
        }

        .layout-topbar-logo-container .layout-topbar-logo .logo-pequeno {
            height: 2.5rem; /* Ajusta el tamaño según sea necesario */
            margin-right: 0.5rem;
        }
    `,
    template: ` <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" (click)="layoutService.onMenuToggle()">
                <i class="pi pi-bars"></i>
            </button>
            <a class="layout-topbar-logo" routerLink="/">
                <!-- Se muestra la imagen de la peña si está disponible, de lo contrario, una imagen por defecto -->
                @if (imageUrl) {
                    <img [src]="imageUrl" alt="Logo de la Peña" class="logo-pequeno" />
                }
                @if (!imageUrl) {
                    <img src="assets/layout/images/logo-dark.svg" alt="Logo por defecto" class="logo-pequeno" />
                }
                <!-- Ajusta la ruta de tu logo por defecto -->
                <span>{{ nombre }}</span>
            </a>
            <!-- Selector de peña: solo visible para el superadmin, que no pertenece a una peña -->
            @if (isSuperAdmin) {
                <p-select
                    [options]="penasDisponibles"
                    [(ngModel)]="penaSeleccionadaId"
                    (ngModelChange)="onPenaChange($event)"
                    optionLabel="nombre"
                    optionValue="id"
                    placeholder="Selecciona una peña"
                    styleClass="ml-3 w-15rem"
                />
            }
        </div>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" (click)="toggleDarkMode()">
                    <i [ngClass]="{ 'pi ': true, 'pi-moon': layoutService.isDarkTheme(), 'pi-sun': !layoutService.isDarkTheme() }"></i>
                </button>
                <div class="relative">
                    <app-configurator />
                </div>
            </div>

            <button class="layout-topbar-menu-button layout-topbar-action" pStyleClass="@next" enterFromClass="hidden" enterActiveClass="animate-scalein" leaveToClass="hidden" leaveActiveClass="animate-fadeout" [hideOnOutsideClick]="true">
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <button type="button" class="layout-topbar-action" (click)="logout()">
                        <i class="pi pi-sign-out"></i>
                        <span>Cerrar Sesión</span>
                    </button>
                </div>
            </div>
        </div>
    </div>`
})
export class AppTopbar implements OnInit, OnDestroy {
    items!: MenuItem[];
    nombre: string = 'Peña Bética Luis Bellver - Gilena';
    imageUrl: string | undefined;
    isSuperAdmin = false;
    penasDisponibles: Pena[] = [];
    penaSeleccionadaId: number | null = null;
    private penaSubscription: Subscription | undefined;

    constructor(
        public layoutService: LayoutService,
        private authService: AuthService,
        private penaService: PenaService,
        private penaContextService: PenaContextService,
        private router: Router
    ) {
        // La suscripción se ha movido a ngOnInit
    }

    ngOnInit(): void {
        this.isSuperAdmin = this.authService.isSuperAdmin();

        if (this.isSuperAdmin) {
            this.cargarPenasDisponibles();
            return;
        }

        this.penaSubscription = this.authService.currentPena.subscribe((pena: Pena | null) => {
            if (pena) {
                this.nombre = pena.nombre;
                this.imageUrl = pena.logo;
            } else {
                // Restablecer a valores por defecto si no hay peña (ej. después de cerrar sesión)
                this.nombre = 'FanOperations App';
                this.imageUrl = undefined;
            }
        });
    }

    ngOnDestroy(): void {
        this.penaSubscription?.unsubscribe(); // Desuscribirse para evitar fugas de memoria
    }

    private cargarPenasDisponibles(): void {
        this.penaService.listAll().subscribe({
            next: (response) => {
                this.penasDisponibles = response.data ?? [];
                this.penaSeleccionadaId = this.penaContextService.getSelectedPenaId();
                this.actualizarNombreYLogo(this.penaSeleccionadaId);
            },
            error: () => {
                this.penasDisponibles = [];
            }
        });
    }

    onPenaChange(penaId: number | null): void {
        this.penaContextService.setSelectedPenaId(penaId);
        // Recargamos para que todas las vistas vuelvan a pedir sus datos ya acotados a la
        // peña recién seleccionada (viaja como cabecera X-Pena-Id en cada petición).
        window.location.reload();
    }

    private actualizarNombreYLogo(penaId: number | null): void {
        const pena = this.penasDisponibles.find((p) => p.id === penaId);
        this.nombre = pena?.nombre ?? 'Panel de superadmin';
        this.imageUrl = pena?.logo;
    }

    toggleDarkMode() {
        this.layoutService.layoutConfig.update((state) => ({
            ...state,
            darkTheme: !state.darkTheme
        }));
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/auth/login']);
    }
}

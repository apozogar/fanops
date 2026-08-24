import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { ActivePenaService } from './active-pena.service';
import { PageReloader } from '@/core/platform/page-reloader.service';
import { PenaContextService } from '@/services/pena-context.service';
import { PenaService } from '@/services/pena.service';
import { ThemeService } from '@/core/theme/theme.service';
import { AuthService } from '@/pages/auth/auth.service';
import { Pena } from '@/interfaces/socio.interface';

const PENAS: Pena[] = [
    { id: 7, nombre: 'Peña Siete', color: '#008835' },
    { id: 8, nombre: 'Peña Ocho', color: '#123456' }
];

class AuthStub {
    superAdmin = true;
    currentPena = new BehaviorSubject<Pena | null>(null).asObservable();
    /**
     * Sesión abierta. Se expone el sujeto para que un test pueda empujar null y simular el
     * cierre de sesión, que es lo que hace a ActivePenaService olvidar la peña activa.
     */
    currentUserSubject = new BehaviorSubject<unknown>({ email: 'superadmin@fanops.local' });
    currentUser = this.currentUserSubject.asObservable();
    isSuperAdmin() {
        return this.superAdmin;
    }
}

class PenaServiceStub {
    respuesta: Pena[] = PENAS;
    fallar = false;
    listAll() {
        return this.fallar ? throwError(() => new Error('boom')) : of({ success: true, message: '', data: this.respuesta });
    }
}

class ReloaderSpy {
    veces = 0;
    reload() {
        this.veces++;
    }
}

describe('ActivePenaService (superadmin)', () => {
    let servicio: ActivePenaService;
    let contexto: PenaContextService;
    let reloader: ReloaderSpy;
    let penaService: PenaServiceStub;
    let auth: AuthStub;

    beforeEach(() => {
        localStorage.clear();
        reloader = new ReloaderSpy();
        penaService = new PenaServiceStub();

        TestBed.configureTestingModule({
            providers: [ActivePenaService, PenaContextService, ThemeService, { provide: AuthService, useClass: AuthStub }, { provide: PenaService, useValue: penaService }, { provide: PageReloader, useValue: reloader }]
        });

        auth = TestBed.inject(AuthService) as unknown as AuthStub;
        servicio = TestBed.inject(ActivePenaService);
        contexto = TestBed.inject(PenaContextService);
    });

    afterEach(() => localStorage.clear());

    it('respeta la peña que ya estaba seleccionada, no la primera de la lista', () => {
        contexto.setSelectedPenaId(8);

        servicio.init();

        expect(servicio.pena()?.id).toBe(8);
        expect(contexto.getSelectedPenaId()).toBe(8);
    });

    it('sin selección previa activa la primera peña y la persiste, sin recargar', () => {
        servicio.init();

        expect(servicio.pena()?.id).toBe(7);
        expect(contexto.getSelectedPenaId()).toBe(7);
        expect(reloader.veces).toBe(0);
    });

    it('al cambiar de peña persiste el id nuevo antes de recargar', () => {
        servicio.init();
        expect(contexto.getSelectedPenaId()).toBe(7);

        servicio.select(8);

        expect(contexto.getSelectedPenaId()).toBe(8);
        expect(servicio.pena()?.id).toBe(8);
        expect(reloader.veces).toBe(1);
    });

    it('no recarga si se vuelve a elegir la peña que ya estaba activa', () => {
        servicio.init();

        servicio.select(7);

        expect(reloader.veces).toBe(0);
        expect(contexto.getSelectedPenaId()).toBe(7);
    });

    it('ignora un id que no corresponde a ninguna peña conocida', () => {
        servicio.init();

        servicio.select(999);

        expect(contexto.getSelectedPenaId()).toBe(7);
        expect(reloader.veces).toBe(0);
    });

    it('al cerrar sesión olvida la peña activa y sus opciones', () => {
        servicio.init();
        expect(servicio.pena()?.id).toBe(7);

        // Cerrar sesión: AuthService empuja null en el usuario.
        auth.currentUserSubject.next(null);

        // Para un superadmin la peña sale del selector, no del usuario, así que sin esto
        // sobrevivía al logout y se seguía viendo en el color de la interfaz y en el título.
        expect(servicio.pena()).toBeNull();
        expect(servicio.options()).toEqual([]);
    });

    it('si una selección guardada ya no existe, cae en la primera peña disponible', () => {
        contexto.setSelectedPenaId(404);

        servicio.init();

        expect(servicio.pena()?.id).toBe(7);
        expect(contexto.getSelectedPenaId()).toBe(7);
    });

    it('si falla la carga de peñas no deja opciones ni se queda cargando', () => {
        penaService.fallar = true;

        servicio.init();

        expect(servicio.options()).toEqual([]);
        expect(servicio.loading()).toBeFalse();
    });
});

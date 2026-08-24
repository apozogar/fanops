import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { penaSlugGuard } from './pena-slug.guard';
import { AuthService } from '@/pages/auth/auth.service';
import { Pena } from '@/interfaces/socio.interface';

const MI_PENA: Pena = { id: 7, nombre: 'Peña Siete', slug: 'pena-siete' };

class AuthStub {
    superAdmin = false;
    penaSubject = new BehaviorSubject<Pena | null>(MI_PENA);
    currentPena = this.penaSubject.asObservable();
    isSuperAdmin() {
        return this.superAdmin;
    }
}

/** Ejecuta el guard como lo haría el router, con el snapshot de ruta y de estado que le pasa. */
function ejecutar(slugEnLaUrl: string | null, url: string) {
    const route = { paramMap: convertToParamMap(slugEnLaUrl ? { penaSlug: slugEnLaUrl } : {}) } as ActivatedRouteSnapshot;
    const state = { url } as RouterStateSnapshot;
    return TestBed.runInInjectionContext(() => penaSlugGuard(route, state));
}

describe('penaSlugGuard', () => {
    let auth: AuthStub;
    let router: Router;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [provideRouter([]), { provide: AuthService, useClass: AuthStub }]
        });

        auth = TestBed.inject(AuthService) as unknown as AuthStub;
        router = TestBed.inject(Router);
        spyOn(router, 'navigateByUrl').and.resolveTo(true);
    });

    it('deja pasar cuando el dominio de la URL es el de la peña del usuario', (done) => {
        (ejecutar('pena-siete', '/pena-siete/socios') as any).subscribe((resultado: boolean) => {
            expect(resultado).toBeTrue();
            expect(router.navigateByUrl).not.toHaveBeenCalled();
            done();
        });
    });

    it('ignora las diferencias de mayúsculas del dominio', (done) => {
        (ejecutar('Pena-Siete', '/Pena-Siete/socios') as any).subscribe((resultado: boolean) => {
            expect(resultado).toBeTrue();
            done();
        });
    });

    it('corrige el dominio cuando la URL apunta a otra peña, conservando el resto de la ruta', (done) => {
        // Los datos nunca estuvieron en riesgo (el backend filtra por el usuario autenticado),
        // pero la pantalla mostraría los socios de tu peña bajo la dirección de otra.
        (ejecutar('otra-pena', '/otra-pena/socios?pagina=2#tabla') as any).subscribe((resultado: boolean) => {
            expect(resultado).toBeFalse();
            expect(router.navigateByUrl).toHaveBeenCalledWith('/pena-siete/socios?pagina=2#tabla', { replaceUrl: true });
            done();
        });
    });

    it('deja pasar al superadmin: para él el dominio elige la peña, no se valida', () => {
        auth.superAdmin = true;

        expect(ejecutar('cualquier-pena', '/cualquier-pena/socios')).toBeTrue();
        expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('no bloquea si todavía no se conoce la peña del usuario', (done) => {
        // Sesión recién restaurada, o guardada antes de que existieran los dominios por peña.
        auth.penaSubject.next(null);

        (ejecutar('pena-siete', '/pena-siete/socios') as any).subscribe((resultado: boolean) => {
            expect(resultado).toBeTrue();
            expect(router.navigateByUrl).not.toHaveBeenCalled();
            done();
        });
    });
});

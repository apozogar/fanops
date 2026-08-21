import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PenaContextInterceptor } from './HttpInterceptors';
import { PenaContextService } from '@/services/pena-context.service';

describe('PenaContextInterceptor', () => {
    let http: HttpClient;
    let httpMock: HttpTestingController;
    let contexto: PenaContextService;

    beforeEach(() => {
        localStorage.clear();
        TestBed.configureTestingModule({
            providers: [
                provideHttpClient(withInterceptorsFromDi()),
                provideHttpClientTesting(),
                PenaContextService,
                { provide: HTTP_INTERCEPTORS, useClass: PenaContextInterceptor, multi: true }
            ]
        });
        http = TestBed.inject(HttpClient);
        httpMock = TestBed.inject(HttpTestingController);
        contexto = TestBed.inject(PenaContextService);
    });

    afterEach(() => {
        httpMock.verify();
        localStorage.clear();
    });

    it('envía la peña seleccionada en la cabecera X-Pena-Id', () => {
        contexto.setSelectedPenaId(8);

        http.get('/api/socios').subscribe();

        const req = httpMock.expectOne('/api/socios');
        expect(req.request.headers.get('X-Pena-Id')).toBe('8');
        req.flush({});
    });

    it('refleja el cambio de peña en las peticiones siguientes', () => {
        contexto.setSelectedPenaId(7);
        http.get('/api/socios').subscribe();
        const primera = httpMock.expectOne('/api/socios');
        expect(primera.request.headers.get('X-Pena-Id')).toBe('7');
        primera.flush({});

        contexto.setSelectedPenaId(8);
        http.get('/api/socios').subscribe();
        const segunda = httpMock.expectOne('/api/socios');
        expect(segunda.request.headers.get('X-Pena-Id')).toBe('8');
        segunda.flush({});
    });

    it('no envía la cabecera cuando no hay peña seleccionada (usuario normal)', () => {
        http.get('/api/socios').subscribe();

        const req = httpMock.expectOne('/api/socios');
        expect(req.request.headers.has('X-Pena-Id')).toBeFalse();
        req.flush({});
    });
});

import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PenaSwitcherComponent } from './pena-switcher.component';
import { ActivePenaService } from '@/core/pena/active-pena.service';
import { Pena } from '@/interfaces/socio.interface';

const PENAS: Pena[] = [
    { id: 1, nombre: 'Peña Uno' },
    { id: 2, nombre: 'Peña Dos' },
    { id: 3, nombre: 'Peña Tres' }
];

/**
 * Doble de ActivePenaService que permite simular lo que ocurre de verdad: las peñas llegan de
 * una llamada HTTP, o sea DESPUÉS del primer renderizado del selector.
 */
class ActivePenaStub {
    private readonly _pena = signal<Pena | null>(null);
    private readonly _options = signal<Pena[]>([]);

    readonly pena = this._pena.asReadonly();
    readonly options = this._options.asReadonly();
    readonly loading = signal(false).asReadonly();

    seleccionadoConId: number | null | undefined;

    isSuperAdmin(): boolean {
        return true;
    }

    select(penaId: number | null): void {
        this.seleccionadoConId = penaId;
    }

    /** Simula la respuesta del backend: llegan las opciones y la peña activa. */
    llegaronLasPenas(activa: Pena): void {
        this._options.set(PENAS);
        this._pena.set(activa);
    }
}

describe('PenaSwitcherComponent', () => {
    let stub: ActivePenaStub;

    beforeEach(async () => {
        stub = new ActivePenaStub();
        await TestBed.configureTestingModule({
            imports: [PenaSwitcherComponent],
            providers: [{ provide: ActivePenaService, useValue: stub }]
        }).compileComponents();
    });

    function render() {
        const fixture = TestBed.createComponent(PenaSwitcherComponent);
        fixture.detectChanges(); // primer render: todavía sin opciones
        return fixture;
    }

    it('deja marcada la peña activa cuando las opciones llegan después del primer render', () => {
        const fixture = render();

        stub.llegaronLasPenas(PENAS[1]); // la activa es la segunda, no la primera
        fixture.detectChanges();

        const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
        expect(select.selectedOptions[0]?.textContent?.trim()).toBe('Peña Dos');
    });

    it('avisa del cambio con el id de la peña elegida', () => {
        const fixture = render();
        stub.llegaronLasPenas(PENAS[0]);
        fixture.detectChanges();

        const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
        // Se elige por posición, como haría una persona, para no depender de cómo se
        // codifique el atributo value de cada opción.
        select.selectedIndex = [...select.options].findIndex((o) => o.textContent?.trim() === 'Peña Tres');
        select.dispatchEvent(new Event('change'));
        fixture.detectChanges();

        expect(stub.seleccionadoConId).toBe(3);
    });
});

import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UiButtonDirective } from './ui-button.directive';

@Component({
    standalone: true,
    imports: [UiButtonDirective],
    template: `
        <button foButton variant="primary" [loading]="cargando"><span>Guardar</span></button>
    `
})
class HostComponent {
    cargando = false;
}

describe('UiButtonDirective', () => {
    function render() {
        const fixture = TestBed.createComponent(HostComponent);
        fixture.detectChanges();
        return {
            fixture,
            boton: (): HTMLButtonElement => fixture.nativeElement.querySelector('button')
        };
    }

    it('no marca carga mientras no se le pida', () => {
        const { boton } = render();

        expect(boton().classList).not.toContain('fo-btn-loading');
        expect(boton().getAttribute('aria-busy')).toBeNull();
    });

    it('marca la carga con la clase del indicador y aria-busy', () => {
        const { fixture, boton } = render();

        fixture.componentInstance.cargando = true;
        fixture.detectChanges();

        expect(boton().classList).toContain('fo-btn-loading');
        expect(boton().getAttribute('aria-busy')).toBe('true');
    });

    it('retira la marca de carga al terminar', () => {
        const { fixture, boton } = render();

        fixture.componentInstance.cargando = true;
        fixture.detectChanges();
        fixture.componentInstance.cargando = false;
        fixture.detectChanges();

        expect(boton().classList).not.toContain('fo-btn-loading');
        expect(boton().getAttribute('aria-busy')).toBeNull();
    });

    it('conserva las clases de la variante mientras carga, para no perder el color', () => {
        const { fixture, boton } = render();

        fixture.componentInstance.cargando = true;
        fixture.detectChanges();

        expect(boton().classList).toContain('bg-accent');
        expect(boton().classList).toContain('text-accent-fg');
    });
});

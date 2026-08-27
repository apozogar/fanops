import {coincideBusqueda, normalizarTexto} from './busqueda-flexible';

describe('busqueda-flexible', () => {
    it('normaliza acentos, mayúsculas y signos', () => {
        expect(normalizarTexto('Núñez-Pérez, José')).toBe('nunez perez jose');
        expect(normalizarTexto(null)).toBe('');
    });

    it('encuentra el nombre con las palabras en otro orden', () => {
        const campos = ['12', 'Pozo Garcia, Alberto', '600 12 34 56', 'alberto@ejemplo.com'];
        expect(coincideBusqueda(campos, 'Alberto Pozo')).toBeTrue();
        expect(coincideBusqueda(campos, 'garcia alberto')).toBeTrue();
        expect(coincideBusqueda(campos, 'pozo')).toBeTrue();
    });

    it('ignora acentos en la consulta y en el dato', () => {
        expect(coincideBusqueda(['Núñez Pérez, María'], 'maria nunez')).toBeTrue();
        expect(coincideBusqueda(['Nunez Perez, Maria'], 'maría núñez')).toBeTrue();
    });

    it('busca teléfonos aunque estén guardados con separadores', () => {
        expect(coincideBusqueda(['600 12 34 56'], '600123456')).toBeTrue();
    });

    it('exige que estén todas las palabras', () => {
        expect(coincideBusqueda(['Pozo Garcia, Alberto'], 'alberto lopez')).toBeFalse();
    });

    it('deja pasar la fila si la consulta está vacía', () => {
        expect(coincideBusqueda(['Pozo Garcia, Alberto'], '   ')).toBeTrue();
    });
});

import { fechaRelativaAlEvento } from './fechas-por-defecto';

describe('fechas-por-defecto', () => {
    it('resta los días y fija la hora indicada', () => {
        const sorteo = fechaRelativaAlEvento(new Date(2026, 8, 15), 2, '18:30');

        expect(sorteo.getFullYear()).toBe(2026);
        expect(sorteo.getMonth()).toBe(8);
        expect(sorteo.getDate()).toBe(13);
        expect(sorteo.getHours()).toBe(18);
        expect(sorteo.getMinutes()).toBe(30);
    });

    it('cruza el cambio de mes y de año sin descuadrarse', () => {
        expect(fechaRelativaAlEvento(new Date(2026, 2, 2), 5, '20:00').getMonth()).toBe(1);
        expect(fechaRelativaAlEvento(new Date(2026, 2, 2), 5, '20:00').getDate()).toBe(25);

        const enero = fechaRelativaAlEvento(new Date(2027, 0, 3), 7, '20:00');

        expect(enero.getFullYear()).toBe(2026);
        expect(enero.getMonth()).toBe(11);
        expect(enero.getDate()).toBe(27);
    });

    it('mantiene la hora aunque por medio entre el horario de verano', () => {
        // En España el reloj se adelanta el último domingo de marzo (29/03/2026). Restando días
        // en vez de horas, "dos días antes a las 20:00" sigue siendo a las 20:00.
        const cierre = fechaRelativaAlEvento(new Date(2026, 2, 31), 3, '20:00');

        expect(cierre.getDate()).toBe(28);
        expect(cierre.getHours()).toBe(20);
    });

    it('usa las 20:00 si la peña no ha configurado hora', () => {
        expect(fechaRelativaAlEvento(new Date(2026, 8, 15), 1, null).getHours()).toBe(20);
        expect(fechaRelativaAlEvento(new Date(2026, 8, 15), 1, '').getHours()).toBe(20);
    });

    it('no toca la fecha original', () => {
        const evento = new Date(2026, 8, 15, 12, 0);

        fechaRelativaAlEvento(evento, 4, '09:00');

        expect(evento.getDate()).toBe(15);
        expect(evento.getHours()).toBe(12);
    });
});

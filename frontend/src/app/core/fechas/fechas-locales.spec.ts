import { aFechaHoraLocal, aFechaLocal, deFechaLocal } from './fechas-locales';

describe('fechas-locales', () => {
    it('envía el día elegido aunque en UTC todavía sea el anterior', () => {
        // Medianoche local del 14: en España, en UTC aún es el 13 a las 22:00.
        const catorce = new Date(2026, 9, 14, 0, 0, 0);
        expect(aFechaLocal(catorce)).toBe('2026-10-14');
    });

    it('envía la hora local, sin pasar a UTC', () => {
        expect(aFechaHoraLocal(new Date(2026, 9, 11, 19, 0, 0))).toBe('2026-10-11T19:00:00');
    });

    it('lee un yyyy-MM-dd del backend como día local', () => {
        const fecha = deFechaLocal('2026-10-14')!;
        expect(fecha.getFullYear()).toBe(2026);
        expect(fecha.getMonth()).toBe(9);
        expect(fecha.getDate()).toBe(14);
        expect(fecha.getHours()).toBe(0);
    });

    it('deja las fechas que vienen del backend tal cual al reenviarlas', () => {
        expect(aFechaLocal('2026-10-14')).toBe('2026-10-14');
        expect(aFechaHoraLocal('2026-10-11T19:00:00')).toBe('2026-10-11T19:00:00');
    });

    it('sin fecha manda null', () => {
        expect(aFechaLocal(null)).toBeNull();
        expect(aFechaHoraLocal(undefined)).toBeNull();
        expect(aFechaLocal('')).toBeNull();
    });
});

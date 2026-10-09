import { esHistorico } from './historico';

describe('esHistorico', () => {
    const evento = '2026-10-14';

    it('un evento futuro o de hoy sigue vigente', () => {
        expect(esHistorico(evento, new Date(2026, 9, 10))).toBeFalse();
        expect(esHistorico(evento, new Date(2026, 9, 14, 23, 59))).toBeFalse();
    });

    it('al día siguiente todavía se ve: es el margen de un día', () => {
        expect(esHistorico(evento, new Date(2026, 9, 15, 0, 0))).toBeFalse();
        expect(esHistorico(evento, new Date(2026, 9, 15, 23, 59))).toBeFalse();
    });

    it('pasado el margen pasa al histórico, también cruzando de mes', () => {
        expect(esHistorico(evento, new Date(2026, 9, 16, 0, 0))).toBeTrue();
        expect(esHistorico('2026-10-31', new Date(2026, 10, 2))).toBeTrue();
        expect(esHistorico('2026-10-31', new Date(2026, 10, 1))).toBeFalse();
    });

    it('sin fecha no se considera histórico', () => {
        expect(esHistorico(null)).toBeFalse();
    });
});

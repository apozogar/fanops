import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

const STORAGE_KEY = 'superadminPenaId';

/**
 * Peña que el superadmin ha elegido trabajar desde el selector de la cabecera. Un usuario normal
 * (admin/socio) no usa esto en absoluto: su peña es siempre la suya propia y sale del backend.
 *
 * El id seleccionado se persiste en localStorage para sobrevivir a un recargado de página, y se
 * envía en cada petición HTTP como cabecera "X-Pena-Id" (ver PenaContextInterceptor).
 */
@Injectable({
    providedIn: 'root'
})
export class PenaContextService {
    private selectedPenaIdSubject = new BehaviorSubject<number | null>(this.loadFromStorage());
    public selectedPenaId$ = this.selectedPenaIdSubject.asObservable();

    getSelectedPenaId(): number | null {
        return this.selectedPenaIdSubject.getValue();
    }

    setSelectedPenaId(penaId: number | null): void {
        this.selectedPenaIdSubject.next(penaId);
        if (penaId === null) {
            localStorage.removeItem(STORAGE_KEY);
        } else {
            localStorage.setItem(STORAGE_KEY, String(penaId));
        }
    }

    clear(): void {
        this.setSelectedPenaId(null);
    }

    private loadFromStorage(): number | null {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) {
            return null;
        }
        const parsed = Number(raw);
        return Number.isFinite(parsed) ? parsed : null;
    }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';

interface InfoResponse {
    build?: {
        commit?: string;
        time?: string;
    };
}

export interface VersionInfo {
    commit: string;
    fecha: Date | null;
}

/**
 * Identifica qué despliegue está corriendo, a partir del commit con el que se compiló el jar
 * (ver la propiedad adicional "commit" del build-info del pom.xml y /management/info). Sirve
 * para confirmar, tras un despliegue, que lo que se ve en el navegador ya es la versión nueva
 * y no una caché vieja.
 */
@Injectable({ providedIn: 'root' })
export class VersionService {
    private readonly http = inject(HttpClient);

    readonly version = toSignal(
        this.http.get<InfoResponse>(`${environment.apiUrl}/management/info`).pipe(
            map((info): VersionInfo => ({
                commit: info.build?.commit ?? '',
                fecha: info.build?.time ? new Date(info.build.time) : null
            })),
            // Sin /management/info (entorno sin actuator, o caído) no se rompe el panel de
            // cuenta por esto: simplemente no se muestra la versión.
            catchError(() => of(null))
        ),
        { initialValue: null }
    );
}

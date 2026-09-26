import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';

interface InfoResponse {
    build?: {
        version?: string;
        commit?: string;
        time?: string;
    };
}

export interface VersionInfo {
    version: string;
    /** null si el build no supo el commit (p.ej. Coolify sin "Include Source Commit in Build"). */
    commit: string | null;
    fecha: Date | null;
}

/**
 * Identifica qué despliegue está corriendo, a partir de la versión del pom.xml y el commit con
 * el que se compiló el jar (ver la propiedad adicional "commit" del build-info y
 * /management/info). Sirve para confirmar, tras un despliegue, que lo que se ve en el navegador
 * ya es la versión nueva y no una caché vieja.
 */
@Injectable({ providedIn: 'root' })
export class VersionService {
    private readonly http = inject(HttpClient);

    readonly version = toSignal(
        this.http.get<InfoResponse>(`${environment.apiUrl}/management/info`).pipe(
            map((info): VersionInfo => {
                const commit = info.build?.commit;
                return {
                    version: info.build?.version ?? '',
                    // "local" y "unknown" son los valores por defecto que pone el pom.xml/Dockerfile
                    // cuando no hay commit real (build local, o Coolify sin el commit activado):
                    // no son un commit y no deben mostrarse como si lo fueran.
                    commit: commit && commit !== 'local' && commit !== 'unknown' ? commit : null,
                    fecha: info.build?.time ? new Date(info.build.time) : null
                };
            }),
            // Sin /management/info (entorno sin actuator, o caído) no se rompe el panel de
            // cuenta por esto: simplemente no se muestra la versión.
            catchError(() => of(null))
        ),
        { initialValue: null }
    );
}

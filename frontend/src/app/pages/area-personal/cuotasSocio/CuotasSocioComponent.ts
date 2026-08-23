import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CardModule } from 'primeng/card';
import { CarnetDto, Pena, Socio } from '@/interfaces/socio.interface';
import { ApiResponse } from '@/interfaces/api-response.interface';
import { CuotasSocioTableComponent } from '@/components/cuotas-socio-table/cuotas-socio-table.component';
import { environment } from '../../../../environments/environment';

/**
 * Pantalla "Mis Cuotas": historial de pagos de cada socio asociado al usuario.
 *
 * Vive en su propia ruta/pestaña de navegación (en vez de compartir pantalla con el carnet)
 * porque son dos tareas distintas: consultar los datos del carnet frente a repasar el
 * historial de cobros. Reutiliza el mismo endpoint que el carnet porque ya trae las cuotas
 * de cada socio en la misma respuesta.
 */
@Component({
    selector: 'app-cuotas-socio',
    standalone: true,
    imports: [CardModule, CuotasSocioTableComponent],
    templateUrl: 'CuotasSocioComponent.html',
    styleUrl: 'CuotasSocioComponent.scss'
})
export class CuotasSocioComponent implements OnInit {
    penaInfo: Pena | null = null;
    socios: Socio[] = [];

    private http = inject(HttpClient);

    ngOnInit(): void {
        this.http.get<ApiResponse<CarnetDto>>(`${environment.apiUrl}/api/socios/me`).subscribe((response) => {
            if (response.success && response.data) {
                this.penaInfo = response.data.penaInfo;
                this.socios = response.data.socios;
            }
        });
    }
}

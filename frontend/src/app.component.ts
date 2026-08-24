import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TituloPaginaService } from '@/core/platform/titulo-pagina.service';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterModule],
    template: `<router-outlet></router-outlet>`
})
export class AppComponent {
    /*
     * Se inyecta solo para crearlo: es un servicio de raíz y su effect no arranca hasta que algo
     * lo pide. Aquí, en el componente raíz, queda activo durante toda la vida de la aplicación.
     */
    private readonly titulo = inject(TituloPaginaService);
}

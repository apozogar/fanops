import { Component, OnInit } from '@angular/core';

import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { map } from 'rxjs/operators';
import { AuthService } from '@/pages/auth/auth.service';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [AppMenuitem, RouterModule],
    template: ` <ul class="layout-menu">
        @for (item of model; track item; let i = $index) {
            @if (!item.separator) {
                <li app-menuitem [item]="item" [index]="i" [root]="true"></li>
            }
            @if (item.separator) {
                <li class="menu-separator"></li>
            }
        }
    </ul>`
})
export class AppMenu implements OnInit {
    model: MenuItem[] = [];

    constructor(private authService: AuthService) {}

    ngOnInit() {
        this.authService.currentUser.pipe(map((user) => user?.authorities?.some((auth) => auth.authority === 'ROLE_ADMIN') ?? false)).subscribe((isAdmin: boolean) => {
            this.model = [];
            this.model.push({
                label: 'Area personal',
                items: [
                    {
                        label: 'Mi carnet',
                        routerLink: ['/carnet-socio'],
                        icon: 'pi pi-id-card'
                    },
                    {
                        label: 'Inscripción a Eventos',
                        routerLink: ['/inscripciones'],
                        icon: 'pi pi-calendar-plus'
                    }
                ]
            });
            if (isAdmin) {
                this.model.push({
                    label: 'Gestion socios',
                    items: [
                        {
                            label: 'Socios',
                            icon: 'pi pi-users',
                            routerLink: ['/socios']
                        }
                    ]
                });
                this.model.push({
                    label: 'Gestion eventos',
                    items: [
                        {
                            label: 'Eventos',
                            icon: 'pi pi-calendar',
                            routerLink: ['/eventos']
                        }
                    ]
                });
            }
        });
    }
}

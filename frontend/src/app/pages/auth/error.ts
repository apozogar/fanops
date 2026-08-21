import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { ThemeToggleComponent } from '@/ui/theme-toggle.component';

import { IconComponent } from '@/ui/icon/icon.component';
import { UiButtonDirective } from '@/ui/ui-button.directive';
@Component({
    selector: 'app-error',
    imports: [UiButtonDirective, IconComponent, ButtonModule, RippleModule, RouterModule, ThemeToggleComponent, ButtonModule],
    standalone: true,
    template: ` <fo-theme-toggle [floating]="true" />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-screen overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, rgba(233, 30, 99, 0.4) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20 flex flex-col items-center" style="border-radius: 53px">
                        <div class="gap-4 flex flex-col items-center">
                            <div class="flex justify-center items-center border-2 border-pink-500 rounded-full" style="height: 3.2rem; width: 3.2rem">
                                <fo-icon name="error" [size]="26" class="text-pink-500" />
                            </div>
                            <h1 class="text-surface-900 dark:text-surface-0 font-bold text-5xl mb-2">Error Occured</h1>
                            <span class="text-muted-color mb-8">Requested resource is not available.</span>
                            <img src="https://primefaces.org/cdn/templates/sakai/auth/asset-error.svg" alt="Error" class="mb-8" width="80%" />
                            <div class="col-span-12 mt-8 text-center">
                                <button foButton variant="danger" routerLink="/">Ir al inicio</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>`
})
export class Error {}

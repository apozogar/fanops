import { Component, booleanAttribute, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { IconComponent } from './icon/icon.component';
import { UiInputDirective } from './ui-input.directive';

/**
 * Campo de contraseña con botón para mostrar u ocultar el texto.
 *
 * Sustituye a `p-password`, que arrastraba su propio contenedor con ancho de contenido y era la
 * causa de que en móvil el campo desbordara la tarjeta. Aquí el control es un `<input>` nativo
 * con `foInput`, así que mide exactamente lo que su contenedor.
 *
 * Implementa ControlValueAccessor, de modo que sigue funcionando con `[(ngModel)]` igual que
 * antes y no hay que tocar la lógica de los componentes que lo usan.
 *
 * @example <fo-password [(ngModel)]="password" name="password" placeholder="Contraseña" />
 */
@Component({
    selector: 'fo-password',
    standalone: true,
    imports: [UiInputDirective, IconComponent],
    host: { class: 'block' },
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => UiPasswordComponent),
            multi: true
        }
    ],
    template: `
        <div class="relative">
            <input
                foInput
                [trailingSlot]="true"
                [invalid]="invalid()"
                [id]="inputId()"
                [type]="revealed() ? 'text' : 'password'"
                [placeholder]="placeholder()"
                [autocomplete]="autocomplete()"
                [disabled]="disabled()"
                [value]="value()"
                (input)="onInput($event)"
                (blur)="onTouched()"
            />
            <button
                type="button"
                class="absolute inset-y-0 right-0 flex w-11 items-center justify-center rounded-r-token text-ink-muted transition-colors hover:text-ink"
                [attr.aria-label]="revealed() ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                [attr.aria-pressed]="revealed()"
                (click)="toggle()"
                tabindex="-1"
            >
                <fo-icon [name]="revealed() ? 'ocultar' : 'ver'" [size]="17" />
            </button>
        </div>
    `
})
export class UiPasswordComponent implements ControlValueAccessor {
    readonly inputId = input<string | null>(null);
    readonly placeholder = input<string>('');
    readonly invalid = input(false, { transform: booleanAttribute });
    /**
     * Pista al gestor de contraseñas. Conviene distinguir `current-password` (inicio de sesión)
     * de `new-password` (registro y restablecimiento) para que no ofrezca la contraseña antigua
     * donde se está creando una nueva.
     */
    readonly autocomplete = input<string>('current-password');

    protected readonly value = signal<string>('');
    protected readonly revealed = signal(false);
    protected readonly disabled = signal(false);

    private onChange: (value: string) => void = () => {};
    protected onTouched: () => void = () => {};

    protected toggle(): void {
        this.revealed.update((v) => !v);
    }

    protected onInput(event: Event): void {
        const next = (event.target as HTMLInputElement).value;
        this.value.set(next);
        this.onChange(next);
    }

    writeValue(value: string | null): void {
        this.value.set(value ?? '');
    }

    registerOnChange(fn: (value: string) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled.set(isDisabled);
    }
}

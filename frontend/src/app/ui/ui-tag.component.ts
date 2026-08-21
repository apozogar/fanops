import { Component, computed, input } from '@angular/core';

export type TagTone = 'neutral' | 'accent' | 'success' | 'warn' | 'danger' | 'info';

const TONES: Record<TagTone, string> = {
    neutral: 'bg-surface-2 text-ink-muted',
    accent: 'bg-accent-soft text-accent-soft-fg',
    success: 'bg-success-soft text-success-soft-fg',
    warn: 'bg-warn-soft text-warn-soft-fg',
    danger: 'bg-danger-soft text-danger-soft-fg',
    info: 'bg-info-soft text-info-soft-fg'
};

/** Etiqueta de estado. Sustituye a p-tag. */
@Component({
    selector: 'fo-tag',
    standalone: true,
    template: `
        <span [class]="classes()">
            <ng-content />
        </span>
    `
})
export class UiTagComponent {
    readonly tone = input<TagTone>('neutral');

    protected readonly classes = computed(
        () => `inline-flex items-center gap-1 px-2 py-0.5 rounded-token-sm text-xs font-medium ${TONES[this.tone()]}`
    );
}

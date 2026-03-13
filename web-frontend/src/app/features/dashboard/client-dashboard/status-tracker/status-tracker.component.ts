import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule } from '@ngx-translate/core';

export interface TrackerStep {
  labelKey: string;
  subKey: string;
  state: 'done' | 'active' | 'pending';
}

@Component({
  selector: 'app-status-tracker',
  standalone: true,
  imports: [MatCardModule, TranslateModule],
  template: `
    <div class="tracker">
      <div class="steps">
        @for (step of steps; track step.labelKey) {
          <div class="step">
            <div class="step-track">
              <div class="step-dot {{ step.state }}">
                @if (step.state === 'done') {
                  <span>✓</span>
                } @else if (step.state === 'active') {
                  <span>→</span>
                } @else {
                  <span>{{ $index + 1 }}</span>
                }
              </div>
              @if (!$last) {
                <div class="step-line" [class.done]="step.state === 'done'"></div>
              }
            </div>
            <div class="step-content">
              <div class="step-label" [class.pending]="step.state === 'pending'" [textContent]="step.labelKey | translate"></div>
              <div class="step-sub" [textContent]="step.subKey | translate"></div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [
    `
      .tracker {
        padding: 0;
      }
      .steps {
        display: flex;
        flex-direction: column;
        gap: 0;
      }
      .step {
        display: flex;
        gap: 14px;
      }
      .step-track {
        display: flex;
        flex-direction: column;
        align-items: center;
      }
      .step-dot {
        width: 22px;
        height: 22px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        font-size: 10px;
        font-weight: 700;
      }
      .step-dot.done {
        background: var(--green);
        color: white;
      }
      .step-dot.active {
        background: var(--amber);
        color: white;
        box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.2);
      }
      .step-dot.pending {
        background: var(--border);
        color: var(--text-muted);
        border: 2px solid var(--border-strong);
      }
      .step-line {
        width: 2px;
        flex: 1;
        min-height: 24px;
        background: var(--border);
        margin: 4px 0;
      }
      .step-line.done {
        background: var(--green);
      }
      .step-content {
        padding: 1px 0 24px;
        flex: 1;
      }
      .step-label {
        font-size: 13px;
        font-weight: 600;
        color: var(--text-primary);
      }
      .step-label.pending {
        color: var(--text-muted);
        font-weight: 400;
      }
      .step-sub {
        font-size: 11px;
        color: var(--text-secondary);
        margin-top: 2px;
      }
    `,
  ],
})
export class StatusTrackerComponent {
  readonly steps: TrackerStep[] = [
    { labelKey: 'status.received', subKey: 'status.receivedSub', state: 'done' },
    { labelKey: 'status.triage', subKey: 'status.triageSub', state: 'done' },
    { labelKey: 'status.inAnalysis', subKey: 'status.inAnalysisSub', state: 'active' },
    { labelKey: 'status.awaitingValidation', subKey: 'status.awaitingValidationSub', state: 'pending' },
    { labelKey: 'status.resolved', subKey: 'status.resolvedSub', state: 'pending' },
  ];
}

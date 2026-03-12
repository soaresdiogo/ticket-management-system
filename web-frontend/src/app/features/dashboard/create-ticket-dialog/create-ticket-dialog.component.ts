import {
  Component,
  inject,
  signal,
  computed,
  ChangeDetectionStrategy,
  OnInit,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { concatMap, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { TicketService } from '../../../core/services/ticket.service';
import { FileService } from '../../../core/services/file.service';
import type { CreateTicketResponse } from '../../../core/models/ticket.model';

/** Max file size (10 MB) aligned with backend. */
const MAX_FILE_SIZE_BYTES = 10_485_760;

/** Allowed MIME types aligned with file-service. */
const ALLOWED_TYPES = new Set([
  'application/pdf',
  'image/jpeg',
  'image/jpg',
  'image/png',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
]);

export const PRIORITY_OPTIONS = [
  { value: 'LOW', labelKey: 'createTicket.priorityLow' },
  { value: 'NORMAL', labelKey: 'createTicket.priorityNormal' },
  { value: 'HIGH', labelKey: 'createTicket.priorityHigh' },
  { value: 'URGENT', labelKey: 'createTicket.priorityUrgent' },
] as const;

@Component({
  selector: 'app-create-ticket-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslateModule,
  ],
  templateUrl: './create-ticket-dialog.component.html',
  styleUrl: './create-ticket-dialog.component.scss',
})
export class CreateTicketDialogComponent implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<CreateTicketDialogComponent>);
  private readonly fb = inject(FormBuilder);
  private readonly ticketService = inject(TicketService);
  private readonly fileService = inject(FileService);

  readonly priorityOptions = PRIORITY_OPTIONS;

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', Validators.required],
    priority: ['NORMAL' as const, Validators.required],
    category: [''],
  });

  readonly selectedFiles = signal<File[]>([]);
  readonly submitInProgress = signal(false);
  readonly submitError = signal<string | null>(null);

  /** Form validity as a signal so computed(canSubmit) re-runs when form becomes valid. */
  private readonly formValid = signal(this.form.valid);

  // #region agent log
  readonly canSubmit = computed(() => {
    const valid = this.formValid();
    const inProgress = this.submitInProgress();
    const result = valid && !inProgress;
    fetch('http://127.0.0.1:7584/ingest/3ae02373-567d-4b7d-be79-e71134a965c8', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Debug-Session-Id': '204607' },
      body: JSON.stringify({
        sessionId: '204607',
        runId: 'post-fix',
        location: 'create-ticket-dialog.component.ts:canSubmit',
        message: 'canSubmit evaluated',
        data: { formValid: valid, submitInProgress: inProgress, result },
        timestamp: Date.now(),
        hypothesisId: 'A',
      }),
    }).catch(() => {});
    return result;
  });
  // #endregion

  ngOnInit(): void {
    this.formValid.set(this.form.valid);
    this.form.statusChanges.subscribe((status) => {
      this.formValid.set(this.form.valid);
      // #region agent log
      fetch('http://127.0.0.1:7584/ingest/3ae02373-567d-4b7d-be79-e71134a965c8', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Debug-Session-Id': '204607' },
        body: JSON.stringify({
          sessionId: '204607',
          runId: 'post-fix',
          location: 'create-ticket-dialog.component.ts:statusChanges',
          message: 'form status changed',
          data: { status, formValid: this.form.valid, value: this.form.getRawValue() },
          timestamp: Date.now(),
          hypothesisId: 'B',
        }),
      }).catch(() => {});
      // #endregion
    });
  }

  readonly fileError = signal<string | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (!files?.length) return;

    this.fileError.set(null);
    const toAdd: File[] = [];
    for (const file of Array.from(files)) {
      if (file.size > MAX_FILE_SIZE_BYTES) {
        this.fileError.set('createTicket.fileTooLarge');
        continue;
      }
      const allowed = file.type ? ALLOWED_TYPES.has(file.type.toLowerCase()) : false;
      if (!allowed) {
        this.fileError.set('createTicket.fileTypeNotAllowed');
        continue;
      }
      toAdd.push(file);
    }
    this.selectedFiles.update((prev) => [...prev, ...toAdd]);
    input.value = '';
  }

  removeFile(index: number): void {
    this.selectedFiles.update((prev) => prev.filter((_, i) => i !== index));
    this.fileError.set(null);
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

  submit(): void {
    if (!this.form.valid || this.submitInProgress()) return;

    this.submitError.set(null);
    this.submitInProgress.set(true);

    const { title, description, priority, category } = this.form.getRawValue();
    const request = {
      title,
      description,
      priority,
      category: category || null,
    };

    this.ticketService
      .createTicket(request)
      .pipe(
        concatMap((ticket) => this.uploadFilesAndReturnTicket(ticket)),
        catchError((err) => {
          this.submitInProgress.set(false);
          const msg =
            err?.error?.message ?? err?.message ?? 'createTicket.submitError';
          this.submitError.set(msg);
          return of(null);
        })
      )
      .subscribe((result) => {
        this.submitInProgress.set(false);
        if (result) this.dialogRef.close(result);
      });
  }

  private uploadFilesAndReturnTicket(
    ticket: CreateTicketResponse
  ): import('rxjs').Observable<CreateTicketResponse> {
    const files = this.selectedFiles();
    if (files.length === 0) return of(ticket);

    const ticketId = ticket.id;
    const uploads = files.map((file) =>
      this.fileService.uploadFile(ticketId, file).pipe(
        catchError(() => of(null))
      )
    );
    return forkJoin(uploads).pipe(
      map(() => ticket),
      catchError(() => of(ticket))
    );
  }
}

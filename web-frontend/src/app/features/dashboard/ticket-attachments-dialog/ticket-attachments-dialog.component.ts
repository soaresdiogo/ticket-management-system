import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';

import { FileService } from '../../../core/services/file.service';
import type { FileListItem } from '../../../core/models/file.model';

export interface TicketAttachmentsDialogData {
  ticketId: string;
  ticketDisplayId?: string;
}

@Component({
  selector: 'app-ticket-attachments-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DecimalPipe,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatProgressSpinnerModule,
    TranslateModule,
  ],
  templateUrl: './ticket-attachments-dialog.component.html',
  styleUrl: './ticket-attachments-dialog.component.scss',
})
export class TicketAttachmentsDialogComponent {
  private readonly fileService = inject(FileService);
  private readonly dialogRef = inject(MatDialogRef<TicketAttachmentsDialogComponent>);
  readonly data = inject<TicketAttachmentsDialogData>(MAT_DIALOG_DATA);

  readonly loading = signal(true);
  readonly error = signal(false);
  private readonly files = signal<FileListItem[]>([]);

  readonly fileList = computed(() => this.files());
  readonly hasFiles = computed(() => this.fileList().length > 0);
  readonly isEmpty = computed(() => !this.loading() && !this.error() && this.fileList().length === 0);

  constructor() {
    this.fileService.listByTicket(this.data.ticketId).subscribe({
      next: (list) => {
        this.files.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  download(file: FileListItem): void {
    this.fileService.downloadAttachment(file.id);
  }

  close(): void {
    this.dialogRef.close();
  }
}

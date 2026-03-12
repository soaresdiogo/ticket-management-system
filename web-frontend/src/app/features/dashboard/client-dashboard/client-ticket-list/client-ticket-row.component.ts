import { Component, input } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

/**
 * Presentational component: displays a single ticket row with title, description, status and date.
 * Receives already-mapped display data to keep single responsibility and testability.
 */
export interface ClientTicketRowDisplay {
  id: string;
  title: string;
  description: string;
  statusLabelKey: string;
  statusCssClass: string;
  createdAtFormatted: string;
}

@Component({
  selector: 'app-client-ticket-row',
  standalone: true,
  imports: [MatListModule, MatChipsModule, MatIconModule, TranslateModule],
  templateUrl: './client-ticket-row.component.html',
  styleUrl: './client-ticket-row.component.scss',
})
export class ClientTicketRowComponent {
  /** Mapped ticket data for display (status label key and CSS class from TicketStatusService). */
  readonly ticket = input.required<ClientTicketRowDisplay>();
}

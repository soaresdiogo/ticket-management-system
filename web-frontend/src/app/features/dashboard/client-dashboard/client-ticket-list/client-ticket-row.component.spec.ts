import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

import { ClientTicketRowComponent, type ClientTicketRowDisplay } from './client-ticket-row.component';
import { MockTranslatePipe } from '../../../../core/testing/translate.mock';

const mockTicket: ClientTicketRowDisplay = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  title: 'IRPJ divergence',
  description: 'Base de cálculo divergente.',
  statusLabelKey: 'client.ticketStatus.open',
  statusCssClass: 'status-open',
  createdAtFormatted: '3/12/25, 10:00 AM',
};

describe('ClientTicketRowComponent', () => {
  let component: ClientTicketRowComponent;
  let fixture: ComponentFixture<ClientTicketRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ClientTicketRowComponent,
        MatListModule,
        MatChipsModule,
        MatIconModule,
        MockTranslatePipe,
      ],
    })
      .overrideComponent(ClientTicketRowComponent, {
        set: {
          imports: [MatListModule, MatChipsModule, MatIconModule, MockTranslatePipe],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ClientTicketRowComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('ticket', mockTicket);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display ticket title and description', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain(mockTicket.title);
    expect(el.textContent).toContain(mockTicket.description);
  });

  it('should display formatted created date', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain(mockTicket.createdAtFormatted);
  });

  it('should apply status CSS class to chip', () => {
    const chip = fixture.nativeElement.querySelector('.pill.status-open');
    expect(chip).toBeTruthy();
  });
});

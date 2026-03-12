import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { of, throwError } from 'rxjs';

import { CreateTicketDialogComponent } from './create-ticket-dialog.component';
import { TicketService } from '../../../core/services/ticket.service';
import { FileService } from '../../../core/services/file.service';
import { MockTranslatePipe } from '../../../core/testing/translate.mock';
import type { CreateTicketResponse } from '../../../core/models/ticket.model';

const mockCreateResponse: CreateTicketResponse = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  tenantId: 't1',
  clientId: 'c1',
  title: 'Test',
  description: 'Desc',
  status: 'OPEN',
  priority: 'NORMAL',
  category: null,
  createdAt: '2025-03-12T10:00:00Z',
};

describe('CreateTicketDialogComponent', () => {
  let component: CreateTicketDialogComponent;
  let fixture: ComponentFixture<CreateTicketDialogComponent>;
  let dialogRefMock: { close: ReturnType<typeof vi.fn> };
  let ticketServiceMock: { createTicket: ReturnType<typeof vi.fn> };
  let fileServiceMock: { uploadFile: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRefMock = { close: vi.fn() };
    ticketServiceMock = { createTicket: vi.fn().mockReturnValue(of(mockCreateResponse)) };
    fileServiceMock = { uploadFile: vi.fn().mockReturnValue(of({ id: 'f1' })) };

    await TestBed.configureTestingModule({
      imports: [
        CreateTicketDialogComponent,
        NoopAnimationsModule,
      ],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: TicketService, useValue: ticketServiceMock },
        { provide: FileService, useValue: fileServiceMock },
      ],
    })
      .overrideComponent(CreateTicketDialogComponent, {
        set: {
          imports: [
            ReactiveFormsModule,
            MatDialogModule,
            MatFormFieldModule,
            MatInputModule,
            MatSelectModule,
            MatButtonModule,
            MatIconModule,
            MatProgressSpinnerModule,
            MockTranslatePipe,
          ],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(CreateTicketDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have priority options', () => {
    expect(component.priorityOptions.length).toBe(4);
    expect(component.form.get('priority')?.value).toBe('NORMAL');
  });

  it('cancel should close dialog with null', () => {
    component.cancel();
    expect(dialogRefMock.close).toHaveBeenCalledWith(null);
  });

  it('submit should not call API when form invalid', () => {
    component.form.patchValue({ title: '', description: '' });
    component.submit();
    expect(ticketServiceMock.createTicket).not.toHaveBeenCalled();
  });

  it('submit should create ticket when valid and close with result', async () => {
    component.form.patchValue({
      title: 'Test ticket',
      description: 'Description',
      priority: 'NORMAL',
      category: '',
    });
    component.submit();
    expect(ticketServiceMock.createTicket).toHaveBeenCalledWith({
      title: 'Test ticket',
      description: 'Description',
      priority: 'NORMAL',
      category: null,
    });
    await new Promise((r) => setTimeout(r, 50));
    expect(dialogRefMock.close).toHaveBeenCalledWith(mockCreateResponse);
  });

  it('submit should set error on createTicket failure', async () => {
    ticketServiceMock.createTicket.mockReturnValue(
      throwError(() => new Error('createTicket.submitError'))
    );
    component.form.patchValue({
      title: 'Test',
      description: 'Desc',
      priority: 'NORMAL',
    });
    component.submit();
    await new Promise((r) => setTimeout(r, 50));
    expect(component.submitError()).toBeTruthy();
    expect(dialogRefMock.close).not.toHaveBeenCalled();
  });

  it('removeFile should remove file at index', () => {
    component.selectedFiles.set([
      new File(['x'], 'a.pdf', { type: 'application/pdf' }),
      new File(['y'], 'b.pdf', { type: 'application/pdf' }),
    ]);
    component.removeFile(0);
    expect(component.selectedFiles().length).toBe(1);
    expect(component.selectedFiles()[0].name).toBe('b.pdf');
  });
});

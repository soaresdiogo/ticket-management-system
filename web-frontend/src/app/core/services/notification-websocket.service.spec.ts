import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { NgZone } from '@angular/core';

import { NotificationWebSocketService } from './notification-websocket.service';
import { AuthService } from './auth.service';

describe('NotificationWebSocketService', () => {
  let service: NotificationWebSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        NotificationWebSocketService,
        { provide: NgZone, useValue: { run: (fn: () => void) => fn(), runOutsideAngular: (fn: () => void) => fn() } },
        { provide: AuthService, useValue: { getAccessToken: () => null } },
      ],
    });
    service = TestBed.inject(NotificationWebSocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should expose notifications$ as Observable', () => {
    expect(service.notifications$).toBeDefined();
    expect(typeof service.notifications$.subscribe).toBe('function');
  });

  it('connect should not throw', () => {
    expect(() => service.connect()).not.toThrow();
  });

  it('disconnect should not throw', () => {
    service.connect();
    expect(() => service.disconnect()).not.toThrow();
  });
});
